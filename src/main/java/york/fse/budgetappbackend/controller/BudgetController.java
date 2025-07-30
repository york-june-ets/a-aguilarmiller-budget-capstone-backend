package york.fse.budgetappbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import york.fse.budgetappbackend.dto.BudgetRequestDTO;
import york.fse.budgetappbackend.dto.BudgetResponseDTO;
import york.fse.budgetappbackend.service.BudgetService;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<BudgetResponseDTO> createBudget(
            @PathVariable Long userId,
            @RequestBody BudgetRequestDTO dto
    ) {
        BudgetResponseDTO created = budgetService.createBudget(userId, dto);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BudgetResponseDTO>> getBudgetsByUser(@PathVariable Long userId) {
        List<BudgetResponseDTO> budgets = budgetService.getBudgetsByUser(userId);
        return ResponseEntity.ok(budgets);
    }

    @PutMapping("/{budgetId}")
    public ResponseEntity<BudgetResponseDTO> updateBudget(
            @PathVariable Long budgetId,
            @RequestBody BudgetRequestDTO dto
    ) {
        BudgetResponseDTO updated = budgetService.updateBudget(budgetId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{budgetId}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long budgetId) {
        budgetService.deleteBudget(budgetId);
        return ResponseEntity.noContent().build();
    }
}
