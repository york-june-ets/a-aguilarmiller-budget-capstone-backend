package york.fse.budgetappbackend.service;

import org.springframework.stereotype.Service;
import york.fse.budgetappbackend.dto.BudgetRequestDTO;
import york.fse.budgetappbackend.dto.BudgetResponseDTO;
import york.fse.budgetappbackend.model.Budget;
import york.fse.budgetappbackend.model.User;
import york.fse.budgetappbackend.repository.BudgetRepository;
import york.fse.budgetappbackend.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;

    public BudgetServiceImpl(BudgetRepository budgetRepository, UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
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

        Budget saved = budgetRepository.save(budget);
        return mapToDTO(saved);
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

        Budget updated = budgetRepository.save(budget);
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
        dto.setActualSpend(budget.getActualSpend());
        return dto;
    }
}
