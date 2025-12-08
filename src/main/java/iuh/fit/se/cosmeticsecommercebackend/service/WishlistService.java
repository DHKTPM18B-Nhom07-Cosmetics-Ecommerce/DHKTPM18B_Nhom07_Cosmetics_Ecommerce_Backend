package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.ProductVariant;

import java.util.List;

/**
 * Service interface cho Wishlist
 * Định nghĩa các phương thức xử lý danh sách yêu thích
 */
public interface WishlistService {

    /**
     * Thêm sản phẩm vào danh sách yêu thích
     * @param accountId ID của tài khoản
     * @param productVariantId ID của biến thể sản phẩm
     */
    void addToWishlist(Long accountId, Long productVariantId);

    /**
     * Xóa sản phẩm khỏi danh sách yêu thích
     * @param accountId ID của tài khoản
     * @param productVariantId ID của biến thể sản phẩm
     */
    void removeFromWishlist(Long accountId, Long productVariantId);

    /**
     * Lấy danh sách tất cả sản phẩm yêu thích
     * @param accountId ID của tài khoản
     * @return Danh sách ProductVariant
     */
    List<ProductVariant> getWishlist(Long accountId);

    /**
     * Kiểm tra sản phẩm có trong wishlist không
     * @param accountId ID của tài khoản
     * @param productVariantId ID của biến thể sản phẩm
     * @return true nếu có, false nếu không
     */
    boolean isInWishlist(Long accountId, Long productVariantId);

    /**
     * Xóa toàn bộ wishlist
     * @param accountId ID của tài khoản
     */
    void clearWishlist(Long accountId);
}
