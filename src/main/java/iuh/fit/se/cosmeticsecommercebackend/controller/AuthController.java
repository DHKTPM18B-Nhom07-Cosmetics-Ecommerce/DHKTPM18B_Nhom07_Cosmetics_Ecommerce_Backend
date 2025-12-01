package iuh.fit.se.cosmeticsecommercebackend.controller;

import iuh.fit.se.cosmeticsecommercebackend.model.Account;
import iuh.fit.se.cosmeticsecommercebackend.payload.*;
import iuh.fit.se.cosmeticsecommercebackend.service.AuthService;
import iuh.fit.se.cosmeticsecommercebackend.repository.AccountRepository;
import iuh.fit.se.cosmeticsecommercebackend.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
// @CrossOrigin(origins = "*") // Nên mở comment dòng này nếu bị lỗi CORS
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        try {
            // Thực hiện xác thực
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            // Đặt Authentication vào Security Context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 1. Lấy UserDetails để tạo Token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtService.generateToken(userDetails);

            // 2. LẤY THÔNG TIN ACCOUNT TỪ DB (Để lấy ID và FullName)
            String username = userDetails.getUsername();
            Account account = accountRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Lỗi logic: Không tìm thấy tài khoản sau khi xác thực thành công."));

            // 3. TRẢ VỀ TOKEN + FULLNAME + ID (QUAN TRỌNG: Đã thêm account.getId())
            // Lưu ý: Class JwtResponse phải có constructor nhận 3 tham số này (như đã sửa ở bước trước)
            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    account.getFullName(),
                    account.getId() // <--- THÊM ID VÀO ĐÂY
            ));

        } catch (BadCredentialsException | UsernameNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Tên đăng nhập hoặc mật khẩu không đúng"));
        } catch (DisabledException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Tài khoản của bạn đã bị vô hiệu hóa."));
        } catch (Exception e) {
            System.err.println("Lỗi xác thực: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Đăng nhập thất bại do lỗi hệ thống."));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(@Validated @RequestBody RegisterRequest request) {
        try {
            // 1. Gọi Service để xử lý logic đăng ký
            // LƯU Ý: Trong AuthService.registerCustomer bạn nhớ thêm logic tạo Customer như đã bàn nhé!
            Account newAccount = authService.registerCustomer(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new AccountInfoResponse(newAccount.getFullName(), newAccount.getUsername()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            System.err.println("Registration Error: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Đăng ký thất bại do lỗi hệ thống."));
        }
    }

    // ... Các API Quên mật khẩu giữ nguyên ...
    @PostMapping("/forgot-password-request")
    public ResponseEntity<?> forgotPasswordRequest(@Validated @RequestBody ForgotPasswordRequest request) {
        try {
            authService.createPasswordResetToken(request);
            return ResponseEntity.ok(new SuccessResponse("Mã xác nhận đã được gửi. Vui lòng kiểm tra hộp thư."));
        } catch (Exception e) {
            return ResponseEntity.ok(new SuccessResponse("Mã xác nhận đã được gửi. Vui lòng kiểm tra hộp thư."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Validated @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok(new SuccessResponse("Đặt lại mật khẩu thành công."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Lỗi hệ thống."));
        }
    }

    private record SuccessResponse(String message) {}
}