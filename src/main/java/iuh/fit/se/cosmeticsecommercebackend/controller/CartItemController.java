package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.model.CartItem;
import iuh.fit.se.cosmeticsecommercebackend.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart-items")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CartItemController {

    private final CartItemService cartItemService;

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

    @PutMapping("/{id}")
    public CartItem update(@PathVariable Long id, @RequestBody CartItem cartItem) {
        return cartItemService.updateCartItem(id, cartItem);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        cartItemService.deleteCartItem(id);
    }
}
