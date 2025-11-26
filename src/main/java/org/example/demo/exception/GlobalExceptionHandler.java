package org.example.demo.exception;

import org.example.demo.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Global Exception Handler - Bắt TẤT CẢ exceptions từ mọi Controller
 * Trả về ErrorResponse đồng nhất cho tất cả lỗi
 */
@RestControllerAdvice  // = @ControllerAdvice + @ResponseBody, tự động convert sang JSON
public class GlobalExceptionHandler {

    /**
     * Xử lý ResourceNotFoundException (404)
     * Throw khi không tìm thấy resource
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),  // 404
                "Not Found",
                ex.getMessage(),
                getPath(request)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Xử lý BadRequestException (400)
     * Throw khi request không hợp lệ
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            BadRequestException ex,
            WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),  // 400
                "Bad Request",
                ex.getMessage(),
                getPath(request)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý UnauthorizedException (401)
     * Throw khi chưa đăng nhập hoặc token không hợp lệ
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex,
            WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),  // 401
                "Unauthorized",
                ex.getMessage(),
                getPath(request)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Xử lý ForbiddenException (403)
     * Throw khi không có quyền truy cập
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(
            ForbiddenException ex,
            WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),  // 403
                "Forbidden",
                ex.getMessage(),
                getPath(request)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Xử lý ConflictException (409)
     * Throw khi có xung đột dữ liệu (email đã tồn tại, double booking...)
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(
            ConflictException ex,
            WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),  // 409
                "Conflict",
                ex.getMessage(),
                getPath(request)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Xử lý MethodArgumentNotValidException (400)
     * Bắt lỗi validation từ @Valid trong Controller
     * Ví dụ: @NotNull, @Email, @Size... fail
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        // Tổng hợp tất cả validation errors thành 1 message
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),  // 400
                "Validation Failed",
                message,
                getPath(request)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý TẤT CẢ exceptions còn lại chưa được handle (500)
     * Catch-all handler để không bỏ sót exception nào
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            WebRequest request) {
        
        // Log exception để developer debug (QUAN TRỌNG!)
        ex.printStackTrace();
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),  // 500
                "Internal Server Error",
                "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau.",  // Không expose chi tiết lỗi
                getPath(request)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Helper method: Lấy request path từ WebRequest
     */
    private String getPath(WebRequest request) {
        try {
            return ((ServletWebRequest) request).getRequest().getRequestURI();
        } catch (Exception e) {
            return "Unknown path";
        }
    }
}
