package iuh.fit.se.cosmeticsecommercebackend.payload;

public class CustomerIdResponse {
    private Long id;

    public CustomerIdResponse() {
    }

    public CustomerIdResponse(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
