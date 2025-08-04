package york.fse.budgetappbackend.service;

import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import york.fse.budgetappbackend.dto.TransactionRequestDTO;
import york.fse.budgetappbackend.model.RecurringTransaction;
import york.fse.budgetappbackend.repository.RecurringTransactionRepository;

import java.time.LocalDate;
import java.util.List;

@Component
public class RecurringTransactionScheduler {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final TransactionService transactionService;

    public RecurringTransactionScheduler(
            RecurringTransactionRepository recurringTransactionRepository,
            TransactionService transactionService) {
        this.recurringTransactionRepository = recurringTransactionRepository;
        this.transactionService = transactionService;
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void generateRecurringTransactions() {
        System.out.println("Starting recurring transaction generation...");

        LocalDate today = LocalDate.now();
        LocalDate twoMonthsAhead = today.plusMonths(2);

        List<RecurringTransaction> allRecurring = recurringTransactionRepository.findAll();
        System.out.println("DEBUG: Found " + allRecurring.size() + " recurring transactions");

        for (RecurringTransaction recurring : allRecurring) {
            System.out.println("DEBUG: Processing recurring transaction: " + recurring.getDescription() + ", nextDate: " + recurring.getNextDate());
            processRecurringTransaction(recurring, today, twoMonthsAhead);
        }

        System.out.println("Completed recurring transaction generation.");
    }

    @Transactional
    public void runManually() {
        System.out.println("Manual trigger started...");
        generateRecurringTransactions();
    }

    private void processRecurringTransaction(RecurringTransaction recurring, LocalDate today, LocalDate endDate) {
        LocalDate currentDate = recurring.getNextDate();
        System.out.println("DEBUG: Original nextDate: " + currentDate + ", today: " + today + ", endDate: " + endDate);

        if (currentDate.isAfter(endDate)) {
            System.out.println("DEBUG: NextDate is beyond our window, starting from today");
            currentDate = today;
        }

        if (currentDate.isBefore(today)) {
            System.out.println("DEBUG: NextDate is in the past, starting from today");
            currentDate = today;
        }

        int transactionsCreated = 0;

        while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
            System.out.println("DEBUG: Checking date: " + currentDate + ", creating transaction: " + !currentDate.isBefore(today));

            if (!currentDate.isBefore(today)) {
                createTransactionFromRecurring(recurring, currentDate);
                transactionsCreated++;
            }

            currentDate = calculateNextDate(currentDate, recurring.getFrequency());
        }

        System.out.println("DEBUG: Created " + transactionsCreated + " transactions for " + recurring.getDescription());

        recurring.setNextDate(currentDate);
        recurringTransactionRepository.save(recurring);
    }

    private LocalDate calculateNextDate(LocalDate currentDate, String frequency) {
        return switch (frequency.toLowerCase()) {
            case "daily" -> currentDate.plusDays(1);
            case "weekly" -> currentDate.plusWeeks(1);
            case "monthly" -> currentDate.plusMonths(1);
            case "quarterly" -> currentDate.plusMonths(3);
            case "yearly" -> currentDate.plusYears(1);
            default -> currentDate.plusMonths(1);
        };
    }

    private void createTransactionFromRecurring(RecurringTransaction recurring, LocalDate date) {
        try {
            System.out.println("DEBUG: Recurring categories: " + recurring.getCategories());

            TransactionRequestDTO dto = new TransactionRequestDTO();
            dto.setDescription(recurring.getDescription() + " (Auto)");
            dto.setAmount(recurring.getAmount());
            dto.setDate(date);
            dto.setType("EXPENSE");
            dto.setAccountId(recurring.getAccount().getId());

            List<String> categories = recurring.getCategories();
            dto.setCategories(categories != null ? List.copyOf(categories) : List.of());

            System.out.println("DEBUG: DTO categories: " + dto.getCategories());

            transactionService.createTransaction(recurring.getUser().getId(), dto);

            System.out.println("Created: " + recurring.getDescription() + " for " + date);

        } catch (Exception e) {
            System.err.println("Failed to create recurring transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }
}