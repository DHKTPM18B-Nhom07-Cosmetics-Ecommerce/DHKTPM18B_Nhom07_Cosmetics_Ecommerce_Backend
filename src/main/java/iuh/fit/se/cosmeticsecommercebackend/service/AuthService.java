package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.Account;
import iuh.fit.se.cosmeticsecommercebackend.model.Customer;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.AccountRole;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.AccountStatus;
import iuh.fit.se.cosmeticsecommercebackend.payload.RegisterRequest;
import iuh.fit.se.cosmeticsecommercebackend.repository.AccountRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
}
