package york.fse.budgetappbackend.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import york.fse.budgetappbackend.dto.TransactionRequestDTO;
import york.fse.budgetappbackend.dto.TransactionResponseDTO;
import york.fse.budgetappbackend.model.*;
import york.fse.budgetappbackend.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageImpl;

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

    private void validateExpenseCategories(List<String> categories, Long userId) {
        if (categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException("Expense transactions require at least one category.");
        }

        List<Budget> activeBudgets = budgetRepository.findByUserIdAndEnabledTrue(userId);
        boolean hasMatchingBudget = activeBudgets.stream()
                .anyMatch(budget -> budget.getCategories().stream()
                        .anyMatch(categories::contains));

        if (!hasMatchingBudget) {
            throw new IllegalArgumentException("Expense categories must be tied to an active budget.");
        }
    }

    private void updateAccountBalance(Account account, TransactionType type, BigDecimal amount, boolean isReversal) {
        BigDecimal adjustedAmount = isReversal ? amount.negate() : amount;

        switch (type) {
            case INCOME, TRANSFER_IN -> account.setBalance(account.getBalance().add(adjustedAmount));
            case EXPENSE, TRANSFER_OUT -> account.setBalance(account.getBalance().subtract(adjustedAmount));
        }
        accountRepository.save(account);
    }

    private void updateBudgetSpend(Long userId, List<String> categories, BigDecimal amount,
                                   Long selectedBudgetId, boolean isReversal, User user, LocalDate transactionDate) {
        BigDecimal adjustedAmount = isReversal ? amount.negate() : amount;

        if (selectedBudgetId != null) {
            Budget selectedBudget = budgetRepository.findById(selectedBudgetId)
                    .orElseThrow(() -> new IllegalArgumentException("Selected budget not found"));

            boolean categoryMatches = selectedBudget.getCategories().stream()
                    .anyMatch(categories::contains);
            if (!categoryMatches) {
                throw new IllegalArgumentException("Selected budget does not match transaction categories.");
            }

            if (isTransactionInBudgetPeriod(transactionDate, selectedBudget.getTimeframe())) {
                BigDecimal previousSpend = selectedBudget.getActualSpend();
                BigDecimal newSpend = previousSpend.add(adjustedAmount);
                selectedBudget.setActualSpend(newSpend);
                budgetRepository.save(selectedBudget);

                if (!isReversal && user != null) {
                    maybeSendThresholdNotification(selectedBudget, previousSpend, newSpend, user);
                }
            }
        } else {
            List<Budget> userBudgets = budgetRepository.findByUserIdAndEnabledTrue(userId);
            Optional<Budget> firstMatch = userBudgets.stream()
                    .filter(budget -> budget.getCategories().stream().anyMatch(categories::contains))
                    .filter(budget -> isTransactionInBudgetPeriod(transactionDate, budget.getTimeframe()))
                    .findFirst();

            if (firstMatch.isPresent()) {
                Budget budget = firstMatch.get();
                BigDecimal previousSpend = budget.getActualSpend();
                BigDecimal newSpend = previousSpend.add(adjustedAmount);
                budget.setActualSpend(newSpend);
                budgetRepository.save(budget);

                if (!isReversal && user != null) {
                    maybeSendThresholdNotification(budget, previousSpend, newSpend, user);
                }
            }
        }
    }

    private boolean isTransactionInBudgetPeriod(LocalDate transactionDate, String timeframe) {
        LocalDate now = LocalDate.now();

        switch (timeframe.toLowerCase()) {
            case "monthly":
                YearMonth transactionMonth = YearMonth.from(transactionDate);
                YearMonth currentMonth = YearMonth.from(now);
                return transactionMonth.equals(currentMonth);

            case "quarterly":
                int transactionQuarter = (transactionDate.getMonthValue() - 1) / 3 + 1;
                int currentQuarter = (now.getMonthValue() - 1) / 3 + 1;
                return transactionDate.getYear() == now.getYear() &&
                        transactionQuarter == currentQuarter;

            case "yearly":
                return transactionDate.getYear() == now.getYear();

            default:
                return false;
        }
    }

    private Transaction createTransferPair(Transaction originalTransaction, Long transferAccountId,
                                           TransactionType pairType, String description) {
        Account transferAccount = accountRepository.findById(transferAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer account not found"));

        Transaction transferPair = new Transaction();
        transferPair.setUser(originalTransaction.getUser());
        transferPair.setAmount(originalTransaction.getAmount());

        String transferDescription;
        if (pairType == TransactionType.TRANSFER_IN) {
            transferDescription = "Transfer from " + originalTransaction.getAccount().getName();
        } else {
            transferDescription = "Transfer to " + transferAccount.getName();
        }

        transferPair.setDescription(transferDescription);
        transferPair.setDate(originalTransaction.getDate());
        transferPair.setType(pairType);
        transferPair.setAccount(transferAccount);
        transferPair.setCategories(Arrays.asList("Transfer"));

        updateAccountBalance(transferAccount, pairType, originalTransaction.getAmount(), false);

        return transactionRepository.save(transferPair);
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

    @Override
    @Transactional
    public TransactionResponseDTO createTransaction(Long userId, TransactionRequestDTO dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        TransactionType type = TransactionType.valueOf(dto.getType().toUpperCase());
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(dto.getAmount());
        transaction.setDate(dto.getDate());
        transaction.setType(type);
        transaction.setCategories(dto.getCategories() != null ? dto.getCategories() : new ArrayList<>());
        System.out.println("DEBUG: Transaction before save - categories: " + transaction.getCategories());
        if (type == TransactionType.TRANSFER_OUT || type == TransactionType.TRANSFER_IN) {
            String transferDescription;

            if (dto.getTransferAccountId() != null) {
                Account targetAccount = accountRepository.findById(dto.getTransferAccountId())
                        .orElseThrow(() -> new IllegalArgumentException("Transfer target account not found"));

                if (type == TransactionType.TRANSFER_OUT) {
                    transferDescription = "Transfer to " + targetAccount.getName();
                } else {
                    transferDescription = "Transfer from " + targetAccount.getName();
                }
            } else {
                if (type == TransactionType.TRANSFER_OUT) {
                    transferDescription = "Transfer to external";
                } else {
                    transferDescription = "Transfer from external";
                }
            }

            transaction.setDescription(transferDescription);
        } else {
            transaction.setDescription(dto.getDescription());
        }

        if (dto.getAccountId() != null) {
            Account account = accountRepository.findById(dto.getAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));
            transaction.setAccount(account);
            updateAccountBalance(account, type, dto.getAmount(), false);
        }

        Transaction saved = transactionRepository.save(transaction);
        System.out.println("DEBUG: Transaction after save - categories: " + saved.getCategories());
        if (type == TransactionType.EXPENSE) {
            updateBudgetSpend(userId, dto.getCategories(), dto.getAmount(),
                    dto.getSelectedBudgetId(), false, user, dto.getDate());
        }

        if ((type == TransactionType.TRANSFER_OUT || type == TransactionType.TRANSFER_IN)
                && dto.getTransferAccountId() != null) {
            TransactionType pairType = (type == TransactionType.TRANSFER_OUT)
                    ? TransactionType.TRANSFER_IN
                    : TransactionType.TRANSFER_OUT;
            createTransferPair(saved, dto.getTransferAccountId(), pairType, dto.getDescription());
        }

        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional
    public TransactionResponseDTO updateTransaction(Long id, TransactionRequestDTO dto) {
        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        TransactionType oldType = existing.getType();
        TransactionType newType = TransactionType.valueOf(dto.getType().toUpperCase());
        BigDecimal oldAmount = existing.getAmount();
        BigDecimal newAmount = dto.getAmount();
        Long userId = existing.getUser().getId();

        if (existing.getAccount() != null) {
            updateAccountBalance(existing.getAccount(), oldType, oldAmount, true);
        }

        if (oldType == TransactionType.EXPENSE) {
            updateBudgetSpend(userId, existing.getCategories(), oldAmount,
                    dto.getSelectedBudgetId(), true, null, existing.getDate());
        }

        existing.setAmount(newAmount);
        existing.setType(newType);
        existing.setDate(dto.getDate());
        existing.setCategories(dto.getCategories() != null ? dto.getCategories() : new ArrayList<>());

        if (newType == TransactionType.TRANSFER_OUT || newType == TransactionType.TRANSFER_IN) {
            String transferDescription;

            if (dto.getTransferAccountId() != null) {
                Account targetAccount = accountRepository.findById(dto.getTransferAccountId())
                        .orElseThrow(() -> new IllegalArgumentException("Transfer target account not found"));

                if (newType == TransactionType.TRANSFER_OUT) {
                    transferDescription = "Transfer to " + targetAccount.getName();
                } else {
                    transferDescription = "Transfer from " + targetAccount.getName();
                }
            } else {
                if (newType == TransactionType.TRANSFER_OUT) {
                    transferDescription = "Transfer to external";
                } else {
                    transferDescription = "Transfer from external";
                }
            }

            existing.setDescription(transferDescription);
        } else {
            existing.setDescription(dto.getDescription());
        }

        Long existingAccountId = existing.getAccount() != null ? existing.getAccount().getId() : null;
        if (!Objects.equals(existingAccountId, dto.getAccountId())) {
            if (dto.getAccountId() != null) {
                Account newAccount = accountRepository.findById(dto.getAccountId())
                        .orElseThrow(() -> new IllegalArgumentException("Account not found"));
                existing.setAccount(newAccount);
            } else {
                existing.setAccount(null);
            }
        }

        Transaction updated = transactionRepository.save(existing);

        if (updated.getAccount() != null) {
            updateAccountBalance(updated.getAccount(), newType, newAmount, false);
        }

        if (newType == TransactionType.EXPENSE) {
            updateBudgetSpend(userId, dto.getCategories(), newAmount,
                    dto.getSelectedBudgetId(), false, existing.getUser(), dto.getDate());
        }

        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (transaction.getAccount() != null) {
            updateAccountBalance(transaction.getAccount(), transaction.getType(),
                    transaction.getAmount(), true);
        }

        if (transaction.getType() == TransactionType.EXPENSE) {
            updateBudgetSpend(transaction.getUser().getId(), transaction.getCategories(),
                    transaction.getAmount(), null, true, null, transaction.getDate());
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

    @Override
    public Page<TransactionResponseDTO> getTransactionsByUserPaginated(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending().and(Sort.by("id").descending()));
        Page<Transaction> transactionPage = transactionRepository.findByUserId(userId, pageable);

        List<TransactionResponseDTO> dtoList = transactionPage.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, transactionPage.getTotalElements());
    }

    @Override
    public Page<TransactionResponseDTO> getTransactionsByUserWithFilters(
            Long userId, int page, int size,
            String startDate, String endDate,
            Long accountId, List<String> categories) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("date").descending().and(Sort.by("id").descending()));

        Page<Transaction> transactionPage;

        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

        boolean hasCategories = categories != null && !categories.isEmpty();
        boolean hasStartDate = start != null;
        boolean hasEndDate = end != null;
        boolean hasAccount = accountId != null;

        if (!hasStartDate && !hasEndDate && !hasAccount && !hasCategories) {
            transactionPage = transactionRepository.findByUserId(userId, pageable);
        }

        else if (hasCategories) {
            if (hasStartDate && hasEndDate && hasAccount) {
                transactionPage = transactionRepository.findByUserIdWithAllFiltersAndCategories(
                        userId, start, end, accountId, categories, pageable);
            } else if (hasStartDate && hasEndDate) {
                transactionPage = transactionRepository.findByUserIdWithDateRangeAndCategories(
                        userId, start, end, categories, pageable);
            } else if (hasStartDate && hasAccount) {
                transactionPage = transactionRepository.findByUserIdWithStartDateAccountAndCategories(
                        userId, start, accountId, categories, pageable);
            } else if (hasEndDate && hasAccount) {
                transactionPage = transactionRepository.findByUserIdWithEndDateAccountAndCategories(
                        userId, end, accountId, categories, pageable);
            } else if (hasStartDate) {
                transactionPage = transactionRepository.findByUserIdWithStartDateAndCategories(
                        userId, start, categories, pageable);
            } else if (hasEndDate) {
                transactionPage = transactionRepository.findByUserIdWithEndDateAndCategories(
                        userId, end, categories, pageable);
            } else if (hasAccount) {
                transactionPage = transactionRepository.findByUserIdWithAccountAndCategories(
                        userId, accountId, categories, pageable);
            } else {
                transactionPage = transactionRepository.findByUserIdWithCategories(
                        userId, categories, pageable);
            }
        }

        else {
            if (hasStartDate && hasEndDate && hasAccount) {
                transactionPage = transactionRepository.findByUserIdWithAllFilters(
                        userId, start, end, accountId, pageable);
            } else if (hasStartDate && hasEndDate) {
                transactionPage = transactionRepository.findByUserIdWithDateRange(
                        userId, start, end, pageable);
            } else if (hasStartDate && hasAccount) {
                transactionPage = transactionRepository.findByUserIdWithStartDateAndAccount(
                        userId, start, accountId, pageable);
            } else if (hasEndDate && hasAccount) {
                transactionPage = transactionRepository.findByUserIdWithEndDateAndAccount(
                        userId, end, accountId, pageable);
            } else if (hasStartDate) {
                transactionPage = transactionRepository.findByUserIdWithStartDate(
                        userId, start, pageable);
            } else if (hasEndDate) {
                transactionPage = transactionRepository.findByUserIdWithEndDate(
                        userId, end, pageable);
            } else if (hasAccount) {
                transactionPage = transactionRepository.findByUserIdAndAccountId(
                        userId, accountId, pageable);
            } else {
                transactionPage = transactionRepository.findByUserId(userId, pageable);
            }
        }

        List<TransactionResponseDTO> dtoList = transactionPage.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, transactionPage.getTotalElements());
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
        response.setAccountId(t.getAccount() != null ? t.getAccount().getId() : null);
        return response;
    }

    @Override
    public List<String> getAllCategoriesByUser(Long userId) {
        return transactionRepository.findAllCategoriesByUserId(userId);
    }
}