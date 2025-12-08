package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.payload.CustomerIdResponse;
import iuh.fit.se.cosmeticsecommercebackend.model.Account;
import iuh.fit.se.cosmeticsecommercebackend.model.Address;
import iuh.fit.se.cosmeticsecommercebackend.model.Customer;
import iuh.fit.se.cosmeticsecommercebackend.service.AccountService;
import iuh.fit.se.cosmeticsecommercebackend.service.CustomerService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerService;
    private final AccountService accountService;

    public CustomerController(CustomerService customerService, AccountService accountService) {
        this.customerService = customerService;
        this.accountService = accountService;
    }

    // Lấy tất cả khách hàng
    @GetMapping
    public ResponseEntity<List<Customer>> getAllAddresses() {
        List<Customer> customers = customerService.getAll();
        return ResponseEntity.ok(customers);
    }

    // 🔹 Lấy khách hàng theo id
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id){
        Customer customer = customerService.findById(id);
        return ResponseEntity.ok(customer);
    }

    // 🔹 Lấy khách hàng theo account ID
    @GetMapping("/account/{accountId}")
    public ResponseEntity<CustomerIdResponse> getCustomerByAccountId(@PathVariable Long accountId){
        Customer customer = customerService.findByAccountId(accountId);
        if (customer == null) {
            throw new EntityNotFoundException("Không tìm thấy khách hàng với account ID: " + accountId);
        }
        return ResponseEntity.ok(new CustomerIdResponse(customer.getId()));
    }

    // 🔹 Cập nhật 1 địa chỉ (PUT /api/addresses/{id})
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable Long id,
            @RequestBody Customer updatedCustomer) {

        Customer saved = customerService.update(id, updatedCustomer);
        return ResponseEntity.ok(saved);
    }

    // 🔹 Tạo mới địa chỉ
    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Map<String, Object> body) {
        if (!body.containsKey("accountId") || body.get("accountId") == null || ((Number) body.get("accountId")).longValue() == 0) {
            return ResponseEntity.badRequest().build(); // Không tạo mới khách hàng nếu accountId = 0 hoặc null
        }
        Long accountId = ((Number) body.get("accountId")).longValue();
        Account account = accountService.findAccountById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản với id: " + accountId));

        Customer customer = new Customer();
        customer.setAccount(account);

        Customer saved = customerService.create(customer);
        return ResponseEntity.ok(saved);
    }

    // 🔹 Xóa địa chỉ
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
