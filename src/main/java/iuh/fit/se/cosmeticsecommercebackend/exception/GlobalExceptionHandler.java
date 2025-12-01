package iuh.fit.se.cosmeticsecommercebackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =============================
    // NOT FOUND
    // =============================
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Không tìm thấy tài nguyên",
                ex.getMessage()
        );
    }

    // =============================
    // BAD STATE (Lỗi nghiệp vụ)
    // =============================
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Trạng thái không hợp lệ",
                ex.getMessage()
        );
    }

    // =============================
    // BAD ARGUMENT (validate input)
    // =============================
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Tham số không hợp lệ",
                ex.getMessage()
        );
    }

    // =============================
    // RUNTIME EXCEPTION (Service throw)
    // =============================
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Yêu cầu không hợp lệ",
                ex.getMessage()
        );
    }

    // =============================
    // OTHER UNEXPECTED EXCEPTIONS
    // =============================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Lỗi hệ thống",
                ex.getMessage()
        );
    }

    // =============================
    // RESPONSE TEMPLATE
    // =============================
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);

        return ResponseEntity.status(status).body(body);
    }
}
