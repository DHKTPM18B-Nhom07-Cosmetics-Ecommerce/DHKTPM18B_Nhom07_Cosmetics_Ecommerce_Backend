package iuh.fit.se.cosmeticsecommercebackend.exception;

/**
 * Ngoại lệ dùng để ném ra khi không tìm thấy tài nguyên (Entity).
 * Ví dụ: Customer không tồn tại, Order không tồn tại, v.v.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
