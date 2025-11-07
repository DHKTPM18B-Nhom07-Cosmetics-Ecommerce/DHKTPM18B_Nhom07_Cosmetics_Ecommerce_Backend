package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.model.Cart;
import iuh.fit.se.cosmeticsecommercebackend.repository.CartRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.CartService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository repo;

    public CartServiceImpl(CartRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Cart> getAll() {
        return repo.findAll();
    }

    @Override
    public Optional<Cart> getById(Long id) {
        return repo.findById(id);
    }

    @Override
    public Cart create(Cart cart) {
        return repo.save(cart);
    }

    @Override
    public Cart update(Long id, Cart cart) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setTotalPrice(cart.getTotalPrice());
                    existing.setCustomer(cart.getCustomer());
                    existing.setItems(cart.getItems());
                    return repo.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
