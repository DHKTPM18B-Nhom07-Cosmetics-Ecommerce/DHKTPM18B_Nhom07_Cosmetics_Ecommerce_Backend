package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.model.Account;
import iuh.fit.se.cosmeticsecommercebackend.service.AccountService;
import iuh.fit.se.cosmeticsecommercebackend.service.RiskService; // [1. IMPORT CÁI NÀY]
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired; // [2. IMPORT CÁI NÀY]
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    // [3. THÊM KÉ CÁI NÀY - DÙNG AUTOWIRED ĐỂ KHÔNG SỬA CONSTRUCTOR]
    @Autowired
    private RiskService riskService;

    // [CONSTRUCTOR CŨ GIỮ NGUYÊN 100%]
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // ... (Các hàm getManagement, getAll, create, update cũ giữ nguyên) ...

    @GetMapping("/management")
    public ResponseEntity<Page<Account>> getAccountsForManagement(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Account> result = accountService.findAccountsForManagement(role, status, search, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public List<Account> getAllAccounts() {
        return accountService.findAllAccounts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        return accountService.findAccountById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Account createAccount(@RequestBody Account account) {
        return accountService.createAccount(account);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable Long id, @RequestBody Account accountDetails) {
        try {
            Account updatedAccount = accountService.updateAccount(id, accountDetails);
            return ResponseEntity.ok(updatedAccount);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> disableAccount(@PathVariable Long id,
                                            @RequestParam(required = false) String reason) {
        try {
            accountService.disableAccount(id, reason);
            return ResponseEntity.ok("Tài khoản đã bị vô hiệu hóa thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/by-username/{username}")
    public ResponseEntity<Account> getAccountByUsername(@PathVariable String username) {
        return accountService.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // [4. THÊM API MỚI VÀO CUỐI CÙNG]
    // Đây là cái API mà Frontend đang gọi bị lỗi nè
    @GetMapping("/{id}/risk-check")
    public ResponseEntity<?> checkRisk(@PathVariable Long id) {
        Account acc = accountService.findAccountById(id).orElse(null);
        if (acc == null) return ResponseEntity.notFound().build();

        // [THÊM ĐOẠN NÀY]: Nếu đã bị Vô hiệu hoá rồi thì trả về NORMAL luôn
        if (acc.getStatus() == iuh.fit.se.cosmeticsecommercebackend.model.enums.AccountStatus.DISABLED) {
            return ResponseEntity.ok(new RiskService.RiskReport("NORMAL", "Tài khoản đã bị vô hiệu hoá."));
        }

        // Logic cũ giữ nguyên
        if (riskService != null) {
            return ResponseEntity.ok(riskService.analyzeRisk(id, acc.getUsername()));
        } else {
            return ResponseEntity.internalServerError().body("Lỗi RiskService");
        }
    }
    @GetMapping("/alerts")
    public ResponseEntity<List<RiskService.SystemAlert>> getSystemAlerts() {
        if (riskService != null) {
            return ResponseEntity.ok(riskService.getAlerts());
        }
        return ResponseEntity.ok(List.of()); // Trả về list rỗng nếu lỗi
    }

    // API: Tìm customer theo accountId, trả về customerId
    @GetMapping("/{accountId}/customer-id")
    public ResponseEntity<Long> getCustomerIdByAccountId(@PathVariable Long accountId) {
        // Tìm customer theo accountId
        iuh.fit.se.cosmeticsecommercebackend.model.Customer customer = accountService.findCustomerByAccountId(accountId);
        if (customer != null && customer.getId() != null) {
            return ResponseEntity.ok(customer.getId());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}