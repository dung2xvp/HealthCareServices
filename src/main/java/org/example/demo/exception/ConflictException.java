package org.example.demo.exception;

/**
 * Exception throw khi có XUNG ĐỘT DỮ LIỆU (409)
 * Ví dụ: Email đã tồn tại, bác sĩ đã có lịch khám vào slot đó...
 */
public class ConflictException extends RuntimeException {
    
    public ConflictException(String message) {
        super(message);
    }
    
    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
