package iuh.fit.se.cosmeticsecommercebackend.payload;

import iuh.fit.se.cosmeticsecommercebackend.model.enums.AccountRole;

public class JwtResponse {
    private String token;
    private String fullName;
    private AccountRole role;

    public JwtResponse() {
    }

    public JwtResponse(String token, String fullName, AccountRole role) {
        this.token = token;
        this.fullName = fullName;
        this.role = role;
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

    public AccountRole getRole() {
        return role;
    }

    public void setRole(AccountRole role) {
        this.role = role;
    }
}
