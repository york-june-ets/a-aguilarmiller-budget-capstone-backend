package york.fse.budgetappbackend.service;

import york.fse.budgetappbackend.dto.MonthlySpendingComparisonDTO;

public interface SpendingService {
    MonthlySpendingComparisonDTO getMonthlySpendingComparison(Long userId, Integer currentMonth, Integer currentYear);
}