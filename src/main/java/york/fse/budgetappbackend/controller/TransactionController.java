package york.fse.budgetappbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import york.fse.budgetappbackend.dto.TransactionRequestDTO;
import york.fse.budgetappbackend.dto.TransactionResponseDTO;
import york.fse.budgetappbackend.model.Budget;
import york.fse.budgetappbackend.repository.BudgetRepository;
import york.fse.budgetappbackend.service.TransactionService;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final BudgetRepository budgetRepository;

    public TransactionController(
            TransactionService transactionService,
            BudgetRepository budgetRepository
    ) {
        this.transactionService = transactionService;
        this.budgetRepository = budgetRepository;
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<TransactionResponseDTO> createTransaction(
            @PathVariable Long userId,
            @RequestBody TransactionRequestDTO dto
    ) {
        List<String> incomingCategories = dto.getCategories();
        List<Budget> userBudgets = budgetRepository.findByUserIdAndEnabledTrue(userId);

        boolean hasMatch = userBudgets.stream()
                .flatMap(b -> b.getCategories().stream())
                .anyMatch(incomingCategories::contains);

        if (!hasMatch) {
            throw new IllegalArgumentException("At least one category must match an active budget.");
        }

        return ResponseEntity.ok(transactionService.createTransaction(userId, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> updateTransaction(
            @PathVariable Long id,
            @RequestBody TransactionRequestDTO dto
    ) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransactionsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(transactionService.getAllTransactionsByUser(userId));
    }
}
