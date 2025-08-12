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
        LocalDate nextDate = recurring.getNextDate();
        System.out.println("DEBUG: Processing " + recurring.getDescription() + ", nextDate: " + nextDate + ", today: " + today);

        if (nextDate.isEqual(today) || nextDate.isBefore(today)) {
            System.out.println("DEBUG: Transaction is due today or overdue, creating...");
            createTransactionFromRecurring(recurring, today);

            LocalDate newNextDate = calculateNextDate(today, recurring.getFrequency());
            recurring.setNextDate(newNextDate);
            recurringTransactionRepository.save(recurring);

            System.out.println("DEBUG: Created transaction for today, next due date: " + newNextDate);
        } else {
            System.out.println("DEBUG: Transaction not due yet (due: " + nextDate + "), skipping...");
        }
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
            dto.setDescription(recurring.getDescription() + " (Recurring Transaction)");
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