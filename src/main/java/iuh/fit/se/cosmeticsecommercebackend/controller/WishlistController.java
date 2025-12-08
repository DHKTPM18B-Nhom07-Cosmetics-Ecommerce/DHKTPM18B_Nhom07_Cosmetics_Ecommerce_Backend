package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.model.ProductVariant;
import iuh.fit.se.cosmeticsecommercebackend.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller xử lý các API liên quan đến Wishlist (Danh sách yêu thích)
 * - Thêm sản phẩm vào wishlist
 * - Xóa sản phẩm khỏi wishlist
 * - Lấy danh sách sản phẩm yêu thích
 * - Kiểm tra sản phẩm có trong wishlist không
 */
@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "*")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    /**
     * Thêm sản phẩm vào danh sách yêu thích
     * @param accountId ID của tài khoản
     * @param productVariantId ID của biến thể sản phẩm
     * @return Thông báo thành công
     */
    @PostMapping("/add/{accountId}/{productVariantId}")
    public ResponseEntity<?> addToWishlist(
            @PathVariable Long accountId,
            @PathVariable Long productVariantId) {
        try {
            wishlistService.addToWishlist(accountId, productVariantId);
            return ResponseEntity.ok(Map.of(
                    "message", "Đã thêm sản phẩm vào danh sách yêu thích",
                    "success", true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Lỗi: " + e.getMessage(),
                    "success", false
            ));
        }
    }

    /**
     * Xóa sản phẩm khỏi danh sách yêu thích
     * @param accountId ID của tài khoản
     * @param productVariantId ID của biến thể sản phẩm
     * @return Thông báo thành công
     */
    @DeleteMapping("/remove/{accountId}/{productVariantId}")
    public ResponseEntity<?> removeFromWishlist(
            @PathVariable Long accountId,
            @PathVariable Long productVariantId) {
        try {
            wishlistService.removeFromWishlist(accountId, productVariantId);
            return ResponseEntity.ok(Map.of(
                    "message", "Đã xóa sản phẩm khỏi danh sách yêu thích",
                    "success", true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Lỗi: " + e.getMessage(),
                    "success", false
            ));
        }
    }

    /**
     * Lấy danh sách tất cả sản phẩm yêu thích của khách hàng
     * @param accountId ID của tài khoản
     * @return Danh sách ProductVariant
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<?> getWishlist(@PathVariable Long accountId) {
        try {
            List<ProductVariant> wishlist = wishlistService.getWishlist(accountId);
            return ResponseEntity.ok(wishlist);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Lỗi: " + e.getMessage(),
                    "success", false
            ));
        }
    }

    /**
     * Kiểm tra xem sản phẩm có trong wishlist không
     * @param accountId ID của tài khoản
     * @param productVariantId ID của biến thể sản phẩm
     * @return true nếu có, false nếu không
     */
    @GetMapping("/check/{accountId}/{productVariantId}")
    public ResponseEntity<?> checkInWishlist(
            @PathVariable Long accountId,
            @PathVariable Long productVariantId) {
        try {
            boolean isInWishlist = wishlistService.isInWishlist(accountId, productVariantId);
            return ResponseEntity.ok(Map.of(
                    "isInWishlist", isInWishlist
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Lỗi: " + e.getMessage(),
                    "success", false
            ));
        }
    }

    /**
     * Xóa toàn bộ danh sách yêu thích
     * @param accountId ID của tài khoản
     * @return Thông báo thành công
     */
    @DeleteMapping("/clear/{accountId}")
    public ResponseEntity<?> clearWishlist(@PathVariable Long accountId) {
        try {
            wishlistService.clearWishlist(accountId);
            return ResponseEntity.ok(Map.of(
                    "message", "Đã xóa toàn bộ danh sách yêu thích",
                    "success", true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Lỗi: " + e.getMessage(),
                    "success", false
            ));
        }
    }
}
