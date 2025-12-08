package iuh.fit.se.cosmeticsecommercebackend.repository;

import iuh.fit.se.cosmeticsecommercebackend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Tìm Employee bằng Username của Account liên kết
    @Query("SELECT e FROM Employee e JOIN e.account a WHERE a.username = :username")
    Optional<Employee> findByAccountUsername(@Param("username") String username);
}