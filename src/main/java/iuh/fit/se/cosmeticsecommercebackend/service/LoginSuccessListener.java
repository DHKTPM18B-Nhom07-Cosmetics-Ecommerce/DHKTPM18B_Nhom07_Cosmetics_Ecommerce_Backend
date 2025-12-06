package iuh.fit.se.cosmeticsecommercebackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class LoginSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    @Autowired
    private RiskService riskService;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        // Lấy thông tin người vừa đăng nhập thành công
        Object principal = event.getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();

            // Báo RiskService: "Nó nhập đúng rồi, xóa án tích cho nó"
            riskService.clearLoginFail(username);
        }
    }
}