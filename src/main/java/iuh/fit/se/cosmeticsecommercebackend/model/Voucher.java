package iuh.fit.se.cosmeticsecommercebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.VoucherScope;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.VoucherStatus;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.VoucherType;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "vouchers")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voucher_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VoucherType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VoucherScope scope = VoucherScope.GLOBAL;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "max_discount", precision = 10, scale = 2)
    private BigDecimal maxDiscount;

    @Column(name = "min_order_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VoucherStatus status = VoucherStatus.UPCOMING;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "per_user_limit")
    private Integer perUserLimit;

    @Column(name = "is_stackable", nullable = false)
    private boolean isStackable = false;

    /* ==========================================================
          RELATIONS (NO MORE JSON IGNORE)
       ========================================================== */

    @OneToMany(mappedBy = "voucher", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"voucher", "customer"})
    private List<VoucherRedemption> redemptions;

    @OneToMany(mappedBy = "voucher", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"voucher", "customer"})
    private List<CustomerVoucher> customerVouchers;

    /* ==========================================================
          MANY-TO-MANY SCOPE (RETURN CLEAN JSON)
       ========================================================== */

    @ManyToMany
    @JoinTable(
            name = "voucher_categories",
            joinColumns = @JoinColumn(name = "voucher_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @JsonIgnoreProperties({"products", "vouchers"})
    private Set<Category> categories = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "voucher_brands",
            joinColumns = @JoinColumn(name = "voucher_id"),
            inverseJoinColumns = @JoinColumn(name = "brand_id")
    )
    @JsonIgnoreProperties({"products", "vouchers"})
    private Set<Brand> brands = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "voucher_products",
            joinColumns = @JoinColumn(name = "voucher_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @JsonIgnoreProperties({"brand", "category", "variants", "reviews"})
    private Set<Product> products = new HashSet<>();

    /* ==========================================================
          TRANSIENT LISTS FOR FE
       ========================================================== */

    @Transient
    private List<Long> categoryIds;

    @Transient
    private List<Long> brandIds;

    @Transient
    private List<Long> productIds;

    /* ==========================================================
          TIMESTAMPS
       ========================================================== */

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = VoucherStatus.UPCOMING;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /* ==========================================================
          GETTERS & SETTERS
       ========================================================== */

    public Voucher() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public VoucherType getType() { return type; }
    public void setType(VoucherType type) { this.type = type; }

    public VoucherScope getScope() { return scope; }
    public void setScope(VoucherScope scope) { this.scope = scope; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public BigDecimal getMaxDiscount() { return maxDiscount; }
    public void setMaxDiscount(BigDecimal maxDiscount) { this.maxDiscount = maxDiscount; }

    public BigDecimal getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }

    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }

    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }

    public VoucherStatus getStatus() { return status; }
    public void setStatus(VoucherStatus status) { this.status = status; }

    public Integer getMaxUses() { return maxUses; }
    public void setMaxUses(Integer maxUses) { this.maxUses = maxUses; }

    public Integer getPerUserLimit() { return perUserLimit; }
    public void setPerUserLimit(Integer perUserLimit) { this.perUserLimit = perUserLimit; }

    public boolean isStackable() { return isStackable; }
    public void setStackable(boolean stackable) { isStackable = stackable; }

    public List<VoucherRedemption> getRedemptions() { return redemptions; }
    public void setRedemptions(List<VoucherRedemption> redemptions) { this.redemptions = redemptions; }

    public List<CustomerVoucher> getCustomerVouchers() { return customerVouchers; }
    public void setCustomerVouchers(List<CustomerVoucher> customerVouchers) { this.customerVouchers = customerVouchers; }

    public Set<Category> getCategories() { return categories; }
    public void setCategories(Set<Category> categories) { this.categories = categories; }

    public Set<Brand> getBrands() { return brands; }
    public void setBrands(Set<Brand> brands) { this.brands = brands; }

    public Set<Product> getProducts() { return products; }
    public void setProducts(Set<Product> products) { this.products = products; }

    public List<Long> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<Long> categoryIds) { this.categoryIds = categoryIds; }

    public List<Long> getBrandIds() { return brandIds; }
    public void setBrandIds(List<Long> brandIds) { this.brandIds = brandIds; }

    public List<Long> getProductIds() { return productIds; }
    public void setProductIds(List<Long> productIds) { this.productIds = productIds; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
