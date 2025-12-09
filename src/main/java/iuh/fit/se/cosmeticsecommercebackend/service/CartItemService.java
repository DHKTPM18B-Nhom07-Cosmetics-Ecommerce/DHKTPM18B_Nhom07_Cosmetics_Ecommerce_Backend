package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.CartItem;

import java.util.List;

public interface CartItemService {
    List<CartItem> getAllCartItems();
    List<CartItem> getCartItemsByCartId(Long cartId);
    CartItem getCartItemById(Long id);
    CartItem createCartItem(CartItem cartItem);
    CartItem updateCartItem(Long id, CartItem cartItem);
    void deleteCartItem(Long id);
    void deleteCartItemById(Long id);
}
