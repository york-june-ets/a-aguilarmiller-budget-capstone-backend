package york.fse.budgetappbackend.service;

import org.springframework.stereotype.Service;
import york.fse.budgetappbackend.dto.CategorySpendingDTO;
import york.fse.budgetappbackend.dto.MonthlySpendingDTO;
import york.fse.budgetappbackend.repository.TransactionRepository;

import java.util.List;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final TransactionRepository transactionRepository;

    public AnalyticsServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public List<CategorySpendingDTO> getTopSpendingCategories(Long userId, int limit) {
        return transactionRepository.findTopSpendingCategories(userId, limit);
    }

    @Override
    public List<MonthlySpendingDTO> getMonthlySpending(Long userId) {
        return transactionRepository.findMonthlySpending(userId);
    }

}