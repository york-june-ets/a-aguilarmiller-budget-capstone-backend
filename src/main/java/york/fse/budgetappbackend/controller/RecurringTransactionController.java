package york.fse.budgetappbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import york.fse.budgetappbackend.dto.RecurringTransactionRequestDTO;
import york.fse.budgetappbackend.dto.RecurringTransactionResponseDTO;
import york.fse.budgetappbackend.service.RecurringTransactionService;

import java.util.List;

@RestController
@RequestMapping("/api/recurring-transactions")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    public RecurringTransactionController(RecurringTransactionService recurringTransactionService) {
        this.recurringTransactionService = recurringTransactionService;
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<RecurringTransactionResponseDTO> createRecurringTransaction(
            @PathVariable Long userId,
            @RequestBody RecurringTransactionRequestDTO dto
    ) {
        RecurringTransactionResponseDTO created = recurringTransactionService.createRecurringTransaction(userId, dto);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RecurringTransactionResponseDTO>> getRecurringTransactionsByUser(
            @PathVariable Long userId
    ) {
        List<RecurringTransactionResponseDTO> recurringTransactions =
                recurringTransactionService.getRecurringTransactionsByUser(userId);
        return ResponseEntity.ok(recurringTransactions);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecurringTransaction(@PathVariable Long id) {
        recurringTransactionService.deleteRecurringTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
