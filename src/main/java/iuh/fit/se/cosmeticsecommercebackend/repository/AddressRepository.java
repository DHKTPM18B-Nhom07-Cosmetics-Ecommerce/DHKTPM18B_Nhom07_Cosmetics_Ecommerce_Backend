package iuh.fit.se.cosmeticsecommercebackend.repository;

import iuh.fit.se.cosmeticsecommercebackend.model.Address;
import iuh.fit.se.cosmeticsecommercebackend.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByCustomerId(Long id);
    Optional<Address> findByCustomerIdAndIsDefaultTrue(Long id);

    @Modifying
    @Query("""
        UPDATE Address a
        SET a.customer = :customer
        WHERE a.customer IS NULL
          AND a.phone = :phone
    """)
    void linkGuestAddressToCustomer(
            @Param("customer") Customer customer,
            @Param("phone") String phone
    );
}