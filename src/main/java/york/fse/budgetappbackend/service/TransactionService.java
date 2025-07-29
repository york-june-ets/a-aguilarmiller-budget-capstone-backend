package york.fse.budgetappbackend.service;

import york.fse.budgetappbackend.dto.TransactionRequestDTO;
import york.fse.budgetappbackend.dto.TransactionResponseDTO;

import java.util.List;

public interface TransactionService {
    TransactionResponseDTO createTransaction(Long userId, TransactionRequestDTO dto);
    TransactionResponseDTO updateTransaction(Long id, TransactionRequestDTO dto);
    void deleteTransaction(Long id);
    List<TransactionResponseDTO> getAllTransactionsByUser(Long userId);
}
