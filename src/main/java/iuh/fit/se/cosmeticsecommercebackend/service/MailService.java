package iuh.fit.se.cosmeticsecommercebackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender javaMailSender;

    // Inject JavaMailSender vào constructor
    @Autowired
    public MailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    /**
     * Gửi email xác nhận Token đặt lại mật khẩu
     * @param toEmail Địa chỉ email nhận
     * @param token Token ngẫu nhiên (hoặc OTP)
     */
    public void sendResetPasswordEmail(String toEmail, String token) {

        SimpleMailMessage message = new SimpleMailMessage();

        // Thiết lập tiêu đề và nội dung email
        message.setFrom("mannghi707@gmail.com");
        message.setTo(toEmail);
        message.setSubject("YÊU CẦU ĐẶT LẠI MẬT KHẨU | EMBROSIA");

        String resetLink = "Mã xác thực của bạn là: " + token +
                "\n\nMã này chỉ có hiệu lực trong 5 phút. Vui lòng chọn xác nhận để chuyển đến trang đặt lại mật khẩu và nhập mã này để hoàn tất." +
                "\n\nNếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.";

        message.setText(resetLink);

        try {
            javaMailSender.send(message);
            System.out.println("Email xác nhận đã được gửi thành công đến: " + toEmail);
        } catch (MailException e) {
            System.err.println("Lỗi gửi email đến " + toEmail + ": " + e.getMessage());
            // throw new RuntimeException("Không thể gửi email xác thực.", e);
        }
    }
}