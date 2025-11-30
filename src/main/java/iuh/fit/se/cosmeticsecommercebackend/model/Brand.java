package iuh.fit.se.cosmeticsecommercebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "brands")
@EqualsAndHashCode(exclude = {"products"})
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

    /** Brand - Product (1-N) */
    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Product> products = new ArrayList<>();

    // N - N với Voucher
    @ManyToMany(mappedBy = "brands")
    @JsonIgnore
    private Set<Voucher> vouchers = new HashSet<>();

    public Brand() {
    }

    public Brand(Long id, String name, String description, String logo, boolean isActive, List<Product> products, Set<Voucher> vouchers) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.logo = logo;
        this.isActive = isActive;
        this.products = products;
        this.vouchers = vouchers;
    }

    // GETTER - SETTER

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    public Set<Voucher> getVouchers() {
        return vouchers;
    }

    public void setVouchers(Set<Voucher> vouchers) {
        this.vouchers = vouchers;
    }
}
