package york.fse.budgetappbackend.service;

import org.springframework.stereotype.Service;
import york.fse.budgetappbackend.dto.TransactionRequestDTO;
import york.fse.budgetappbackend.dto.TransactionResponseDTO;
import york.fse.budgetappbackend.model.*;
import york.fse.budgetappbackend.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;

    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            UserRepository userRepository,
            BudgetRepository budgetRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.budgetRepository = budgetRepository;
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

        if ((type == TransactionType.TRANSFER_IN || type == TransactionType.TRANSFER_OUT)
                && (dto.getDescription() == null || dto.getDescription().isBlank())) {

            String targetName = "external account";
            if (dto.getTransferTargetAccountId() != null) {
                Optional<Account> target = accountRepository.findById(dto.getTransferTargetAccountId());
                targetName = target.map(Account::getName).orElse(targetName);
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

        List<Budget> activeBudgets = budgetRepository.findByUserIdAndEnabledTrue(userId);
        Set<String> txCategories = new HashSet<>(dto.getCategories());

        Long selectedBudgetId = dto.getSelectedBudgetId();
        if (selectedBudgetId != null) {
            Budget selectedBudget = budgetRepository.findById(selectedBudgetId)
                    .orElseThrow(() -> new IllegalArgumentException("Selected budget not found"));

            boolean categoryMatches = selectedBudget.getCategories().stream()
                    .anyMatch(txCategories::contains);

            if (!categoryMatches) {
                throw new IllegalArgumentException("Selected budget does not match transaction categories.");
            }

            selectedBudget.setActualSpend(selectedBudget.getActualSpend().add(amount));
            budgetRepository.save(selectedBudget);
        } else {
            Optional<Budget> firstMatch = activeBudgets.stream()
                    .filter(b -> b.getCategories().stream().anyMatch(txCategories::contains))
                    .findFirst();
            System.out.println("➡️ Transaction Categories: " + txCategories);
            System.out.println("📋 Checking active budgets...");

            for (Budget b : activeBudgets) {
                System.out.println("🧾 Budget ID " + b.getId() + ", Categories: " + b.getCategories());
                boolean matched = b.getCategories().stream().anyMatch(txCategories::contains);
                System.out.println("   → Matched: " + matched);
            }

            firstMatch.ifPresent(budget -> {
                budget.setActualSpend(budget.getActualSpend().add(amount));
                budgetRepository.save(budget);
            });
            firstMatch.ifPresent(budget -> {
                budget.setActualSpend(budget.getActualSpend().add(amount));
                budgetRepository.save(budget);
            });
        }

        // MIRROR IF TRANSFER
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
        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        BigDecimal oldAmount = existing.getAmount();
        List<String> oldCategories = existing.getCategories();
        Long userId = existing.getUser().getId();
        Set<String> newCategories = new HashSet<>(dto.getCategories());

        List<Budget> userBudgets = budgetRepository.findByUserIdAndEnabledTrue(userId);

        Long oldSelectedBudgetId = dto.getSelectedBudgetId();

        if (oldSelectedBudgetId != null) {
            Budget oldBudget = budgetRepository.findById(oldSelectedBudgetId)
                    .orElseThrow(() -> new IllegalArgumentException("Selected budget not found"));

            oldBudget.setActualSpend(oldBudget.getActualSpend().subtract(oldAmount));
            budgetRepository.save(oldBudget);
        } else {
            Optional<Budget> firstMatch = userBudgets.stream()
                    .filter(b -> b.getCategories().stream().anyMatch(newCategories::contains))
                    .findFirst();

            firstMatch.ifPresent(budget -> {
                budget.setActualSpend(budget.getActualSpend().add(dto.getAmount()));
                budgetRepository.save(budget);
            });
        }
        existing.setAmount(dto.getAmount());
        existing.setDescription(dto.getDescription());
        existing.setType(TransactionType.valueOf(dto.getType().toUpperCase()));
        existing.setDate(dto.getDate());
        existing.setCategories(dto.getCategories());

        if (!existing.getAccount().getId().equals(dto.getAccountId())) {
            Account newAccount = accountRepository.findById(dto.getAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));
            existing.setAccount(newAccount);
        }

        Transaction updated = transactionRepository.save(existing);

        Long selectedBudgetId = dto.getSelectedBudgetId();

        if (selectedBudgetId != null) {
            Budget budget = budgetRepository.findById(selectedBudgetId)
                    .orElseThrow(() -> new IllegalArgumentException("Selected budget not found"));

            boolean categoryMatches = budget.getCategories().stream().anyMatch(newCategories::contains);
            if (!categoryMatches) {
                throw new IllegalArgumentException("Selected budget does not match transaction categories.");
            }

            budget.setActualSpend(budget.getActualSpend().add(dto.getAmount()));
            budgetRepository.save(budget);
        } else {
            Optional<Budget> firstMatch = userBudgets.stream()
                    .filter(b -> b.getCategories().stream().anyMatch(newCategories::contains))
                    .findFirst();

            firstMatch.ifPresent(budget -> {
                budget.setActualSpend(budget.getActualSpend().add(dto.getAmount()));
                budgetRepository.save(budget);
            });
        }

        return mapToResponseDTO(updated);
    }

    @Override
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        BigDecimal amount = transaction.getAmount();
        List<String> categories = transaction.getCategories();
        Long userId = transaction.getUser().getId();

        List<Budget> userBudgets = budgetRepository.findByUserIdAndEnabledTrue(userId);

        Optional<Budget> firstMatch = userBudgets.stream()
                .filter(b -> b.getCategories().stream().anyMatch(categories::contains))
                .findFirst();

        firstMatch.ifPresent(budget -> {
            budget.setActualSpend(budget.getActualSpend().subtract(amount));
            budgetRepository.save(budget);
        });

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
