package iuh.fit.se.cosmeticsecommercebackend.dto.request;

import lombok.Data;
import java.util.List;

public class ProductRequest {
    private String name;
    private String description;
    private Long categoryId; // Chấp nhận ID trực tiếp
    private Long brandId;    // Chấp nhận ID trực tiếp
    private Boolean isActive; // Trạng thái hoạt động
    private List<String> images;
    private List<ProductVariantRequest> variants;
    
    // Hỗ trợ trường hợp frontend gửi object {id: ...} (fallback)
    public void setCategory(Object categoryObj) {
        if (categoryObj instanceof Number) {
            this.categoryId = ((Number) categoryObj).longValue();
        } else if (categoryObj instanceof java.util.Map) {
            java.util.Map<?, ?> map = (java.util.Map<?, ?>) categoryObj;
            Object idVal = map.get("id");
            if (idVal instanceof Number) {
                this.categoryId = ((Number) idVal).longValue();
            }
        }
    }

    public void setBrand(Object brandObj) {
        if (brandObj instanceof Number) {
            this.brandId = ((Number) brandObj).longValue();
        } else if (brandObj instanceof java.util.Map) {
            java.util.Map<?, ?> map = (java.util.Map<?, ?>) brandObj;
            Object idVal = map.get("id");
            if (idVal instanceof Number) {
                this.brandId = ((Number) idVal).longValue();
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<ProductVariantRequest> getVariants() {
        return variants;
    }

    public void setVariants(List<ProductVariantRequest> variants) {
        this.variants = variants;
    }
}
