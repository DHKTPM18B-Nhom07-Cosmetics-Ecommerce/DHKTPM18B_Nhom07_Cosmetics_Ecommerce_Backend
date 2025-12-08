package iuh.fit.se.cosmeticsecommercebackend.controller; // Sửa package theo project của bạn

import iuh.fit.se.cosmeticsecommercebackend.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "*")
public class ContactController {

    @Autowired
    private MailService mailService;

    @PostMapping("/send")
    public ResponseEntity<String> submitContactForm(@RequestBody Map<String, String> request) {

        String name = request.get("name");
        String email = request.get("email");

        if (email != null && !email.isEmpty()) {
            mailService.sendContactAutoReply(email, name);
            return ResponseEntity.ok("Đã gửi tin nhắn và email xác nhận thành công!");
        } else {
            return ResponseEntity.badRequest().body("Email không hợp lệ");
        }
    }
}