package iuh.fit.se.cosmeticsecommercebackend.model;

import iuh.fit.se.cosmeticsecommercebackend.model.enums.AccountRole;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho tài khoản người dùng
 * Quan hệ 1-1 với Customer hoặc Employee
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    /**
     * Tên đăng nhập là email
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false, length = 255)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountStatus status = AccountStatus.ACTIVE;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountRole role;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(length = 500)
    private String disabledReason;
    
    @Column(nullable = false, length = 100)
    private String fullName;
    
    @Column(length = 20)
    private String phoneNumber;
    
    /**
     * Tự động set createdAt khi tạo mới
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

