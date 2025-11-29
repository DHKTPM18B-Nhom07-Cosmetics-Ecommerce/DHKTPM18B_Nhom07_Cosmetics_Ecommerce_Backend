package iuh.fit.se.cosmeticsecommercebackend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    private static final int EXPIRATION_MINUTES = 5; // Token hết hạn sau 5 phút

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token; // Token ngẫu nhiên (hoặc OTP)

    @OneToOne(targetEntity = Account.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "account_id")
    private Account account; // Tài khoản liên quan

    @Column(nullable = false)
    private LocalDateTime expiryDate; // Thời gian hết hạn

    public PasswordResetToken(String token, Account account) {
        this.token = token;
        this.account = account;
        this.expiryDate = calculateExpiryDate();
    }

    public PasswordResetToken() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    private LocalDateTime calculateExpiryDate() {
        return LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
    }

    // Kiểm tra Token còn hạn hay không
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
