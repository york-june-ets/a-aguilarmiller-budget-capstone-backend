package york.fse.budgetappbackend.service;

import york.fse.budgetappbackend.dto.AccountRequestDTO;
import york.fse.budgetappbackend.dto.AccountResponseDTO;

import java.util.List;

public interface AccountService {
    AccountResponseDTO createAccount(AccountRequestDTO dto);
    AccountResponseDTO updateAccount(Long id, AccountRequestDTO dto);
    void deleteAccount(Long id);
    List<AccountResponseDTO> getAccountsByUser(Long userId);
}
