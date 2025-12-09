package iuh.fit.se.cosmeticsecommercebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
@Entity
@Table(name = "addresses")
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "customer")
@EqualsAndHashCode(exclude = "customer")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

public class Address {

    @Id
    @Column(name = "address_id")
    private Long id;

    /**
     * Nhiều Address có thể thuộc 1 Customer
     * Guest checkout → customer = NULL
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = true)
    @JsonBackReference
    @JsonIgnore
    private Customer customer;

    @Column(nullable = true, length = 100)
    private String fullName;

    @Column(nullable = true, length = 20)
    private String phone;

    @Column(nullable = true, length = 255)
    private String address;

    @Column(nullable = true, length = 100)
    private String city;

    @Column(nullable = true, length = 100)
    private String state;

    @Column(nullable = true, length = 100)
    private String country;

    /**
     * Chỉ áp dụng cho address của CUSTOMER
     * Guest luôn = false
     */
    @Column(name = "is_default")
    private boolean isDefault = false;

    /* ===== GETTER / SETTER ===== */

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
    public static Long generateAddressId() {
        return Math.abs(UUID.randomUUID().getMostSignificantBits());}

}
