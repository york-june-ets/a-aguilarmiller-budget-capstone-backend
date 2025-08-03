package york.fse.budgetappbackend.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import york.fse.budgetappbackend.dto.PaginatedTransactionResponseDTO;
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
        TransactionResponseDTO response = transactionService.createTransaction(userId, dto);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> updateTransaction(
            @PathVariable Long id,
            @RequestBody TransactionRequestDTO dto
    ) {
        TransactionResponseDTO response = transactionService.updateTransaction(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PaginatedTransactionResponseDTO> getTransactionsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) List<String> categories
    ) {
        Page<TransactionResponseDTO> transactionPage =
                transactionService.getTransactionsByUserWithFilters(
                        userId, page, size, startDate, endDate, accountId, categories
                );

        PaginatedTransactionResponseDTO response = new PaginatedTransactionResponseDTO(
                transactionPage.getContent(),
                transactionPage.getNumber(),
                transactionPage.getTotalPages(),
                transactionPage.getTotalElements(),
                transactionPage.getSize()
        );

        return ResponseEntity.ok(response);
    }
}