package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.model.CartItem;
import iuh.fit.se.cosmeticsecommercebackend.service.CartItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart-items")
@CrossOrigin(origins = "*")
public class CartItemController {

    private final CartItemService cartItemService;

    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @GetMapping
    public List<CartItem> getAll() {
        return cartItemService.getAllCartItems();
    }

    @GetMapping("/{id}")
    public CartItem getById(@PathVariable Long id) {
        return cartItemService.getCartItemById(id);
    }

    @GetMapping("/cart/{cartId}")
    public List<CartItem> getByCart(@PathVariable Long cartId) {
        return cartItemService.getCartItemsByCartId(cartId);
    }

    @PostMapping
    public CartItem create(@RequestBody CartItem cartItem) {
        return cartItemService.createCartItem(cartItem);
    }

    // --- SỬA HÀM UPDATE NÀY ---
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        try {
            Integer newQuantity = request.get("quantity");
            if (newQuantity == null || newQuantity < 1) {
                return ResponseEntity.badRequest().body("Số lượng không hợp lệ");
            }

            // 1. Lấy item cũ từ DB lên
            CartItem existingItem = cartItemService.getCartItemById(id);

            // 2. Cập nhật số lượng mới vào object này
            existingItem.setQuantity(newQuantity);

            // 3. Gọi service để tính toán lại tiền và lưu
            CartItem updatedItem = cartItemService.updateCartItem(id, existingItem);

            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        cartItemService.deleteCartItem(id);
    }
}