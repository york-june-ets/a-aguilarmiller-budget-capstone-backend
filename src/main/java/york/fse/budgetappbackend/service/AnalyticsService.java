package york.fse.budgetappbackend.service;

import york.fse.budgetappbackend.dto.CategorySpendingDTO;
import york.fse.budgetappbackend.dto.MonthlySpendingDTO;

import java.util.List;

public interface AnalyticsService {
    List<CategorySpendingDTO> getTopSpendingCategories(Long userId, int limit);
    List<MonthlySpendingDTO> getMonthlySpending(Long userId);
}