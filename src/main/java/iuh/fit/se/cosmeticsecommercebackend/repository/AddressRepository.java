package iuh.fit.se.cosmeticsecommercebackend.repository;

import iuh.fit.se.cosmeticsecommercebackend.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByCustomerId(Long id);

    // Explicit JPQL to avoid property-name ambiguity for boolean field 'isDefault'
    @Query("SELECT a FROM Address a WHERE a.customer.id = :customerId AND a.isDefault = true")
    Optional<Address> findDefaultByCustomerId(Long customerId);

    /**
     * Unset isDefault for all addresses of a customer except the provided address id.
     * Runs as a bulk update for efficiency.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.customer.id = :customerId AND a.id <> :excludeId")
    void unsetDefaultForCustomerExcept(Long customerId, Long excludeId);

    /**
     * Unset isDefault for all addresses of a customer.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.customer.id = :customerId")
    void unsetDefaultForCustomer(Long customerId);
}
