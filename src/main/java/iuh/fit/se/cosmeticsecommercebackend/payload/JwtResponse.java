package iuh.fit.se.cosmeticsecommercebackend.payload;

import iuh.fit.se.cosmeticsecommercebackend.model.enums.AccountRole;

public class JwtResponse {
    private String token;
    private String fullName;
    private AccountRole role;
    private Long id; // <--- Đảm bảo có dòng này

    public JwtResponse() {
    }


        // --- ĐÂY LÀ CONSTRUCTOR MÀ LỖI ĐANG BÁO THIẾU ---
        public JwtResponse(String token, String fullName, Long id, AccountRole role) {
            this.token = token;
            this.fullName = fullName;
            this.role = role;
            this.id = id;
            }

            public String getToken () {
                return token;
            }

            public void setToken (String token){
                this.token = token;
            }

            public String getFullName () {
                return fullName;
            }

            public void setFullName (String fullName){
                this.fullName = fullName;
            }

            public Long getId () {
                return id;
            }

            public void setId (Long id){
                this.id = id;
            }

        public AccountRole getRole() {
            return role;
        }

        public void setRole(AccountRole role) {
            this.role = role;
        }
    }
