package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface AccountService {
    List<Account> findAllAccounts();
    Optional<Account> findAccountById(Long id);
    Account createAccount(Account account);
    Account updateAccount(Long id, Account accountDetails);
    void disableAccount(Long id, String reason);

    Page<Account> findAccountsForManagement(
            String role,
            String status,
            String search,
            Pageable pageable
    );

}