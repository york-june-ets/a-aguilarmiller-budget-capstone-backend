package york.fse.budgetappbackend.service;

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

        // Normalize frequency to lowercase to match database constraint
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
    public void deleteRecurringTransaction(Long id) {
        recurringTransactionRepository.deleteById(id);
    }

    private LocalDate calculateNextDate(LocalDate start, String frequency) {
        return switch (frequency.toLowerCase()) {
            case "daily" -> start.plusDays(1);
            case "weekly" -> start.plusWeeks(1);
            case "monthly" -> start.plusMonths(1);
            case "quarterly" -> start.plusMonths(3);
            case "yearly" -> start.plusYears(1);
            default -> throw new IllegalArgumentException("Invalid frequency: " + frequency);
        };
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
