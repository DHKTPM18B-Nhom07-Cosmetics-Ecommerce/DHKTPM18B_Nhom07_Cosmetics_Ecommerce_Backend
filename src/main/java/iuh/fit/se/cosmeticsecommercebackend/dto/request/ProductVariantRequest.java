package iuh.fit.se.cosmeticsecommercebackend.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

public class ProductVariantRequest {
    private String variantName;
    private BigDecimal price;
    private Integer quantity;
    private Integer sold;
    private List<String> imageUrls;

    public Integer getSold() {
        return sold;
    }

    public void setSold(Integer sold) {
        this.sold = sold;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
