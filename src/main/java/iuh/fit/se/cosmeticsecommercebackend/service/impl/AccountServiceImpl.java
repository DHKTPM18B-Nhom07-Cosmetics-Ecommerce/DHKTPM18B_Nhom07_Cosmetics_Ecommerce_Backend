package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.model.Account;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.AccountStatus;
import iuh.fit.se.cosmeticsecommercebackend.repository.AccountRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service

@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public List<Account> findAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public Optional<Account> findAccountById(Long id) {
        return accountRepository.findById(id);
    }

    @Override
    public Account createAccount(Account account) {
        // Cần mã hóa mật khẩu trước khi lưu
        // account.setPassword(passwordEncoder.encode(account.getPassword()));
        return accountRepository.save(account);
    }

    @Override
    public Account updateAccount(Long id, Account accountDetails) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Account với id: " + id));

        // Cập nhật các trường
        existingAccount.setUsername(accountDetails.getUsername());
        existingAccount.setFullName(accountDetails.getFullName());
        existingAccount.setPhoneNumber(accountDetails.getPhoneNumber());
        existingAccount.setStatus(accountDetails.getStatus());
        existingAccount.setRole(accountDetails.getRole());
        existingAccount.setDisabledReason(accountDetails.getDisabledReason());

        // Kiểm tra nếu có mật khẩu mới thì mới cập nhật
        if (accountDetails.getPassword() != null && !accountDetails.getPassword().isEmpty()) {
            // Cần mã hóa mật khẩu
            // existingAccount.setPassword(passwordEncoder.encode(accountDetails.getPassword()));
            existingAccount.setPassword(accountDetails.getPassword()); // Tạm thời
        }

        return accountRepository.save(existingAccount);
    }
    @Override
    public void disableAccount(Long id, String reason) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setStatus(AccountStatus.DISABLED);
        account.setDisabledReason(reason != null ? reason : "No reason provided");

        accountRepository.save(account);
    }
}