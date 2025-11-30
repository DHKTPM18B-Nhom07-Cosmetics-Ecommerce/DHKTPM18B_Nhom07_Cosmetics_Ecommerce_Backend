package iuh.fit.se.cosmeticsecommercebackend.payload;

public class ErrorResponse {
    private String message; // Thông báo lỗi
    public ErrorResponse() {
    }
    public ErrorResponse(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
