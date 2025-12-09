package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.model.Cart;
import iuh.fit.se.cosmeticsecommercebackend.model.CartItem;
import iuh.fit.se.cosmeticsecommercebackend.repository.CartItemRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.CartRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.CartItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;

    private final CartRepository cartRepository; // Inject thêm cái này

    // Constructor Injection cả 2 Repo
    public CartItemServiceImpl(CartItemRepository cartItemRepository, CartRepository cartRepository) {
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
    }

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
        if (cartItem.getProductVariant() != null && cartItem.getProductVariant().getPrice() != null) {
            cartItem.setSubTotal(
                    cartItem.getProductVariant().getPrice()
                            .multiply(new BigDecimal(cartItem.getQuantity()))
            );
        }
        return cartItemRepository.save(cartItem);
    }

    @Override
    @Transactional // Đảm bảo giao dịch
    public CartItem updateCartItem(Long id, CartItem cartItem) {
        // Lấy item hiện tại trong DB
        CartItem existing = getCartItemById(id);

        // Cập nhật số lượng từ tham số truyền vào
        existing.setQuantity(cartItem.getQuantity());

        // Có thể cập nhật variant/cart nếu cần, nhưng trường hợp này chủ yếu là quantity
        // existing.setProductVariant(cartItem.getProductVariant());
        // existing.setCart(cartItem.getCart());

        // --- 1. TÍNH LẠI SUBTOTAL CỦA ITEM ---
        BigDecimal price = existing.getProductVariant().getPrice();
        BigDecimal newSubTotal = price.multiply(BigDecimal.valueOf(existing.getQuantity()));
        existing.setSubTotal(newSubTotal);

        // Lưu item
        CartItem savedItem = cartItemRepository.save(existing);

        // --- 2. CẬP NHẬT TỔNG TIỀN (TOTAL PRICE) CỦA GIỎ HÀNG ---
        updateCartTotal(existing.getCart());

        return savedItem;
    }

    @Override
    @Transactional
    public void deleteCartItem(Long id) {
        CartItem item = getCartItemById(id);
        Cart cart = item.getCart();

        cartItemRepository.deleteById(id);

        // Sau khi xóa cũng cần tính lại tổng tiền giỏ hàng
        // Lưu ý: Lúc này item đã xóa khỏi DB nhưng có thể vẫn còn trong list items của object cart (nếu cart được load trước đó)
        // Nên tốt nhất là load lại list items hoặc tính toán cẩn thận.
        // Cách đơn giản nhất ở đây là:
        cart.getItems().remove(item); // Xóa khỏi list trong bộ nhớ
        updateCartTotal(cart);
    }

    @Override
    public void deleteCartItemById(Long id) {
        cartItemRepository.deleteById(id);
    }

    // Hàm phụ để tính tổng tiền và lưu giỏ hàng
    private void updateCartTotal(Cart cart) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            if (item.getSubTotal() != null) {
                total = total.add(item.getSubTotal());
            }
        }
        cart.setTotalPrice(total);
        cartRepository.save(cart);
    }
}