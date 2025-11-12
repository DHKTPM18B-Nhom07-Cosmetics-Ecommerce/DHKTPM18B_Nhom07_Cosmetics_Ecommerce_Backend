package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.model.Address;
import iuh.fit.se.cosmeticsecommercebackend.repository.AddressRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.AddressService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;

    public AddressServiceImpl(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public List<Address> getAll() {
        return addressRepository.findAll();
    }

    @Override
    public Address findById(Long id) {
        return addressRepository.findById(id).orElse(null);
    }

    @Override
    public Address create(Address address) {
        return addressRepository.save(address);
    }

    @Override
    public Address update(Long addressId, Address updatedAddress) {
        // Tìm địa chỉ hiện tại
        Address existing = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy địa chỉ với id: " + addressId));

        // Cập nhật các trường (chỉ update những trường cho phép)
        existing.setFullName(updatedAddress.getFullName());
        existing.setPhone(updatedAddress.getPhone());
        existing.setAddress(updatedAddress.getAddress());
        existing.setCity(updatedAddress.getCity());
        existing.setState(updatedAddress.getState());
        existing.setCountry(updatedAddress.getCountry());
        existing.setDefault(updatedAddress.isDefault());

        // Đảm bảo chỉ có 1 địa chỉ mặc định cho mỗi customer:
        if (updatedAddress.isDefault()) {
            unsetOtherDefaultAddresses(existing.getCustomer().getId(), existing.getId());
        }

        // Lưu lại thay đổi
        return addressRepository.save(existing);
    }

    /**
     * Hủy trạng thái mặc định của các địa chỉ khác cùng customer
     */
    private void unsetOtherDefaultAddresses(Long customerId, Long excludeAddressId) {
        List<Address> addresses = addressRepository.findAll();

        addresses.stream()
                .filter(addr -> addr.getCustomer().getId().equals(customerId)
                        && !addr.getId().equals(excludeAddressId)
                        && addr.isDefault())
                .forEach(addr -> {
                    addr.setDefault(false);
                    addressRepository.save(addr);
                });
    }

    @Override
    public void delete(Long id) {
        addressRepository.deleteById(id);
    }

    @Override
    public List<Address> findByCustomerId(Long id) {
        return addressRepository.findByCustomerId(id);
    }

    @Override
    public Address getDefaultAddressByCustomerId(Long id) {
        return addressRepository.findByCustomerIdAndIsDefaultTrue(id)
                .orElse(null);
    }
}
