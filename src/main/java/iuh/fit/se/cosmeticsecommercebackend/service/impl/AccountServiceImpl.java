package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.model.Account;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.AccountRole;

import iuh.fit.se.cosmeticsecommercebackend.model.enums.AccountStatus;
import java.util.ArrayList;

import iuh.fit.se.cosmeticsecommercebackend.repository.AccountRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.AccountService;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        return accountRepository.save(account);
    }

    @Override
    public Account updateAccount(Long id, Account accountDetails) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Account với id: " + id));

        existingAccount.setUsername(accountDetails.getUsername());
        existingAccount.setFullName(accountDetails.getFullName());
        existingAccount.setPhoneNumber(accountDetails.getPhoneNumber());
        existingAccount.setStatus(accountDetails.getStatus());
        existingAccount.setRole(accountDetails.getRole());
        existingAccount.setDisabledReason(accountDetails.getDisabledReason());

        if (accountDetails.getPassword() != null && !accountDetails.getPassword().isEmpty()) {
            existingAccount.setPassword(accountDetails.getPassword());
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

    // PHÂN TRANG + FILTER CHO ADMIN DASHBOARD
    @Override
    public Page<Account> findAccountsForManagement(String role, String status, String search, Pageable pageable) {
        return accountRepository.findAll(new Specification<Account>() {
            @Override
            public Predicate toPredicate(Root<Account> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();

                if (role != null && !role.isEmpty()) {
                    predicates.add(cb.equal(root.get("role"), AccountRole.valueOf(role.toUpperCase())));
                }

                if (status != null && !status.isEmpty()) {
                    predicates.add(cb.equal(root.get("status"), AccountStatus.valueOf(status.toUpperCase())));
                }

                if (search != null && !search.isEmpty()) {
                    String likePattern = "%" + search.toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("username")), likePattern),
                            cb.like(cb.lower(root.get("fullName")), likePattern)
                    ));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            }
        }, pageable);
    }
}