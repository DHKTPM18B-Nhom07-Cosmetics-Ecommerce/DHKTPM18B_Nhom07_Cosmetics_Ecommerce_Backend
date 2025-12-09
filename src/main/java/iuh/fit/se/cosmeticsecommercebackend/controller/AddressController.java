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
@CrossOrigin(origins = "*") // Đảm bảo dòng này có để tránh lỗi CORS
public class AddressController {

    private final AddressService addressService;
    private final CustomerRepository customerRepository;

    public AddressController(AddressService addressService, CustomerRepository customerRepository) {
        this.addressService = addressService;
        this.customerRepository = customerRepository;
    }

    // 1. Lấy tất cả địa chỉ (Admin dùng)
    @GetMapping
    public ResponseEntity<List<Address>> getAllAddresses() {
        return ResponseEntity.ok(addressService.getAll());
    }

    // 2. Lấy địa chỉ theo Account ID (SỬA LOGIC TẠI ĐÂY)
    @GetMapping("/customer/{accountId}")
    public ResponseEntity<List<Address>> getAddressesByAccountId(@PathVariable Long accountId) {
        // Tìm Customer dựa trên Account ID (Thay vì Customer ID)
        Customer customer = customerRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng sở hữu Account ID: " + accountId));

        // Lấy danh sách địa chỉ của customer đó
        return ResponseEntity.ok(addressService.findByCustomerId(customer.getId()));
    }

    // 3. Lấy địa chỉ mặc định
    @GetMapping("/customer/{customerId}/default")
    public ResponseEntity<Address> getDefaultAddress(@PathVariable Long customerId) {
        return ResponseEntity.ok(addressService.getDefaultAddressByCustomerId(customerId));
    }

    // 4. Cập nhật địa chỉ
    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(@PathVariable Long id, @RequestBody Address updatedAddress) {
        Address saved = addressService.update(id, updatedAddress);
        return ResponseEntity.ok(saved);
    }

    // 5. Tạo mới địa chỉ (SỬA LOGIC QUAN TRỌNG TẠI ĐÂY)
    @PostMapping
    public ResponseEntity<Address> createAddress(@RequestBody Map<String, Object> body) {
        // Lấy Account ID mà frontend gửi lên
        Long accountId = ((Number) body.get("customerId")).longValue();

        // --- SỬA LẠI DÒNG NÀY ---
        // Dùng findByAccount_Id để tìm ra đúng Customer
        Customer customer = customerRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng sở hữu Account ID: " + accountId));
        // ------------------------

        Address address = new Address();
        address.setCustomer(customer); // Gán customer tìm được vào địa chỉ
        address.setFullName((String) body.get("fullName"));
        address.setPhone((String) body.get("phone"));
        address.setAddress((String) body.get("address"));
        address.setCity((String) body.get("city"));
        address.setState((String) body.get("state"));
        address.setCountry((String) body.get("country"));

        Object isDefaultObj = body.get("default");
        address.setDefault(isDefaultObj != null && (boolean) isDefaultObj);

        Address saved = addressService.create(address);
        return ResponseEntity.ok(saved);
    }

    // 6. Xóa địa chỉ
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}