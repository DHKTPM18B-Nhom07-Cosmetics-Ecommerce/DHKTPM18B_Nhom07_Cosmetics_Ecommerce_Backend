package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.Customer;
import org.springframework.stereotype.Service;

import java.util.List;

public interface CustomerService {
    public List<Customer> getAll();
    public Customer findById(Long id);
    public Customer create(Customer customer);
    public Customer update(Long id, Customer customer);
    public void delete(Long id);

    Customer findByAccountUsername(String username);
    Customer findByAccountId(Long accountId);
}
