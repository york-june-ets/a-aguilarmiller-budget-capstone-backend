package york.fse.budgetappbackend.service;

import org.springframework.stereotype.Service;
import york.fse.budgetappbackend.dto.AccountRequestDTO;
import york.fse.budgetappbackend.dto.AccountResponseDTO;
import york.fse.budgetappbackend.model.Account;
import york.fse.budgetappbackend.model.User;
import york.fse.budgetappbackend.repository.AccountRepository;
import york.fse.budgetappbackend.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountServiceImpl(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AccountResponseDTO createAccount(AccountRequestDTO dto) {
        Optional<User> optionalUser = userRepository.findById(dto.getUserId());
        if (optionalUser.isEmpty()) throw new IllegalArgumentException("User not found");

        Account account = new Account();
        account.setName(dto.getName());
        account.setType(dto.getType());
        account.setBalance(dto.getBalance());
        account.setUser(optionalUser.get());

        return mapToDTO(accountRepository.save(account));
    }

    @Override
    public AccountResponseDTO updateAccount(Long id, AccountRequestDTO dto) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (dto.getName() != null) account.setName(dto.getName());
        if (dto.getType() != null) account.setType(dto.getType());
        if (dto.getBalance() != null) account.setBalance(dto.getBalance());

        return mapToDTO(accountRepository.save(account));
    }

    @Override
    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new IllegalArgumentException("Account not found");
        }
        accountRepository.deleteById(id);
    }

    @Override
    public List<AccountResponseDTO> getAccountsByUser(Long userId) {
        return accountRepository.findAllByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private AccountResponseDTO mapToDTO(Account account) {
        AccountResponseDTO dto = new AccountResponseDTO();
        dto.setId(account.getId());
        dto.setName(account.getName());
        dto.setType(account.getType());
        dto.setBalance(account.getBalance());
        dto.setUserId(account.getUser().getId());
        dto.setCreatedAt(account.getCreatedAt());
        return dto;
    }
}
