package iuh.fit.se.cosmeticsecommercebackend.payload;

import lombok.Data;

@Data
public class AddToCartRequest {
    private Long accountId;
    private Long variantId;
    private int quantity;
}
