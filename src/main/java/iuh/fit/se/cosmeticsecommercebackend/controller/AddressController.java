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

    public AddressController(AddressService addressService,
                             CustomerRepository customerRepository) {
        this.addressService = addressService;
        this.customerRepository = customerRepository;
    }

    // Lấy tất cả địa chỉ
    @GetMapping
    public ResponseEntity<List<Address>> getAllAddresses() {
        return ResponseEntity.ok(addressService.getAll());
    }

    // 🔹 Lấy danh sách địa chỉ (Frontend gửi AccountID -> Backend tìm Customer)
    @GetMapping("/customer/{accountId}")
    public ResponseEntity<List<Address>> getAddressesByAccountId(@PathVariable Long accountId) {
        // Ưu tiên logic của nhánh HEAD (Fix lỗi ID)
        Customer customer = customerRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng với Account ID: " + accountId));

        return ResponseEntity.ok(addressService.findByCustomerId(customer.getId()));
    }
    // 🔹 Lấy địa chỉ mặc định
    @GetMapping("/customer/{customerId}/default")
    public ResponseEntity<Address> getDefaultAddress(@PathVariable Long customerId) {
        return ResponseEntity.ok(addressService.getDefaultAddressByCustomerId(customerId));
    }

    // 🔹 Tạo mới địa chỉ
    @PostMapping
    public ResponseEntity<Address> createAddress(@RequestBody Map<String, Object> body) {
        // Lấy Account ID từ body
        Long accountId = ((Number) body.get("customerId")).longValue();

        // Ưu tiên logic của nhánh HEAD (Fix lỗi ID): Tìm Customer theo AccountID
        Customer customer = customerRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khách hàng với Account ID: " + accountId));

        Address address = new Address();
        // Nếu bên main có logic generate ID riêng thì có thể giữ lại, nếu không thì để tự động tăng
        // address.setId(Address.generateAddressId()); // Bỏ comment dòng này nếu nhóm bạn bắt buộc dùng ID tự tạo

        address.setCustomer(customer);
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

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
