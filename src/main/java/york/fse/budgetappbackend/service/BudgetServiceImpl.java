package york.fse.budgetappbackend.service;

import org.springframework.stereotype.Service;
import york.fse.budgetappbackend.dto.BudgetRequestDTO;
import york.fse.budgetappbackend.dto.BudgetResponseDTO;
import york.fse.budgetappbackend.model.Budget;
import york.fse.budgetappbackend.model.Transaction;
import york.fse.budgetappbackend.model.TransactionType;
import york.fse.budgetappbackend.model.User;
import york.fse.budgetappbackend.repository.BudgetRepository;
import york.fse.budgetappbackend.repository.TransactionRepository;
import york.fse.budgetappbackend.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public BudgetServiceImpl(BudgetRepository budgetRepository,
                             UserRepository userRepository,
                             TransactionRepository transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public BudgetResponseDTO createBudget(Long userId, BudgetRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check for duplicates
        for (String category : dto.getCategories()) {
            boolean exists = budgetRepository.existsByUserIdAndCategoryAndTimeframe(userId, category, dto.getTimeframe());
            if (exists) {
                throw new IllegalArgumentException("You already have a budget for category '" + category + "' in this timeframe.");
            }
        }

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setAmount(dto.getAmount());
        budget.setTimeframe(dto.getTimeframe());
        budget.setCategories(dto.getCategories());
        budget.setEnabled(dto.isEnabled());

        System.out.println("Incoming enabled flag: " + dto.isEnabled());

        Budget saved = budgetRepository.save(budget);

        linkExistingTransactionsToNewBudget(saved);

        return mapToDTO(saved);
    }

    private void linkExistingTransactionsToNewBudget(Budget budget) {
        if (!budget.isEnabled()) {
            return;
        }

        LocalDate[] dateRange = calculateBudgetDateRange(budget.getTimeframe());
        LocalDate startDate = dateRange[0];
        LocalDate endDate = dateRange[1];

        List<Transaction> matchingTransactions = findMatchingTransactions(
                budget.getUser().getId(),
                budget.getCategories(),
                startDate,
                endDate
        );

        BigDecimal totalSpending = matchingTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        budget.setActualSpend(totalSpending);
        budgetRepository.save(budget);

        System.out.println("Linked " + matchingTransactions.size() +
                " existing transactions totaling $" + totalSpending +
                " to new " + budget.getTimeframe() + " budget for categories: " +
                budget.getCategories());
    }

    private LocalDate[] calculateBudgetDateRange(String timeframe) {
        LocalDate now = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;

        switch (timeframe.toLowerCase()) {
            case "monthly":
                YearMonth currentMonth = YearMonth.from(now);
                startDate = currentMonth.atDay(1);
                endDate = currentMonth.atEndOfMonth();
                break;
            case "quarterly":
                int currentQuarter = (now.getMonthValue() - 1) / 3 + 1;
                int quarterStartMonth = (currentQuarter - 1) * 3 + 1;
                startDate = LocalDate.of(now.getYear(), quarterStartMonth, 1);
                endDate = startDate.plusMonths(3).minusDays(1);
                break;
            case "yearly":
                startDate = LocalDate.of(now.getYear(), 1, 1);
                endDate = LocalDate.of(now.getYear(), 12, 31);
                break;
            default:
                throw new IllegalArgumentException("Unsupported timeframe: " + timeframe);
        }

        return new LocalDate[]{startDate, endDate};
    }

    private List<Transaction> findMatchingTransactions(Long userId, List<String> categories,
                                                       LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findExpenseTransactionsByUserAndCategoriesInDateRange(
                userId, categories, startDate, endDate
        );
    }

    @Override
    public List<BudgetResponseDTO> getBudgetsByUser(Long userId) {
        return budgetRepository.findAllByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BudgetResponseDTO updateBudget(Long budgetId, BudgetRequestDTO dto) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found"));

        budget.setAmount(dto.getAmount());
        budget.setTimeframe(dto.getTimeframe());
        budget.setCategories(dto.getCategories());
        budget.setEnabled(dto.isEnabled());

        Budget updated = budgetRepository.save(budget);

        if (dto.isEnabled()) {
            linkExistingTransactionsToNewBudget(updated);
        } else {
            updated.setActualSpend(BigDecimal.ZERO);
            budgetRepository.save(updated);
        }

        return mapToDTO(updated);
    }

    @Override
    public void deleteBudget(Long budgetId) {
        if (!budgetRepository.existsById(budgetId)) {
            throw new IllegalArgumentException("Budget not found");
        }
        budgetRepository.deleteById(budgetId);
    }

    private BudgetResponseDTO mapToDTO(Budget budget) {
        BudgetResponseDTO dto = new BudgetResponseDTO();
        dto.setId(budget.getId());
        dto.setAmount(budget.getAmount());
        dto.setTimeframe(budget.getTimeframe());
        dto.setCategories(budget.getCategories());
        dto.setActualSpend(budget.getActualSpend());
        dto.setEnabled(budget.isEnabled());
        return dto;
    }
}