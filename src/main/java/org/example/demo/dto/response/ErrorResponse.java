package org.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Class đại diện cho format của error response trả về API
 * Sử dụng cho tất cả lỗi trong hệ thống
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    // Thời điểm xảy ra lỗi
    private LocalDateTime timestamp;
    
    // HTTP status code (400, 401, 404, 500...)
    private int status;
    
    // Tên lỗi ngắn gọn (Bad Request, Not Found...)
    private String error;
    
    // Message chi tiết mô tả lỗi (user sẽ thấy message này)
    private String message;
    
    // URL endpoint gây ra lỗi (để debug)
    private String path;
}
