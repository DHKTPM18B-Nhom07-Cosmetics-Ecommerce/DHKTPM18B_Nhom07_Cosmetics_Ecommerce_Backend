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
@ToString(exclude = {"account"})
@EqualsAndHashCode(exclude = {"account"})

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
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Account account;

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDateTime hireDate) {
        this.hireDate = hireDate;
    }

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

