package iuh.fit.se.cosmeticsecommercebackend.model.enums;
/**
 * Các loại voucher
 */
public enum VoucherType {
    /**
     * giảm % trên subtotal đủ điều kiện
     */
    PERCENT,
    /**
     * giảm số tiền cố định
     */
    AMOUNT,
    /**
     * free ship (phí vận chuyển = 0)
     */
    SHIPPING_FREE,
}
