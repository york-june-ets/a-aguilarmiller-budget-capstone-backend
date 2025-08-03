package york.fse.budgetappbackend.service;

import org.springframework.data.domain.Page;
import york.fse.budgetappbackend.dto.TransactionRequestDTO;
import york.fse.budgetappbackend.dto.TransactionResponseDTO;

import java.util.List;

public interface TransactionService {
    TransactionResponseDTO createTransaction(Long userId, TransactionRequestDTO dto);
    TransactionResponseDTO updateTransaction(Long id, TransactionRequestDTO dto);
    void deleteTransaction(Long id);
    List<TransactionResponseDTO> getAllTransactionsByUser(Long userId);

    Page<TransactionResponseDTO> getTransactionsByUserWithFilters(
            Long userId, int page, int size,
            String startDate, String endDate,
            Long accountId, List<String> categories
    );
    Page<TransactionResponseDTO> getTransactionsByUserPaginated(Long userId, int page, int size);
}