package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.Account;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.OrderStatus;
import iuh.fit.se.cosmeticsecommercebackend.repository.AccountRepository;
import iuh.fit.se.cosmeticsecommercebackend.repository.OrderRepository;
import jakarta.annotation.PostConstruct; // [QUAN TRỌNG]
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RiskService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AccountRepository accountRepository; // Để quét lại toàn bộ user

    @Autowired
    private MailService mailService;

    // Bộ nhớ tạm
    private final Map<String, Integer> loginFailures = new ConcurrentHashMap<>();
    private final List<SystemAlert> recentAlerts = new CopyOnWriteArrayList<>();
    private final String ADMIN_EMAIL = "huynhlehoan151@gmail.com";

    // =========================================================================
    // [TÍNH NĂNG MỚI]: QUÉT LẠI DỮ LIỆU CŨ KHI SERVER KHỞI ĐỘNG
    // Giúp cái chuông không bị mất dữ liệu khi restart
    // =========================================================================
    @PostConstruct
    public void scanHistoricalRisks() {
        System.out.println(">>> ĐANG QUÉT LẠI DỮ LIỆU RỦI RO CŨ...");
        List<Account> accounts = accountRepository.findAll();

        for (Account acc : accounts) {
            // Chỉ quét tài khoản đang hoạt động
            if (acc.getStatus() == iuh.fit.se.cosmeticsecommercebackend.model.enums.AccountStatus.ACTIVE) {
                // Check spam đơn
                RiskReport report = analyzeRisk(acc.getId(), acc.getUsername());
                if (report.level.equals("HIGH")) {
                    // Nếu thấy rủi ro -> Nạp lại vào chuông ngay lập tức
                    System.out.println(">>> Khôi phục cảnh báo cho: " + acc.getUsername());

                    // Loại cảnh báo tùy theo note
                    String type = report.note.contains("hủy") ? "SPAM ĐƠN" : "BẢO MẬT";
                    addAlert(type, "Cảnh báo cũ: " + report.note, "HIGH", acc.getId());
                }
            }
        }
    }

    // =========================================================================
    // CÁC HÀM XỬ LÝ SỰ KIỆN (GIỮ NGUYÊN NHƯ CŨ)
    // =========================================================================

    public void recordLoginFail(String username) {
        loginFailures.merge(username, 1, Integer::sum);
        int fails = loginFailures.get(username);

        // Logic cũ: Sai >= 5 lần
        if (fails >= 5 && fails % 5 == 0) {
            String msg = "Tài khoản " + username + " sai mật khẩu " + fails + " lần liên tiếp.";

            // Tìm ID để gắn link
            Long targetId = null;
            try {
                Account acc = accountRepository.findByUsername(username).orElse(null);
                if (acc != null) targetId = acc.getId();
            } catch (Exception e) {}

            addAlert("BẢO MẬT", msg, "HIGH", targetId);

            try {
                mailService.sendAdminAlertEmail(ADMIN_EMAIL, null, msg);
                if (username.contains("@")) mailService.sendSecurityWarningToUser(username, username);
            } catch (Exception e) {
                System.err.println("Lỗi mail login: " + e.getMessage());
            }
        }
    }

    public void checkAndAlertOrderSpam(Long accountId, String username) {
        try {
            LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
            long cancelCount = orderRepository.countOrdersByStatusAndDate(
                    accountId, OrderStatus.CANCELLED, oneDayAgo
            );

            System.out.println("LOG CHECK SPAM: " + username + " = " + cancelCount);

            if (cancelCount >= 5) {
                String warning = "User " + username + " hủy đơn thứ " + cancelCount + " trong 24h.";
                addAlert("SPAM ĐƠN", warning, "HIGH", accountId);

                try {
                    mailService.sendAdminAlertEmail(ADMIN_EMAIL, null, warning);
                } catch (Exception e) {
                    System.err.println("Lỗi mail spam: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi DB: " + e.getMessage());
        }
    }

    // =========================================================================
    // CÁC HÀM PHỤ TRỢ
    // =========================================================================

    private void addAlert(String type, String message, String level, Long targetId) {
        // Thêm vào đầu danh sách
        recentAlerts.add(0, new SystemAlert(type, message, level, LocalDateTime.now(), targetId));
        // Giới hạn 20 thông báo
        if (recentAlerts.size() > 20) recentAlerts.remove(recentAlerts.size() - 1);
    }

    public List<SystemAlert> getAlerts() {
        return recentAlerts;
    }

    public void clearLoginFail(String username) { loginFailures.remove(username); }

    public RiskReport analyzeRisk(Long accountId, String username) {
        String level = "NORMAL"; String note = "";
        try {
            LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
            long count = orderRepository.countOrdersByStatusAndDate(accountId, OrderStatus.CANCELLED, oneDayAgo);
            if (count >= 5) { level = "HIGH"; note = "Spam hủy " + count + " đơn/24h"; }
        } catch (Exception e) {}

        int fails = loginFailures.getOrDefault(username, 0);
        if (fails >= 5) {
            level = "HIGH";
            note = (note.isEmpty() ? "" : note + ". ") + "Sai mật khẩu " + fails + " lần";
        }
        return new RiskReport(level, note);
    }

    // DTO
    public static class SystemAlert {
        public String type, message, level;
        public LocalDateTime time;
        public Long targetId;

        public SystemAlert(String t, String m, String l, LocalDateTime time, Long tid) {
            this.type = t; this.message = m; this.level = l; this.time = time; this.targetId = tid;
        }
    }
    public static class RiskReport {
        public String level, note;
        public RiskReport(String l, String n) { level = l; note = n; }
    }
}