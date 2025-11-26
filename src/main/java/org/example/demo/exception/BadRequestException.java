package org.example.demo.exception;

/**
 * Exception throw khi REQUEST KHÔNG HỢP LỆ (400)
 * Ví dụ: Ngày khám quá khứ, email sai format, dữ liệu không hợp lệ...
 */
public class BadRequestException extends RuntimeException {
    
    public BadRequestException(String message) {
        super(message);
    }
    
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
