package york.fse.budgetappbackend.service;

import york.fse.budgetappbackend.dto.BudgetRequestDTO;
import york.fse.budgetappbackend.dto.BudgetResponseDTO;

import java.util.List;

public interface BudgetService {
    BudgetResponseDTO createBudget(Long userId, BudgetRequestDTO dto);
    List<BudgetResponseDTO> getBudgetsByUser(Long userId);
    BudgetResponseDTO updateBudget(Long budgetId, BudgetRequestDTO dto);
    void deleteBudget(Long budgetId);
}
