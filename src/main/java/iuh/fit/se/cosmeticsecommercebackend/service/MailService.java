package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MailService {

    private final JavaMailSender javaMailSender;

    // Inject JavaMailSender vào constructor
    @Autowired
    public MailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }
    private final String SENDER_EMAIL = "huynhlehoan151@gmail.com";
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

//    [THÊM MỚI] Gửi email thông báo cho USER khi tài khoản bị vô hiệu hoá
@Async
    public void sendAccountDisabledEmail(String toEmail, String fullName, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(SENDER_EMAIL);
        message.setTo(toEmail);
        message.setSubject("THÔNG BÁO: TÀI KHOẢN ĐÃ BỊ VÔ HIỆU HÓA | EMBROSIA");

        String content = "Xin chào " + fullName + ",\n\n" +
                "Tài khoản của bạn tại hệ thống Embrosia đã bị vô hiệu hóa bởi quản trị viên.\n\n" +
                "LÝ DO: " + reason + "\n\n" +
                "Nếu bạn cho rằng đây là sự nhầm lẫn, vui lòng liên hệ bộ phận Chăm sóc khách hàng để được hỗ trợ.\n" +
                "Trân trọng,\n" +
                "Đội ngũ Embrosia.";

        message.setText(content);

        try {
            javaMailSender.send(message);
            System.out.println("Email thông báo khóa tài khoản đã gửi đến: " + toEmail);
        } catch (MailException e) {
            System.err.println("Lỗi gửi email khóa tài khoản: " + e.getMessage());
        }
    }

    /**
     * 3. [THÊM MỚI] Gửi email cảnh báo cho ADMIN khi có tài khoản bị khoá
     */
    @Async
    public void sendAdminAlertEmail(String adminEmail, Account lockedAccount, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(SENDER_EMAIL);
        message.setTo(adminEmail);
        message.setSubject("[ALERT] TÀI KHOẢN ĐÃ BỊ KHÓA: " + lockedAccount.getUsername());

        String content = "HỆ THỐNG GHI NHẬN HÀNH ĐỘNG KHÓA TÀI KHOẢN:\n\n" +
                "- Tài khoản bị khóa: " + lockedAccount.getUsername() + " (ID: " + lockedAccount.getId() + ")\n" +
                "- Tên người dùng: " + lockedAccount.getFullName() + "\n" +
                "- Lý do khóa: " + reason + "\n" +
                "- Thời gian thực hiện: " + LocalDateTime.now() + "\n\n" +
                "Vui lòng kiểm tra lại nếu cần thiết.";

        message.setText(content);

        try {
            javaMailSender.send(message);
            System.out.println("Email cảnh báo Admin đã gửi đến: " + adminEmail);
        } catch (MailException e) {
            System.err.println("Lỗi gửi email Admin: " + e.getMessage());
        }
    }
    @Async
    public void sendSecurityWarningToUser(String toEmail, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(SENDER_EMAIL);
        message.setTo(toEmail);
        message.setSubject("⚠️ CẢNH BÁO BẢO MẬT: ĐĂNG NHẬP THẤT BẠI NHIỀU LẦN");

        String content = "Xin chào " + (fullName != null ? fullName : "Quý khách") + ",\n\n" +
                "Hệ thống phát hiện tài khoản của bạn vừa có hơn 5 lần đăng nhập thất bại liên tiếp.\n" +
                "Nếu không phải bạn thực hiện, vui lòng đổi mật khẩu ngay lập tức hoặc liên hệ Admin để khóa tài khoản tạm thời.\n\n" +
                "IP truy cập: (Hệ thống ghi nhận)\n" +
                "Thời gian: " + java.time.LocalDateTime.now() + "\n\n" +
                "Trân trọng,\nĐội ngũ Embrosia.";

        message.setText(content);
        try {
            javaMailSender.send(message);
        } catch (Exception e) {
            System.err.println("Lỗi gửi mail user warning: " + e.getMessage());
        }
    }
    /**
     *  Gửi email tự động phản hồi khi khách hàng điền form Liên hệ
     */
    @Async
    public void sendContactAutoReply(String toEmail, String customerName) {
        SimpleMailMessage message = new SimpleMailMessage();

        // Dùng lại email người gửi đã cấu hình sẵn
        message.setFrom(SENDER_EMAIL);
        message.setTo(toEmail);
        message.setSubject("XÁC NHẬN: CHÚNG TÔI ĐÃ NHẬN ĐƯỢC CÂU HỎI CỦA BẠN | EMBROSIA");

        String content = "Chào " + customerName + ",\n\n" +
                "Cảm ơn bạn đã liên hệ với Embrosia Cosmetic.\n" +
                "Chúng tôi đã nhận được tin nhắn của bạn và sẽ phản hồi trong thời gian sớm nhất (thường trong vòng 24h làm việc).\n\n" +
                "Trong lúc chờ đợi, bạn có thể tham khảo các câu hỏi thường gặp tại website hoặc gọi hotline nếu cần hỗ trợ gấp.\n\n" +
                "Trân trọng,\n" +
                "Đội ngũ Chăm sóc khách hàng Embrosia.";

        message.setText(content);

        try {
            javaMailSender.send(message);
            System.out.println("Auto-reply contact email sent to: " + toEmail);
        } catch (MailException e) {
            System.err.println("Lỗi gửi email auto-reply: " + e.getMessage());
        }
    }
}