package iuh.fit.se.cosmeticsecommercebackend.model.enums;

/**
 * Enum đại diện cho trạng thái đơn hàng
 */
public enum OrderStatus {
    /**
     * Đơn hàng đang chờ xác nhận
     */
    PENDING,
    
    /**
     * Đơn hàng đã được xác nhận
     */
    CONFIRMED,
    
    /**
     * Đang xử lý/đóng gói
     */
    PROCESSING,
    
    /**
     * Đang vận chuyển
     */
    SHIPPING,
    
    /**
     * Đã giao hàng thành công
     */
    DELIVERED,
    
    /**
     * Đơn hàng bị hủy
     */
    CANCELLED,
    
    /**
     * Đơn hàng bị hoàn trả
     */
    RETURNED,
    
    /**
     * Đơn hàng hoàn tiền
     */
    REFUNDED
}

