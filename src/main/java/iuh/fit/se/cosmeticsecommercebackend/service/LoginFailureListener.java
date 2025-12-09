package iuh.fit.se.cosmeticsecommercebackend.service; // Để chung package service

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
public class LoginFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    @Autowired
    private RiskService riskService; // Gọi thằng RiskService ra để ghi sổ

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        // 1. Lấy cái username vừa nhập sai
        String username = (String) event.getAuthentication().getPrincipal();

        // 2. Báo cho RiskService biết: "Thằng này vừa sai pass!"
        // (RiskService sẽ tự lo việc đếm 5 lần rồi gửi mail)
        riskService.recordLoginFail(username);

        System.out.println("Listener đã bắt được lỗi đăng nhập của: " + username);
    }
}