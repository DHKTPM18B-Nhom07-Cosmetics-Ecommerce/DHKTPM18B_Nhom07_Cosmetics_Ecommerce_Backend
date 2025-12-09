package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.model.Address;
import iuh.fit.se.cosmeticsecommercebackend.model.Customer;
import iuh.fit.se.cosmeticsecommercebackend.repository.CustomerRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.AddressService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "*")
public class AddressController {

    private final AddressService addressService;
    private final CustomerRepository customerRepository;

    public AddressController(AddressService addressService, CustomerRepository customerRepository) {
        this.addressService = addressService;
        this.customerRepository = customerRepository;
    }
    // Lấy tất cả địa chỉ
    @GetMapping
    public ResponseEntity<List<Address>> getAllAddresses() {
        List<Address> addresses = addressService.getAll();
        return ResponseEntity.ok(addresses);
    }

    // 🔹 Lấy tất cả địa chỉ theo customerId
    @GetMapping("/customer/{accountId}")
    public ResponseEntity<List<Address>> getAddressesByAccountId(@PathVariable Long accountId) {
        // Tìm Customer dựa trên Account ID
        Customer customer = customerRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng với Account ID: " + accountId));

        // Lấy địa chỉ của Customer vừa tìm được
        List<Address> addresses = addressService.findByCustomerId(customer.getId());
        return ResponseEntity.ok(addresses);
    }
    // 🔹 Lấy địa chỉ mặc định của customer
        @GetMapping("/customer/{customerId}/default")
        public ResponseEntity<Address> getDefaultAddress(@PathVariable Long customerId) {
            Address address = addressService.getDefaultAddressByCustomerId(customerId);
            return ResponseEntity.ok(address);
        }

    // 🔹 Cập nhật 1 địa chỉ (PUT /api/addresses/{id})
    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(
            @PathVariable Long id,
            @RequestBody Address updatedAddress) {

        Address saved = addressService.update(id, updatedAddress);
        return ResponseEntity.ok(saved);
    }

    // 🔹 Tạo mới địa chỉ
        @PostMapping
        public ResponseEntity<Address> createAddress(@RequestBody Map<String, Object> body) {
            // Lấy Account ID từ body (Frontend gửi key là 'customerId' nhưng giá trị là accountId)
            Long accountId = ((Number) body.get("customerId")).longValue();

            // Tìm Customer chuẩn từ Account ID
            Customer customer = customerRepository.findByAccount_Id(accountId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng với Account ID: " + accountId));

            Address address = new Address();
            address.setCustomer(customer); // Gán customer tìm được vào địa chỉ

            address.setFullName((String) body.get("fullName"));
            address.setPhone((String) body.get("phone"));
            address.setAddress((String) body.get("address"));
            address.setCity((String) body.get("city"));
            address.setState((String) body.get("state"));
            address.setCountry((String) body.get("country"));

            // Xử lý an toàn cho boolean default
            Object isDefaultObj = body.get("default");
            address.setDefault(isDefaultObj != null && (boolean) isDefaultObj);

            Address saved = addressService.create(address);
            return ResponseEntity.ok(saved);
        }

    // 🔹 Xóa địa chỉ
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
            addressService.delete(id);
            return ResponseEntity.noContent().build();
        }
}
