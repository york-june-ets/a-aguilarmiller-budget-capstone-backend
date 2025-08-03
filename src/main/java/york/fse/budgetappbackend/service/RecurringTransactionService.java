package york.fse.budgetappbackend.service;

import york.fse.budgetappbackend.dto.RecurringTransactionRequestDTO;
import york.fse.budgetappbackend.dto.RecurringTransactionResponseDTO;

import java.util.List;

public interface RecurringTransactionService {

    RecurringTransactionResponseDTO createRecurringTransaction(Long userId, RecurringTransactionRequestDTO dto);

    List<RecurringTransactionResponseDTO> getRecurringTransactionsByUser(Long userId);

    void deleteRecurringTransaction(Long id);

    RecurringTransactionResponseDTO updateRecurringTransaction(Long id, RecurringTransactionRequestDTO dto);

}