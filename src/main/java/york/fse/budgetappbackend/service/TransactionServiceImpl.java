package york.fse.budgetappbackend.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import york.fse.budgetappbackend.dto.TransactionRequestDTO;
import york.fse.budgetappbackend.dto.TransactionResponseDTO;
import york.fse.budgetappbackend.model.*;
import york.fse.budgetappbackend.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final NotificationRepository notificationRepository;

    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            UserRepository userRepository,
            BudgetRepository budgetRepository,
            NotificationRepository notificationRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.budgetRepository = budgetRepository;
        this.notificationRepository = notificationRepository;
    }

    private void maybeSendThresholdNotification(Budget budget, BigDecimal previousSpend, BigDecimal newSpend, User user) {
        BigDecimal threshold75 = budget.getAmount().multiply(BigDecimal.valueOf(0.75));
        BigDecimal threshold100 = budget.getAmount();

        boolean crossed75 = previousSpend.compareTo(threshold75) < 0
                && newSpend.compareTo(threshold75) >= 0;

        boolean crossed100 = previousSpend.compareTo(threshold100) < 0
                && newSpend.compareTo(threshold100) >= 0;

        if (crossed75 || crossed100) {
            String categoryLabel = budget.getCategories().isEmpty()
                    ? "a category"
                    : budget.getCategories().get(0);

            String message = String.format("You have reached %s of your %s budget.",
                    crossed100 ? "100%" : "75%",
                    categoryLabel
            );

            Notification notification = new Notification();
            notification.setUser(user);
            notification.setMessage(message);
            notification.setTimestamp(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    private void updateActualSpendForBudgets(Collection<Budget> budgets, BigDecimal delta) {
       for (Budget budget : budgets) {
           budget.setActualSpend(budget.getActualSpend().add(delta));
           budgetRepository.save(budget);
       }
    }

    @Override
    @Transactional
    public TransactionResponseDTO createTransaction(Long userId, TransactionRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<String> categories = dto.getCategories();
        if (categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException("At least one category is required.");
        }

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(dto.getAmount());
        transaction.setDescription(dto.getDescription());
        transaction.setDate(dto.getDate());
        TransactionType type = TransactionType.valueOf(dto.getType().toUpperCase());
        transaction.setType(type);
        transaction.setCategories(categories);

        Account account = null;
        if (dto.getAccountId() != null) {
            account = accountRepository.findById(dto.getAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));
            transaction.setAccount(account);

            switch (type) {
                case INCOME -> account.setBalance(account.getBalance().add(dto.getAmount()));
                case EXPENSE, TRANSFER_OUT -> account.setBalance(account.getBalance().subtract(dto.getAmount()));
                case TRANSFER_IN -> account.setBalance(account.getBalance().add(dto.getAmount()));
            }

            accountRepository.save(account);
        }

        Transaction saved = transactionRepository.save(transaction);

        Budget selectedBudget = null;
        if (dto.getSelectedBudgetId() != null) {
            selectedBudget = budgetRepository.findById(dto.getSelectedBudgetId())
                    .orElseThrow(() -> new IllegalArgumentException("Budget not found"));

            List<String> selectedBudgetCategories = selectedBudget.getCategories();
            boolean matches = categories.stream()
                    .anyMatch(cat -> selectedBudgetCategories.contains(cat));
            if (!matches) {
                throw new IllegalArgumentException("Selected budget does not match any transaction categories.");
            }

            BigDecimal previousSpend = selectedBudget.getActualSpend();
            BigDecimal newSpend = previousSpend.add(dto.getAmount());

            selectedBudget.setActualSpend(newSpend);
            budgetRepository.save(selectedBudget);

            maybeSendThresholdNotification(selectedBudget, previousSpend, newSpend, user);
        } else {
            List<Budget> matchingBudgets = budgetRepository.findByUserIdAndEnabledTrue(userId).stream().filter(b -> b.getCategories().stream().anyMatch(categories::contains)).toList();
      updateActualSpendForBudgets(matchingBudgets, dto.getAmount());
        }

        return mapToResponseDTO(saved);
    }

    @Override
    public TransactionResponseDTO updateTransaction(Long id, TransactionRequestDTO dto) {
        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        BigDecimal oldAmount = existing.getAmount();
        Long userId = existing.getUser().getId();
        Set<String> newCategories = new HashSet<>(dto.getCategories());
        BigDecimal newAmount = dto.getAmount();

        List<Budget> userBudgets = budgetRepository.findByUserIdAndEnabledTrue(userId);

        Long oldSelectedBudgetId = dto.getSelectedBudgetId();
        if (oldSelectedBudgetId != null) {
            Budget oldBudget = budgetRepository.findById(oldSelectedBudgetId)
                    .orElseThrow(() -> new IllegalArgumentException("Selected budget not found"));
            oldBudget.setActualSpend(oldBudget.getActualSpend().subtract(oldAmount));
            budgetRepository.save(oldBudget);
        }

        existing.setAmount(newAmount);
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

            budget.setActualSpend(budget.getActualSpend().add(newAmount));
            budgetRepository.save(budget);
        } else {
            Optional<Budget> firstMatch = userBudgets.stream()
                    .filter(b -> b.getCategories().stream().anyMatch(newCategories::contains))
                    .findFirst();
            firstMatch.ifPresent(budget -> {
                budget.setActualSpend(budget.getActualSpend().add(newAmount));
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
        TransactionResponseDTO response = new TransactionResponseDTO();
        response.setId(t.getId());
        response.setDescription(t.getDescription());
        response.setAmount(t.getAmount());
        response.setDate(t.getDate());
        response.setType(t.getType().toString());
        response.setCategories(t.getCategories());
        response.setAccountName(t.getAccount() != null ? t.getAccount().getName() : "Deleted Account");
        return response;
    }
}
