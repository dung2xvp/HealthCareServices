package org.example.demo.exception;

/**
 * Exception throw khi CHƯA ĐĂNG NHẬP (401)
 * Ví dụ: Token không hợp lệ, token expired, chưa authenticate...
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
