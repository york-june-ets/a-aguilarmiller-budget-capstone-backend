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

import java.math.BigDecimal;
import java.time.LocalDate;
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
        transaction.setDate(dto.getDate());
        transaction.setType(TransactionType.valueOf(dto.getType().toUpperCase()));
        transaction.setCategories(dto.getCategories());

        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        transaction.setAccount(account);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        transaction.setUser(user);

        TransactionType type = transaction.getType();
        BigDecimal amount = dto.getAmount();
        LocalDate txDate = dto.getDate();
        LocalDate accountCreated = account.getCreatedAt() != null
                ? account.getCreatedAt().toLocalDate()
                : txDate;

        // Auto-generate transfer descriptions if empty
        if ((type == TransactionType.TRANSFER_IN || type == TransactionType.TRANSFER_OUT)
                && (dto.getDescription() == null || dto.getDescription().isBlank())) {

            String targetName = "external account";

            if (dto.getTransferTargetAccountId() != null) {
                Optional<Account> target = accountRepository.findById(dto.getTransferTargetAccountId());
                if (target.isPresent()) {
                    targetName = target.get().getName();
                }
            }

            if (type == TransactionType.TRANSFER_OUT) {
                transaction.setDescription("Transfer to " + targetName);
            } else {
                transaction.setDescription("Transfer from " + targetName);
            }
        } else {
            transaction.setDescription(dto.getDescription());
        }

        if (!txDate.isBefore(accountCreated)) {
            if (type == TransactionType.INCOME || type == TransactionType.TRANSFER_IN) {
                account.setBalance(account.getBalance().add(amount));
            } else if (type == TransactionType.EXPENSE || type == TransactionType.TRANSFER_OUT) {
                account.setBalance(account.getBalance().subtract(amount));
            }
            accountRepository.save(account);
        }

        Transaction saved = transactionRepository.save(transaction);

        // If internal transfer, create mirrored transaction
        if (dto.getTransferTargetAccountId() != null) {
            Account targetAccount = accountRepository.findById(dto.getTransferTargetAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Transfer target account not found"));

            Transaction mirrored = new Transaction();
            mirrored.setUser(user);
            mirrored.setAccount(targetAccount);
            mirrored.setDate(txDate);
            mirrored.setAmount(amount);
            mirrored.setCategories(dto.getCategories());

            if (type == TransactionType.TRANSFER_OUT) {
                mirrored.setType(TransactionType.TRANSFER_IN);
                mirrored.setDescription("Transfer from " + account.getName());
            } else {
                mirrored.setType(TransactionType.TRANSFER_OUT);
                mirrored.setDescription("Transfer to " + account.getName());
            }

            transactionRepository.save(mirrored);

            if (!txDate.isBefore(targetAccount.getCreatedAt().toLocalDate())) {
                if (mirrored.getType() == TransactionType.INCOME || mirrored.getType() == TransactionType.TRANSFER_IN) {
                    targetAccount.setBalance(targetAccount.getBalance().add(amount));
                } else {
                    targetAccount.setBalance(targetAccount.getBalance().subtract(amount));
                }
                accountRepository.save(targetAccount);
            }
        }

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
        List<Transaction> transactions = transactionRepository.findAllByUserIdOrderByDateDescIdDesc(userId);
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
        dto.setAccountId(t.getAccount() != null ? t.getAccount().getId() : null);
        dto.setCategories(t.getCategories() != null ? t.getCategories() : List.of());
        dto.setAccountName(t.getAccount() != null ? t.getAccount().getName() : "Deleted Account");
        return dto;
    }
}
