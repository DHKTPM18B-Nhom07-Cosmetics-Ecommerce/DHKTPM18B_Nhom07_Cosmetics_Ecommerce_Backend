package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.model.CartItem;
import iuh.fit.se.cosmeticsecommercebackend.repository.CartItemRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;

    @Override
    public List<CartItem> getAllCartItems() {
        return cartItemRepository.findAll();
    }

    @Override
    public List<CartItem> getCartItemsByCartId(Long cartId) {
        return cartItemRepository.findByCart_Id(cartId);
    }

    @Override
    public CartItem getCartItemById(Long id) {
        return cartItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CartItem not found with id: " + id));
    }

    @Override
    public CartItem createCartItem(CartItem cartItem) {
        // Tính lại subtotal
        if (cartItem.getProductVariant() != null && cartItem.getProductVariant().getPrice() != null) {
            cartItem.setSubTotal(
                    cartItem.getProductVariant().getPrice()
                            .multiply(new java.math.BigDecimal(cartItem.getQuantity()))
            );
        }
        return cartItemRepository.save(cartItem);
    }

    @Override
    public CartItem updateCartItem(Long id, CartItem cartItem) {
        CartItem existing = getCartItemById(id);
        existing.setQuantity(cartItem.getQuantity());
        existing.setProductVariant(cartItem.getProductVariant());
        existing.setCart(cartItem.getCart());
        // Cập nhật subtotal
        if (cartItem.getProductVariant() != null && cartItem.getProductVariant().getPrice() != null) {
            existing.setSubTotal(
                    cartItem.getProductVariant().getPrice()
                            .multiply(new java.math.BigDecimal(cartItem.getQuantity()))
            );
        }
        return cartItemRepository.save(existing);
    }

    @Override
    public void deleteCartItem(Long id) {
        cartItemRepository.deleteById(id);
    }
}
