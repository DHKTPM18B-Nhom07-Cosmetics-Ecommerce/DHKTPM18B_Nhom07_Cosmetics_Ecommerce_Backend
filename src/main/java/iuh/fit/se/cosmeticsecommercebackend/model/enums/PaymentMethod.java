package iuh.fit.se.cosmeticsecommercebackend.model.enums;

/**
 * Enum đại diện cho phương thức thanh toán
 */
public enum PaymentMethod {
    /**
     * Thanh toán khi nhận hàng
     */
    COD,
    
    /**
     * Thanh toán qua thẻ tín dụng/ghi nợ
     */
    CREDIT_CARD,
    
    /**
     * Thanh toán qua ví điện tử
     */
    E_WALLET,
    
    /**
     * Chuyển khoản ngân hàng
     */
    BANK_TRANSFER,
    
    /**
     * Thanh toán qua PayPal
     */
    PAYPAL,
    
    /**
     * Thanh toán qua MoMo
     */
    MOMO,
    
    /**
     * Thanh toán qua ZaloPay
     */
    ZALOPAY,
    
    /**
     * Thanh toán qua VNPay
     */
    VNPAY
}

