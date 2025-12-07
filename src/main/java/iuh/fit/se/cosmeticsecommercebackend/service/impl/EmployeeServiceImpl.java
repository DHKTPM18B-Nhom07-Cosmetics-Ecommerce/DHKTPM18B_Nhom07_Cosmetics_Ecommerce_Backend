package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.model.Account;

import iuh.fit.se.cosmeticsecommercebackend.model.Employee;
import iuh.fit.se.cosmeticsecommercebackend.repository.AccountRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.EmployeeRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
//@RequiredArgsConstructor// Tự động @Autowired các field final
@Transactional // Đảm bảo các thao tác DB là một khối
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, AccountRepository accountRepository) {
        this.employeeRepository = employeeRepository;
        this.accountRepository = accountRepository;
    }
    @Override
    public List<Employee> findAllEmployees() {
        return employeeRepository.findAll();
    }

    @Override
    public Optional<Employee> findEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    @Override
    public Employee createEmployee(Employee employee) {
        // Vì không có Cascade, ta phải lưu Account trước
        Account account = employee.getAccount();
        if (account == null) {
            throw new IllegalArgumentException("Employee must have an associated account.");
        }

        // Tốt nhất là mã hóa mật khẩu ở đây
         account.setPassword(passwordEncoder.encode(account.getPassword()));

        // Lưu Account trước để lấy ID
        Account savedAccount = accountRepository.save(account);

        // Gán Account đã lưu vào Employee
        employee.setAccount(savedAccount);

        // Lưu Employee
        return employeeRepository.save(employee);
    }

    @Override
    public Employee updateEmployee(Long id, Employee employeeDetails) {
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        Account existingAccount = existingEmployee.getAccount();
        Account accountDetails = employeeDetails.getAccount();

        // Cập nhật thông tin Account
        if (accountDetails != null) {
            existingAccount.setUsername(accountDetails.getUsername());
            existingAccount.setFullName(accountDetails.getFullName());
            existingAccount.setPhoneNumber(accountDetails.getPhoneNumber());
            existingAccount.setStatus(accountDetails.getStatus());
            existingAccount.setRole(accountDetails.getRole());

            // Nếu mật khẩu được gửi lên (không rỗng) thì mới cập nhật
            if (accountDetails.getPassword() != null && !accountDetails.getPassword().isEmpty()) {
                // existingAccount.setPassword(passwordEncoder.encode(accountDetails.getPassword()));
                existingAccount.setPassword(accountDetails.getPassword()); // Tạm thời chưa mã hóa
            }
            accountRepository.save(existingAccount);
        }

        // Cập nhật thông tin Employee
        existingEmployee.setHireDate(employeeDetails.getHireDate());

        return employeeRepository.save(existingEmployee);
    }

    @Override
    public void deleteEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        // Vì không có Cascade, ta phải xóa Employee trước
        employeeRepository.delete(employee);

        // Sau đó xóa Account
        accountRepository.delete(employee.getAccount());
    }
}
