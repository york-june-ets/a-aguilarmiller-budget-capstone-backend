package york.fse.budgetappbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import york.fse.budgetappbackend.dto.TransactionRequestDTO;
import york.fse.budgetappbackend.dto.TransactionResponseDTO;
import york.fse.budgetappbackend.service.TransactionService;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<TransactionResponseDTO> createTransaction(
            @PathVariable Long userId,
            @RequestBody TransactionRequestDTO dto
    ) {
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
