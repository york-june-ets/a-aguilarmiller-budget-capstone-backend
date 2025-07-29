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
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found with id " + userId);
        }

        Optional<Account> optionalAccount = accountRepository.findById(dto.getAccountId());
        if (optionalAccount.isEmpty()) {
            throw new IllegalArgumentException("Account not found with id " + dto.getAccountId());
        }

        RecurringTransaction transaction = new RecurringTransaction();
        transaction.setUser(optionalUser.get());
        transaction.setAccount(optionalAccount.get());
        transaction.setDescription(dto.getDescription());
        transaction.setAmount(dto.getAmount());
        transaction.setFrequency(dto.getFrequency());
        transaction.setStartDate(dto.getStartDate());
        transaction.setNextDate(calculateNextDate(dto.getStartDate(), dto.getFrequency()));
        transaction.setCategories(dto.getCategories());
        transaction.setLastGeneratedDate(null);

        RecurringTransaction saved = recurringTransactionRepository.save(transaction);

        return new RecurringTransactionResponseDTO(
                saved.getId(),
                saved.getUser().getId(),
                saved.getAccount().getId(),
                saved.getAccount().getName(),
                saved.getDescription(),
                saved.getAmount(),
                saved.getFrequency(),
                saved.getStartDate(),
                saved.getNextDate(),
                saved.getCategories(),
                saved.getLastGeneratedDate()
        );
    }

    @Override
    public List<RecurringTransactionResponseDTO> getRecurringTransactionsByUser(Long userId) {
        return recurringTransactionRepository.findByUserId(userId)
                .stream()
                .map(tx -> new RecurringTransactionResponseDTO(
                        tx.getId(),
                        tx.getUser().getId(),
                        tx.getAccount().getId(),
                        tx.getAccount().getName(),
                        tx.getDescription(),
                        tx.getAmount(),
                        tx.getFrequency(),
                        tx.getStartDate(),
                        tx.getNextDate(),
                        tx.getCategories(),
                        tx.getLastGeneratedDate()
                ))
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
}
