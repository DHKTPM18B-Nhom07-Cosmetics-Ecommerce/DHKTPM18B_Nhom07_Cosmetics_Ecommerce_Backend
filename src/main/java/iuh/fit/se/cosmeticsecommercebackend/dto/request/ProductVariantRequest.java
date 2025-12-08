package iuh.fit.se.cosmeticsecommercebackend.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductVariantRequest {
    private String variantName;
    private BigDecimal price;
    private Integer quantity;
    private List<String> imageUrls;
}
