package york.fse.budgetappbackend.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import york.fse.budgetappbackend.dto.RecurringTransactionRequestDTO;
import york.fse.budgetappbackend.dto.RecurringTransactionResponseDTO;
import york.fse.budgetappbackend.model.Account;
import york.fse.budgetappbackend.model.RecurringTransaction;
import york.fse.budgetappbackend.model.User;
import york.fse.budgetappbackend.repository.AccountRepository;
import york.fse.budgetappbackend.repository.RecurringTransactionRepository;
import york.fse.budgetappbackend.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecurringTransactionServiceImpl implements RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public RecurringTransactionServiceImpl(
            RecurringTransactionRepository recurringTransactionRepository,
            UserRepository userRepository,
            AccountRepository accountRepository
    ) {
        this.recurringTransactionRepository = recurringTransactionRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public RecurringTransactionResponseDTO createRecurringTransaction(Long userId, RecurringTransactionRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        RecurringTransaction recurringTransaction = new RecurringTransaction();
        recurringTransaction.setUser(user);
        recurringTransaction.setDescription(dto.getDescription());
        recurringTransaction.setAmount(dto.getAmount());
        recurringTransaction.setStartDate(dto.getStartDate());
        recurringTransaction.setNextDate(calculateNextDate(dto.getStartDate(), dto.getFrequency()));

        recurringTransaction.setLastGeneratedDate(null);
        recurringTransaction.setCategories(dto.getCategories());

        recurringTransaction.setFrequency(dto.getFrequency().toLowerCase());

        if (dto.getAccountId() != null) {
            Account account = accountRepository.findById(dto.getAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + dto.getAccountId()));
            recurringTransaction.setAccount(account);
        }

        recurringTransactionRepository.save(recurringTransaction);

        return mapToResponseDTO(recurringTransaction);
    }

    @Override
    public List<RecurringTransactionResponseDTO> getRecurringTransactionsByUser(Long userId) {
        return recurringTransactionRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteRecurringTransaction(Long id) {
        RecurringTransaction recurringTransaction = recurringTransactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recurring transaction not found with id: " + id));
        recurringTransactionRepository.deleteById(id);
    }

    @Override
    @Transactional
    public RecurringTransactionResponseDTO updateRecurringTransaction(Long id, RecurringTransactionRequestDTO dto) {
        RecurringTransaction existingTransaction = recurringTransactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recurring transaction not found with id: " + id));

        existingTransaction.setDescription(dto.getDescription());
        existingTransaction.setAmount(dto.getAmount());
        existingTransaction.setFrequency(dto.getFrequency().toLowerCase());
        existingTransaction.setStartDate(dto.getStartDate());
        existingTransaction.setCategories(dto.getCategories());

        if (dto.getAccountId() != null) {
            Account account = accountRepository.findById(dto.getAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + dto.getAccountId()));
            existingTransaction.setAccount(account);
        }

        existingTransaction.setNextDate(calculateNextDate(dto.getStartDate(), dto.getFrequency()));

        RecurringTransaction saved = recurringTransactionRepository.save(existingTransaction);
        return mapToResponseDTO(saved);
    }

    private LocalDate calculateNextDate(LocalDate start, String frequency) {
        LocalDate now = LocalDate.now();
        LocalDate nextDate = start;

        while (nextDate.isBefore(now) || nextDate.isEqual(now)) {
            switch (frequency.toLowerCase()) {
                case "daily":
                    nextDate = nextDate.plusDays(1);
                    break;
                case "weekly":
                    nextDate = nextDate.plusWeeks(1);
                    break;
                case "monthly":
                    nextDate = nextDate.plusMonths(1);
                    break;
                case "quarterly":
                    nextDate = nextDate.plusMonths(3);
                    break;
                case "yearly":
                    nextDate = nextDate.plusYears(1);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid frequency: " + frequency);
            }
        }

        return nextDate;
    }

    private RecurringTransactionResponseDTO mapToResponseDTO(RecurringTransaction tx) {
        return new RecurringTransactionResponseDTO(
                tx.getId(),
                tx.getUser().getId(),
                tx.getAccount() != null ? tx.getAccount().getId() : null,
                tx.getAccount() != null ? tx.getAccount().getName() : "Deleted Account",
                tx.getDescription(),
                tx.getAmount(),
                tx.getFrequency(),
                tx.getStartDate(),
                tx.getNextDate(),
                tx.getCategories(),
                tx.getLastGeneratedDate()
        );
    }
}