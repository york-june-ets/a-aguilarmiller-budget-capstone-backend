package york.fse.budgetappbackend.service;

import org.springframework.stereotype.Service;
import york.fse.budgetappbackend.dto.TransactionRequestDTO;
import york.fse.budgetappbackend.dto.TransactionResponseDTO;
import york.fse.budgetappbackend.model.Account;
import york.fse.budgetappbackend.model.Transaction;
import york.fse.budgetappbackend.model.TransactionType;
import york.fse.budgetappbackend.model.User;
import york.fse.budgetappbackend.repository.AccountRepository;
import york.fse.budgetappbackend.repository.TransactionRepository;
import york.fse.budgetappbackend.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            UserRepository userRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Override
    public TransactionResponseDTO createTransaction(Long userId, TransactionRequestDTO dto) {
        Transaction transaction = new Transaction();

        transaction.setAmount(dto.getAmount());
        transaction.setDescription(dto.getDescription());
        transaction.setType(TransactionType.valueOf(dto.getType().toUpperCase()));
        transaction.setDate(dto.getDate());
        transaction.setCategories(dto.getCategories());

        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        transaction.setAccount(account);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        transaction.setUser(user);

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponseDTO(saved);
    }

    @Override
    public TransactionResponseDTO updateTransaction(Long id, TransactionRequestDTO dto) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        transaction.setAmount(dto.getAmount());
        transaction.setDescription(dto.getDescription());
        transaction.setType(TransactionType.valueOf(dto.getType().toUpperCase()));
        transaction.setDate(dto.getDate());
        transaction.setCategories(dto.getCategories());

        if (!transaction.getAccount().getId().equals(dto.getAccountId())) {
            Account newAccount = accountRepository.findById(dto.getAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));
            transaction.setAccount(newAccount);
        }

        Transaction updated = transactionRepository.save(transaction);
        return mapToResponseDTO(updated);
    }

    @Override
    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new IllegalArgumentException("Transaction not found");
        }
        transactionRepository.deleteById(id);
    }

    @Override
    public List<TransactionResponseDTO> getAllTransactionsByUser(Long userId) {
        List<Transaction> transactions = transactionRepository.findAllByUserIdOrderByDateDesc(userId);
        return transactions.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private TransactionResponseDTO mapToResponseDTO(Transaction t) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setId(t.getId());
        dto.setAmount(t.getAmount());
        dto.setDescription(t.getDescription());
        dto.setType(t.getType().toString());
        dto.setDate(t.getDate());
        dto.setAccountId(t.getAccount().getId());
        dto.setCategories(t.getCategories());
        dto.setAccountName(t.getAccount().getName());
        return dto;
    }
}
