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
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Address>> getAddressesByCustomerId(@PathVariable Long customerId) {
        List<Address> addresses = addressService.findByCustomerId(customerId);
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
        Long customerId = ((Number) body.get("customerId")).longValue();
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy customer id: " + customerId));

        Address address = new Address();
        address.setCustomer(customer);
        address.setFullName((String) body.get("fullName"));
        address.setPhone((String) body.get("phone"));
        address.setAddress((String) body.get("address"));
        address.setCity((String) body.get("city"));
        address.setState((String) body.get("state"));
        address.setCountry((String) body.get("country"));
        address.setDefault((boolean) body.get("default"));

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
