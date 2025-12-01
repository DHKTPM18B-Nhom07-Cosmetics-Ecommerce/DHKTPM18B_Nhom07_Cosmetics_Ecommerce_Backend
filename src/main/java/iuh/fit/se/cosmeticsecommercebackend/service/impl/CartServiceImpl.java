package iuh.fit.se.cosmeticsecommercebackend.service.impl;

import iuh.fit.se.cosmeticsecommercebackend.model.*;
import iuh.fit.se.cosmeticsecommercebackend.repository.*;
import iuh.fit.se.cosmeticsecommercebackend.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    private final CustomerRepository customerRepository;
    private final ProductVariantRepository productVariantRepository;

    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           CustomerRepository customerRepository,
                           ProductVariantRepository productVariantRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.customerRepository = customerRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Override
    public List<Cart> getAll() {
        return cartRepository.findAll();
    }

    @Override
    public Optional<Cart> getById(Long id) {
        return cartRepository.findById(id);
    }

    @Override
    public Cart create(Cart cart) {
        return cartRepository.save(cart);
    }

    @Override
    public Cart update(Long id, Cart cart) {
        return cartRepository.findById(id)
                .map(existing -> {
                    existing.setTotalPrice(cart.getTotalPrice());
                    existing.setCustomer(cart.getCustomer());
                    existing.setItems(cart.getItems());
                    return cartRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    @Override
    public void delete(Long id) {
        cartRepository.deleteById(id);
    }

    // =========================================================================
    // 🔥 LOGIC MỚI 1: TÌM GIỎ HÀNG THEO ACCOUNT ID (Để hiển thị lên Frontend)
    // =========================================================================
    @Override
    @Transactional
    public Cart getCartByAccountId(Long accountId) {
        // 1. Tìm Customer dựa trên Account ID
        Customer customer = customerRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng với Account ID: " + accountId));

        // 2. Tìm Giỏ hàng của Customer này.
        // Nếu chưa có -> Tự động tạo mới và trả về giỏ rỗng (Tránh lỗi null bên Frontend)
        return cartRepository.findByCustomer_Id(customer.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    newCart.setTotalPrice(BigDecimal.ZERO);
                    return cartRepository.save(newCart);
                });
    }

    // =========================================================================
    // 🔥 LOGIC MỚI 2: THÊM VÀO GIỎ HÀNG
    // =========================================================================
    @Override
    @Transactional
    public Cart addToCart(Long accountId, Long variantId, int quantity) {
        // BƯỚC 1: Tìm Customer
        Customer customer = customerRepository.findByAccount_Id(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng liên kết với tài khoản này!"));

        // BƯỚC 2: Tìm hoặc Tạo Giỏ hàng
        Cart cart = cartRepository.findByCustomer_Id(customer.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    newCart.setTotalPrice(BigDecimal.ZERO);
                    return cartRepository.save(newCart);
                });

        // BƯỚC 3: Tìm Sản phẩm (Biến thể)
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm (biến thể) không tồn tại hoặc đã bị xóa!"));

        // BƯỚC 4: Kiểm tra trùng lặp và cập nhật
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductVariant().getId().equals(variantId))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            // Đã có -> Cộng dồn
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);

            BigDecimal newSubTotal = variant.getPrice().multiply(BigDecimal.valueOf(existingItem.getQuantity()));
            existingItem.setSubTotal(newSubTotal);

            cartItemRepository.save(existingItem);
        } else {
            // Chưa có -> Tạo mới
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductVariant(variant);
            newItem.setQuantity(quantity);

            BigDecimal subTotal = variant.getPrice().multiply(BigDecimal.valueOf(quantity));
            newItem.setSubTotal(subTotal);

            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        // BƯỚC 5: Cập nhật tổng tiền giỏ hàng
        updateCartTotal(cart);

        return cartRepository.save(cart);
    }

    // Hàm phụ tính tổng tiền
    private void updateCartTotal(Cart cart) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            if (item.getSubTotal() != null) {
                total = total.add(item.getSubTotal());
            }
        }
        cart.setTotalPrice(total);
    }
}