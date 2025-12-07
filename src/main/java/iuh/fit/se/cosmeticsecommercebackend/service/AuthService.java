package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.Account;
import iuh.fit.se.cosmeticsecommercebackend.model.Customer;
import iuh.fit.se.cosmeticsecommercebackend.model.PasswordResetToken;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.AccountRole;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.AccountStatus;
import iuh.fit.se.cosmeticsecommercebackend.payload.*;
import iuh.fit.se.cosmeticsecommercebackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private MailService mailService;

    @Transactional
    public Account registerCustomer(RegisterRequest request) {

        if (accountRepository.findByUsername(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại.");
        }

        Account account = new Account();
        account.setUsername(request.getEmail());
        account.setFullName(request.getFullName());
        account.setPhoneNumber(request.getPhone());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setRole(AccountRole.CUSTOMER);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());

        Account savedAccount = accountRepository.save(account);

        Customer customer = new Customer();
        customer.setAccount(savedAccount);
        Customer savedCustomer = customerRepository.save(customer);

        // GẮN ĐƠN GUEST → CUSTOMER
        orderService.linkGuestOrders(request.getPhone(), savedCustomer);

        return savedAccount;
    }

    @Transactional
    public void createPasswordResetToken(ForgotPasswordRequest request) {
        Account account = accountRepository.findByUsername(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        tokenRepository.deleteByAccount(account);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, account);
        tokenRepository.save(resetToken);

        mailService.sendResetPasswordEmail(account.getUsername(), token);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Token không hợp lệ"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Token hết hạn");
        }

        Account account = resetToken.getAccount();
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);

        tokenRepository.delete(resetToken);
    }
}
