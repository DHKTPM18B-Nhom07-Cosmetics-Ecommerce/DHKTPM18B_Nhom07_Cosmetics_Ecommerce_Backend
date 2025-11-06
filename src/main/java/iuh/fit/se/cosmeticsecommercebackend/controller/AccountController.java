package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.model.Account;
import iuh.fit.se.cosmeticsecommercebackend.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")

public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // 1. READ (All)
    @GetMapping
    public List<Account> getAllAccounts() {
        return accountService.findAllAccounts();
    }

    // 2. READ (By ID)
    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long id) {
        return accountService.findAccountById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. CREATE
    @PostMapping
    public Account createAccount(@RequestBody Account account) {
        // Hàm này tạo 1 Account độc lập (ví dụ: cho Customer)
        return accountService.createAccount(account);
    }

    // 4. UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable Long id, @RequestBody Account accountDetails) {
        try {
            Account updatedAccount = accountService.updateAccount(id, accountDetails);
            return ResponseEntity.ok(updatedAccount);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 5. SOFT DELETE (Disable Account)
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

}