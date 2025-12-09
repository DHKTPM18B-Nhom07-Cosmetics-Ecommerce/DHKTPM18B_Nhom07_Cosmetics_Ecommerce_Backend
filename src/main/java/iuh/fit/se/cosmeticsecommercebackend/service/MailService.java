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
    /**
     * [FIXED] Gửi email HTML sử dụng MimeMessage (Thay vì SimpleMailMessage)
     */
    @Async
    public void sendOrderConfirmationEmail(String toEmail, Order order) {
        if (toEmail == null || toEmail.isEmpty()) return;

        // 1. Dùng MimeMessage để hỗ trợ HTML
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            // tham số true ở đây nghĩa là multipart (hỗ trợ file đính kèm/html)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(SENDER_EMAIL);
            helper.setTo(toEmail);
            helper.setSubject("✨ XÁC NHẬN ĐƠN HÀNG #" + order.getId() + " | EMBROSIA");

            // Format tiền tệ VNĐ
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            // 2. Xây dựng nội dung HTML
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><style>");
            html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
            html.append(".container { max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 8px; overflow: hidden; }");
            html.append(".header { background-color: #2E5F6D; color: #fff; padding: 20px; text-align: center; }"); // Màu xanh Embrosia
            html.append(".content { padding: 20px; background-color: #fff; }");
            html.append("table { width: 100%; border-collapse: collapse; margin-top: 10px; }");
            html.append("th { background-color: #f8f9fa; border-bottom: 2px solid #dee2e6; padding: 10px; text-align: left; font-size: 14px; }");
            html.append("td { border-bottom: 1px solid #dee2e6; padding: 10px; font-size: 14px; }");
            html.append(".total-row td { font-weight: bold; color: #2E5F6D; }");
            html.append(".footer { background-color: #f8f9fa; padding: 15px; text-align: center; font-size: 12px; color: #666; }");
            html.append("</style></head><body>");

            html.append("<div class='container'>");

            // --- HEADER ---
            html.append("<div class='header'>");
            html.append("<h2 style='margin:0;'>XÁC NHẬN ĐƠN HÀNG</h2>");
            html.append("<p style='margin:5px 0 0;'>Mã đơn: #").append(order.getId()).append("</p>");
            html.append("</div>");

            // --- CONTENT ---
            html.append("<div class='content'>");

            // Lấy tên khách (Nếu có account lấy tên account, không thì lấy trong Customer)
            String cusName = "Quý khách";
            if (order.getCustomer() != null && order.getCustomer().getAccount() != null) {
                cusName = order.getCustomer().getAccount().getFullName();
            }
            html.append("<p>Xin chào <strong>").append(cusName).append("</strong>,</p>");
            html.append("<p>Cảm ơn bạn đã đặt hàng tại Embrosia. Đơn hàng của bạn đã được ghi nhận vào lúc ").append(order.getOrderDate().format(dateFormat)).append(".</p>");

            // Bảng sản phẩm
            html.append("<table>");
            html.append("<thead><tr><th>Sản phẩm</th><th style='text-align:center'>SL</th><th style='text-align:right'>Thành tiền</th></tr></thead>");
            html.append("<tbody>");

            for (OrderDetail detail : order.getOrderDetails()) {
                String productName = "Sản phẩm";
                String sku = "";
                // Null check kỹ càng
                if(detail.getProductVariant() != null && detail.getProductVariant().getProduct() != null) {
                    productName = detail.getProductVariant().getProduct().getName();
                    sku = detail.getProductVariant().getVariantName();
                }

                // Dùng getUnitPrice() chuẩn Entity
                BigDecimal price = detail.getUnitPrice();
                BigDecimal quantity = new BigDecimal(detail.getQuantity());
                BigDecimal lineTotal = price.multiply(quantity);

                html.append("<tr>");
                html.append("<td><strong>").append(productName).append("</strong><br><small style='color:#777'>SKU: ").append(sku).append("</small></td>");
                html.append("<td style='text-align:center'>").append(detail.getQuantity()).append("</td>");
                html.append("<td style='text-align:right'>").append(currencyFormat.format(lineTotal)).append("</td>");
                html.append("</tr>");
            }

            // Phí vận chuyển
            html.append("<tr>");
            html.append("<td colspan='2' style='text-align:right'>Phí vận chuyển:</td>");
            html.append("<td style='text-align:right'>").append(currencyFormat.format(order.getShippingFee())).append("</td>");
            html.append("</tr>");

            // Tổng tiền - Dùng getTotal() chuẩn Entity
            html.append("<tr class='total-row'>");
            html.append("<td colspan='2' style='text-align:right; font-size:16px;'>TỔNG THANH TOÁN:</td>");
            html.append("<td style='text-align:right; font-size:16px; color:#d63031;'>").append(currencyFormat.format(order.getTotal())).append("</td>");
            html.append("</tr>");

            html.append("</tbody></table>");

            // Thông tin giao hàng
            html.append("<div style='margin-top: 20px; padding: 15px; background-color: #fdfdfd; border: 1px dashed #ccc; border-radius: 5px;'>");
            html.append("<h4 style='margin-top:0; color:#2E5F6D;'>Thông tin nhận hàng</h4>");
            if (order.getAddress() != null) {
                // Dùng getter Address chuẩn
                html.append("<p style='margin:5px 0;'><strong>Người nhận:</strong> ").append(order.getAddress().getFullName()).append("</p>");
                html.append("<p style='margin:5px 0;'><strong>SĐT:</strong> ").append(order.getAddress().getPhone()).append("</p>");
                html.append("<p style='margin:5px 0;'><strong>Địa chỉ:</strong> ")
                        .append(order.getAddress().getAddress()).append(", ")
                        .append(order.getAddress().getCity()).append(", ")
                        .append(order.getAddress().getState())
                        .append("</p>");
            } else {
                html.append("<p>Theo thông tin tài khoản mặc định.</p>");
            }
            html.append("</div>");

            html.append("</div>"); // End content

            // --- FOOTER ---
            html.append("<div class='footer'>");
            html.append("<p>Mọi thắc mắc vui lòng liên hệ hotline: <strong>1900 123 456</strong></p>");
            html.append("<p>&copy; 2025 Embrosia Cosmetic. All rights reserved.</p>");
            html.append("</div>");

            html.append("</div></body></html>");

            // QUAN TRỌNG: set true để nhận dạng HTML
            helper.setText(html.toString(), true);

            javaMailSender.send(message);
            System.out.println("HTML Email đã gửi thành công tới: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Lỗi tạo mail HTML: " + e.getMessage());
        }
    }


}