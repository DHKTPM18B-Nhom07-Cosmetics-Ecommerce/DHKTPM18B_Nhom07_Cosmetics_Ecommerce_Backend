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
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired // Thêm Autowired này để truy cập thông tin đầy đủ
    private AccountRepository accountRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        try {
            // Thực hiện xác thực (Spring Security sẽ ném ngoại lệ nếu thất bại)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            // Đặt Authentication vào Security Context (Bắt buộc)
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 1. Lấy UserDetails
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtService.generateToken(userDetails);

            // 2. LẤY TÊN ĐẦY ĐỦ TỪ CSDL
            String username = userDetails.getUsername();
            Account account = accountRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Lỗi logic: Không tìm thấy tài khoản sau khi xác thực thành công."));

            // 3. Trả về Token và Tên đầy đủ
            return ResponseEntity.ok(new JwtResponse(jwt, account.getFullName()));

        } catch (BadCredentialsException | UsernameNotFoundException e) {
            // Bắt lỗi: Sai mật khẩu hoặc không tìm thấy tên đăng nhập/email
            // Trả về 401 Unauthorized
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Tên đăng nhập hoặc mật khẩu không đúng"));
        } catch (DisabledException e) {
            // Bắt lỗi: Tài khoản bị vô hiệu hóa (Status = DISABLED)
            // Trả về 401 Unauthorized
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên."));
        } catch (Exception e) {
            // Bắt các lỗi không xác định khác (lỗi code, lỗi service,...)
            System.err.println("Lỗi xác thực không xác định: " + e.getMessage());

            // Trả về lỗi 500
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Đăng nhập thất bại do lỗi hệ thống."));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(@Validated @RequestBody RegisterRequest request) {
        try {
            // 1. Gọi Service để xử lý logic đăng ký
            Account newAccount = authService.registerCustomer(request);

            // 2. (Tùy chọn) Tự động đăng nhập sau khi đăng ký thành công:
            // Bạn có thể gọi AuthenticationManager ở đây và trả về JWT,
            // nhưng để đơn giản, chúng ta sẽ chỉ trả về thành công 201 Created.

            // Trả về thông tin cơ bản
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new AccountInfoResponse(newAccount.getFullName(), newAccount.getUsername()));

        } catch (IllegalArgumentException e) {
            // Bắt lỗi email đã tồn tại (từ Service)
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST) // 400 Bad Request
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            // Lỗi khác (ví dụ: lỗi CSDL)
            System.err.println("Registration Error: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Đăng ký thất bại do lỗi hệ thống."));
        }
    }

    /**
     * QUÊN MẬT KHẨU: ENDPOINT 1 - Yêu cầu gửi Token qua Email
     * @param request Chứa email
     */
    @PostMapping("/forgot-password-request")
    public ResponseEntity<?> forgotPasswordRequest(@Validated @RequestBody ForgotPasswordRequest request) {
        try {
            authService.createPasswordResetToken(request);

            // Luôn trả về 200 OK ngay cả khi email không tồn tại (để tránh rò rỉ thông tin)
            return ResponseEntity.ok(new SuccessResponse("Mã xác nhận đã được gửi. Vui lòng kiểm tra hộp thư."));
        } catch (IllegalArgumentException e) {
            // Nếu xảy ra lỗi tìm thấy email (từ service), vẫn trả về thông báo chung
            return ResponseEntity.ok(new SuccessResponse("Mã xác nhận đã được gửi. Vui lòng kiểm tra hộp thư."));
        } catch (Exception e) {
            System.err.println("Forgot Password Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Lỗi hệ thống khi xử lý yêu cầu."));
        }
    }

    /**
     * QUÊN MẬT KHẨU: ENDPOINT 2 - Đặt lại mật khẩu bằng Token
     * @param request Chứa token và mật khẩu mới
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Validated @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);

            return ResponseEntity.ok(new SuccessResponse("Đặt lại mật khẩu thành công. Bạn có thể đăng nhập bằng mật khẩu mới."));

        } catch (IllegalArgumentException e) {
            // Bắt lỗi Token không hợp lệ hoặc hết hạn (từ AuthService)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            System.err.println("Reset Password Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Lỗi hệ thống khi đặt lại mật khẩu."));
        }
    }

    // DTO đơn giản cho phản hồi thành công
    private record SuccessResponse(String message) {}
}