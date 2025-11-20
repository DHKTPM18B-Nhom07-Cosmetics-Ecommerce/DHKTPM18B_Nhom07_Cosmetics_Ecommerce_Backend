package iuh.fit.se.cosmeticsecommercebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho thương hiệu sản phẩm
 * Quan hệ 1-n với Product
 */
@Entity
@Table(name = "brands")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"products", "vouchers"})
public class Brand {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id")
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 500)
    private String logo;
    
    @Column(nullable = false, name = "is_active")
    private boolean isActive = true;
    
    /**
     * Quan hệ 1-n với Product
     * mappedBy = "brand" tham chiếu đến thuộc tính brand trong Product entity
     * cascade = CascadeType.ALL: các thao tác trên Brand sẽ cascade sang Product
     * orphanRemoval = true: tự động xóa Product khi bị remove khỏi collection
     */
    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore //tránh vòng lặp JSON khi trả response
    private List<Product> products = new ArrayList<>();


    /* 1-N với Voucher
    một brand có thể có nhiều voucher riêng
    thêm liên kết 2 chiều với voucher, dùng fetch để tối uu hiệu năng
    JsonIgnore //tránh vòng lặp JSON khi trả response
    */
    @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Voucher> vouchers = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<Voucher> getVouchers() {
        return vouchers;
    }

    public void setVouchers(List<Voucher> vouchers) {
        this.vouchers = vouchers;
    }
}

