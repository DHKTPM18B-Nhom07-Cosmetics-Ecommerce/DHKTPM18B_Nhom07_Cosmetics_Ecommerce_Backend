package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.Address;
import iuh.fit.se.cosmeticsecommercebackend.model.Customer;

import java.util.List;
import java.util.Optional;


public interface AddressService {
    public List<Address> getAll();
    public Address findById(Long id);
    public Address create(Address address);
    public Address update(Long id, Address address);
    public void delete(Long id);
    public List<Address> findByCustomerId(Long id);
    public Address getDefaultAddressByCustomerId(Long id);

//    nghiệp vụ guest → customer
    void linkGuestAddresses(String phone, Customer customer);

}