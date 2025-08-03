package york.fse.budgetappbackend.service;

import org.springframework.stereotype.Service;
import york.fse.budgetappbackend.dto.MonthlySpendingComparisonDTO;
import york.fse.budgetappbackend.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class SpendingServiceImpl implements SpendingService {

    private final TransactionRepository transactionRepository;

    public SpendingServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public MonthlySpendingComparisonDTO getMonthlySpendingComparison(Long userId, Integer currentMonth, Integer currentYear) {
        LocalDate now = LocalDate.now();

        int month = currentMonth != null ? currentMonth : now.getMonthValue();
        int year = currentYear != null ? currentYear : now.getYear();

        LocalDate previousMonthDate = LocalDate.of(year, month, 1).minusMonths(1);
        int previousMonth = previousMonthDate.getMonthValue();
        int previousYear = previousMonthDate.getYear();

        BigDecimal currentMonthSpending = transactionRepository.getTotalExpensesByUserAndMonth(userId, month, year);
        BigDecimal previousMonthSpending = transactionRepository.getTotalExpensesByUserAndMonth(userId, previousMonth, previousYear);

        return new MonthlySpendingComparisonDTO(currentMonthSpending, previousMonthSpending);
    }
}