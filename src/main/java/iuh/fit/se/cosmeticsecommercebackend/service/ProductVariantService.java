package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.ProductVariant;

import java.util.List;

public interface ProductVariantService {
    List<ProductVariant> getAll();
    ProductVariant getById(Long id);
    List<ProductVariant> getByProductId(Long productId);
    ProductVariant create(ProductVariant variant);
    ProductVariant update(Long id, ProductVariant variant);
    void delete(Long id);
    /**
     * Tăng tồn kho của biến thể sản phẩm sau khi đơn hàng bị hủy hoặc trả hàng.
     * @param variantId ID của biến thể sản phẩm
     * @param quantity Số lượng cần hoàn trả/tăng thêm
     */
    void increaseStock(Long variantId, int quantity);
}
