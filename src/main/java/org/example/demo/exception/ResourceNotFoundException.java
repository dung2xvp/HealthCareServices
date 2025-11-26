package org.example.demo.exception;

/**
 * Exception throw khi KHÔNG TÌM THẤY resource (404)
 * Ví dụ: Không tìm thấy bác sĩ, bệnh nhân, lịch đặt...
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
