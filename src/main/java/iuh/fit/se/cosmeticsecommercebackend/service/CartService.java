package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.Cart;

import java.util.List;
import java.util.Optional;

public interface CartService {
    List<Cart> getAll();
    Optional<Cart> getById(Long id);
    Cart create(Cart cart);
    Cart update(Long id, Cart cart);
    void delete(Long id);
}
