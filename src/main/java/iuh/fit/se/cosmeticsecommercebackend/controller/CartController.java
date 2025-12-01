package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.model.Cart;
import iuh.fit.se.cosmeticsecommercebackend.payload.AddToCartRequest;
import iuh.fit.se.cosmeticsecommercebackend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carts")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Cart>> getAllCarts() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cart> getCartById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Cart> createCart(@RequestBody Cart cart) {
        return ResponseEntity.ok(service.create(cart));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cart> updateCart(@PathVariable Long id, @RequestBody Cart cart) {
        return ResponseEntity.ok(service.update(id, cart));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCart(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request) {
        try {
            // Gọi hàm service bạn vừa khai báo
            Cart updatedCart = service.addToCart(
                    request.getAccountId(),
                    request.getVariantId(),
                    request.getQuantity()
            );
            return ResponseEntity.ok(updatedCart);
        } catch (Exception e) {
            // Trả về lỗi nếu không tìm thấy sản phẩm/user...
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/user/{accountId}")
    public ResponseEntity<Cart> getCartByAccountId(@PathVariable Long accountId) {
        try {
            // Gọi hàm getCartByAccountId mà bạn đã viết trong ServiceImpl
            Cart cart = service.getCartByAccountId(accountId);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
