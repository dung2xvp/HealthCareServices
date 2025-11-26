package org.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho response sau khi đăng nhập thành công
 * Chứa JWT token và thông tin user
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    
    private String accessToken;
    
    @Builder.Default
    private String tokenType = "Bearer";
    
    private Long expiresIn; // Milliseconds (ví dụ: 86400000 = 24 giờ)
    private UserResponse userInfo;
}

