package iuh.fit.se.cosmeticsecommercebackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho nhân viên
 * Quan hệ 1-1 với Account
 */
@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "account")
@EqualsAndHashCode(exclude = "account")
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Quan hệ 1-1 với Account
     * 1 Employee có 1 Account
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;
    
    @Column(nullable = false)
    private LocalDateTime hireDate;
    
    /**
     * Tự động set hireDate khi tạo mới
     */
    @PrePersist
    protected void onCreate() {
        if (hireDate == null) {
            hireDate = LocalDateTime.now();
        }
    }
}

