package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.Account;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import iuh.fit.se.cosmeticsecommercebackend.model.Order;
import iuh.fit.se.cosmeticsecommercebackend.model.OrderDetail;
import iuh.fit.se.cosmeticsecommercebackend.model.Address; // Import Address
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.math.BigDecimal;

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

    /**
     * [UPDATE] Gửi email xác nhận đơn hàng định dạng HTML đẹp mắt
     */
    @Async
    public void sendOrderConfirmationEmail(String toEmail, Order order) {
        if (toEmail == null || toEmail.isEmpty()) return;

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(SENDER_EMAIL);
            helper.setTo(toEmail);
            helper.setSubject("XÁC NHẬN ĐƠN HÀNG #" + order.getId() + " | EMBROSIA");

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

            // ======================= BUILD HTML =======================
            StringBuilder html = new StringBuilder();

            html.append("<div style='font-family:Arial, sans-serif; font-size:14px; color:#333;'>");

            html.append("<h2 style='color:#444;'>Xác nhận đơn hàng #" + order.getId() + "</h2>");
            html.append("<p>Xin chào,<br>Đơn hàng của bạn đã được ghi nhận thành công!</p>");

            html.append("<p><strong>Mã đơn hàng:</strong> " + order.getId() + "<br>");
            html.append("<strong>Ngày đặt:</strong> " + order.getOrderDate() + "</p>");

            html.append("<hr style='margin:16px 0;'>");

            // ======================= PRODUCT LIST =======================
            html.append("<h3 style='margin-bottom:8px;'>Sản phẩm đã đặt:</h3>");
            html.append("<table style='width:100%; border-collapse:collapse;'>");

            for (OrderDetail detail : order.getOrderDetails()) {
                String productName = "Sản phẩm";

                if (detail.getProductVariant() != null && detail.getProductVariant().getProduct() != null) {
                    productName = detail.getProductVariant().getProduct().getName();
                }

                BigDecimal price = detail.getUnitPrice();
                BigDecimal quantity = new BigDecimal(detail.getQuantity());
                BigDecimal lineTotal = price.multiply(quantity);

                html.append("<tr>");
                html.append("<td style='padding:6px 0;'>" + productName + " (x" + detail.getQuantity() + ")</td>");
                html.append("<td style='text-align:right; padding:6px 0;'>" + currencyFormat.format(lineTotal) + "</td>");
                html.append("</tr>");
            }

            html.append("</table>");

            html.append("<hr style='margin:16px 0;'>");

            // ======================= TOTAL =======================
            html.append("<p><strong>Tổng cộng:</strong> " + currencyFormat.format(order.getTotal()) + "</p>");

            html.append("<hr style='margin:16px 0;'>");

            // ======================= ADDRESS =======================
            html.append("<h3>Địa chỉ giao hàng</h3>");
            if (order.getAddress() != null) {
                String address = (order.getAddress().getAddress() != null) ? order.getAddress().getAddress() : "";
                String city = (order.getAddress().getCity() != null) ? order.getAddress().getCity() : "";

                html.append("<p>" + address);
                if (!city.isEmpty()) html.append(", " + city);
                html.append("</p>");
            } else {
                html.append("<p>(Theo thông tin đã đăng ký)</p>");
            }

            html.append("<br><p>Cảm ơn bạn đã mua sắm tại <strong>Embro­sia</strong>!</p>");
            html.append("</div>");

            helper.setText(html.toString(), true);

            javaMailSender.send(mimeMessage);
            System.out.println("Đã gửi mail HTML order tới: " + toEmail);

        } catch (Exception e) {
            System.err.println("Lỗi gửi mail HTML: " + e.getMessage());
        }
    }


}