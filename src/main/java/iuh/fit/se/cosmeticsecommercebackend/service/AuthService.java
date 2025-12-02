package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.Account;
import iuh.fit.se.cosmeticsecommercebackend.model.Customer;
import iuh.fit.se.cosmeticsecommercebackend.model.PasswordResetToken;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.AccountRole;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.AccountStatus;
import iuh.fit.se.cosmeticsecommercebackend.payload.ForgotPasswordRequest;
import iuh.fit.se.cosmeticsecommercebackend.payload.RegisterRequest;
import iuh.fit.se.cosmeticsecommercebackend.payload.ResetPasswordRequest;
import iuh.fit.se.cosmeticsecommercebackend.repository.AccountRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.CustomerRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.PasswordResetTokenRepository;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired // TIÊM MAIL SERVICE THỰC TẾ
    private MailService mailService;

    /**
     * Xử lý logic đăng ký khách hàng mới: Tạo Account và Customer.
     * @param request Dữ liệu đăng ký từ Frontend.
     * @return Account đã được tạo.
     */
    @Transactional
    public Account registerCustomer(RegisterRequest request) {

        // 1. Kiểm tra tồn tại (để tránh lỗi Unique Constraint)
        if (accountRepository.findByUsername(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại.");
        }

        // 2. Tạo Account Entity
        Account newAccount = new Account();
        newAccount.setUsername(request.getEmail()); // Username là Email
        newAccount.setFullName(request.getFullName());
        newAccount.setPhoneNumber(request.getPhone());

        // Hash mật khẩu trước khi lưu (BẮT BUỘC TRONG MÔI TRƯỜNG CÓ PasswordEncoder)
        newAccount.setPassword(passwordEncoder.encode(request.getPassword()));

        // Thiết lập trạng thái mặc định (theo yêu cầu)
        newAccount.setRole(AccountRole.CUSTOMER);
        newAccount.setStatus(AccountStatus.ACTIVE);
        newAccount.setDisabledReason(null);
        newAccount.setCreatedAt(LocalDateTime.now()); // @PrePersist trong Account.java cũng làm điều này

        // 3. Lưu Account
        Account savedAccount = accountRepository.save(newAccount);

        // 4. Tạo Customer Entity (Quan hệ 1-1 với Account)
        Customer newCustomer = new Customer();
        newCustomer.setAccount(savedAccount);

        // 5. Lưu Customer
        customerRepository.save(newCustomer);

        return savedAccount;
    }

    /**
     * BƯỚC 1: Xử lý yêu cầu quên mật khẩu (Gửi Token qua email)
     */
    @Transactional
    public void createPasswordResetToken(ForgotPasswordRequest request) {

        // 1. Tìm Account
        Account account = accountRepository.findByUsername(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email này."));

        // 2. Tạo Token ngẫu nhiên
        String token = UUID.randomUUID().toString();

        // 3. Xóa Token cũ của Account này
        // Spring Data JPA sẽ tự động tìm và xóa các token liên kết với Account này.
        tokenRepository.deleteByAccount(account);
        PasswordResetToken oldToken = tokenRepository.findByAccount(account).orElse(null);
        if (oldToken != null)
            tokenRepository.delete(oldToken);
        tokenRepository.flush();

        // 4. Lưu Token mới vào CSDL
        PasswordResetToken resetToken = new PasswordResetToken(token, account);
        tokenRepository.save(resetToken);

        // 5. Gửi Email
        mailService.sendResetPasswordEmail(account.getUsername(), token);
    }

    /**
     * BƯỚC 2: Xử lý đặt lại mật khẩu bằng Token
     * @param request Chứa Token và Mật khẩu mới
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        // 1. Tìm Token trong CSDL
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Token không hợp lệ."));

        // 2. Kiểm tra Token còn hạn không
        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken); // Xóa token hết hạn
            throw new IllegalArgumentException("Token đã hết hạn. Vui lòng gửi lại yêu cầu.");
        }

        // 3. Cập nhật mật khẩu mới cho Account
        Account account = resetToken.getAccount();
        String encodedPassword = passwordEncoder.encode(request.getNewPassword()); // MÃ HÓA MẬT KHẨU MỚI
        account.setPassword(encodedPassword);
        accountRepository.save(account);

        // 4. Xóa Token khỏi CSDL sau khi sử dụng thành công
        tokenRepository.delete(resetToken);
    }
}
