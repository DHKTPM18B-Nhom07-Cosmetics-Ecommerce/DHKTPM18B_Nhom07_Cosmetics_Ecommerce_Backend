package iuh.fit.se.cosmeticsecommercebackend.payload;

public class JwtResponse {
    private String token;
    private String fullName;
    private Long id; // <--- Đảm bảo có dòng này

    public JwtResponse() {
    }

    // --- ĐÂY LÀ CONSTRUCTOR MÀ LỖI ĐANG BÁO THIẾU ---
    public JwtResponse(String token, String fullName, Long id) {
        this.token = token;
        this.fullName = fullName;
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}