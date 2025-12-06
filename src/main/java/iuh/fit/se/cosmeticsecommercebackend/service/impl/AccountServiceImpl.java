package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.model.Account;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.AccountRole;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.AccountStatus;
import iuh.fit.se.cosmeticsecommercebackend.repository.AccountRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.AccountService;
import iuh.fit.se.cosmeticsecommercebackend.service.RiskService; // Import mới
import iuh.fit.se.cosmeticsecommercebackend.service.MailService; // Import mới

import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
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

    // [THÊM KÉ Ở ĐÂY - KHÔNG SỬA CONSTRUCTOR]
    // Dùng @Autowired trực tiếp trên field để Spring tự tiêm vào
    // Không cần thêm vào constructor bên dưới
    @Autowired
    private RiskService riskService;

    @Autowired
    private MailService mailService;


    // [CONSTRUCTOR CŨ CỦA NHÓM - GIỮ NGUYÊN Y XÌ]
    // Bạn tuyệt đối không thêm tham số nào vào đây nhé
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
    public Optional<Account> findByUsername(String username) {
        return accountRepository.findByUsername(username);
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

        // 1. Code cũ: Cập nhật DB
        account.setStatus(AccountStatus.DISABLED);
        // Nếu biến reason null thì gán mặc định để ko lỗi
        String finalReason = (reason != null && !reason.isEmpty()) ? reason : "Vi phạm chính sách";
        account.setDisabledReason(finalReason);
        accountRepository.save(account);
        if (riskService != null) {
            riskService.clearLoginFail(account.getUsername());
        }
        // 2. [THÊM KÉ]: Gửi Email (Bọc try-catch để không ảnh hưởng luồng chính)
        try {
            // Gửi user
            if (account.getUsername() != null) {
                // Giả định username là email, nếu có field email riêng thì sửa thành account.getEmail()
                mailService.sendAccountDisabledEmail(account.getUsername(), account.getFullName(), finalReason);
            }
            // Gửi admin
            mailService.sendAdminAlertEmail("admin@embrosia.com", account, finalReason);
        } catch (Exception e) {
            // Lỗi mail thì thôi, kệ nó, không throw exception
            System.err.println("Lỗi gửi mail disable: " + e.getMessage());
        }
    }

    // PHÂN TRANG + FILTER CHO ADMIN DASHBOARD
    @Override
    public Page<Account> findAccountsForManagement(String role, String status, String search, Pageable pageable) {
        // 1. Code cũ: Lấy dữ liệu
        Page<Account> pageResult = accountRepository.findAll(new Specification<Account>() {
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

        // 2. Duyệt để gắn cờ Risk (Chỉ chạy trên RAM)
        if (riskService != null) { // Check null cho an toàn
            for (Account acc : pageResult.getContent()) {
                if (acc.getStatus() == AccountStatus.DISABLED) {
                    acc.setRiskLevel("NORMAL");
                    continue;
                }
                try {
                    RiskService.RiskReport report = riskService.analyzeRisk(acc.getId(), acc.getUsername());
                    // Gán vào biến @Transient
                    acc.setRiskLevel(report.level);
                    acc.setRiskNote(report.note);
                } catch (Exception e) {
                    // Lỗi gì thì bỏ qua, coi như không risk
                }
            }
        }

        return pageResult;
    }
}