package iuh.fit.se.cosmeticsecommercebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity đại diện cho danh mục sản phẩm
 * Quan hệ 1-n với Product
 */
@Entity
@Table(name = "categories")
@EqualsAndHashCode(exclude = {"products", "vouchers"})
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * Quan hệ 1-n với Product
     * mappedBy = "category" tham chiếu đến thuộc tính category trong Product entity
     * cascade = CascadeType.ALL: các thao tác trên Category sẽ cascade sang Product
     * orphanRemoval = true: tự động xóa Product khi bị remove khỏi collection
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Product> products = new ArrayList<>();

    // N - N với Voucher
    @ManyToMany(mappedBy = "categories")
    @JsonIgnore
    private Set<Voucher> vouchers = new HashSet<>();

    public Category(Long id, String name, List<Product> products, Set<Voucher> vouchers) {

        this.id = id;
        this.name = name;
        this.products = products;
        this.vouchers = vouchers;
    }

    public Category() {
    }

    public Set<Voucher> getVouchers() {
        return vouchers;
    }

    public void setVouchers(Set<Voucher> vouchers) {
        this.vouchers = vouchers;
    }

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

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

}
