package org.example.demo.exception;

/**
 * Exception throw khi KHÔNG CÓ QUYỀN (403)
 * Ví dụ: User cố truy cập resource của người khác, cố xóa dữ liệu không phải của mình...
 */
public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
