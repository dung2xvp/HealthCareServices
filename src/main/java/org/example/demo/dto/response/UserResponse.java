package org.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO cho response thông tin người dùng
 * KHÔNG BAO GỒM password và các thông tin nhạy cảm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    
    private Integer nguoiDungID;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String diaChi;
    private LocalDate ngaySinh;
    private Integer gioiTinh;
    private String vaiTro; // "BENHNHAN", "BACSI", "ADMIN"
    private Boolean trangThai;
    private String avatarUrl;
    private LocalDateTime createdAt;
    
    // KHÔNG bao gồm:
    // - matKhau
    // - verificationCode
    // - codeExpiry
    // - badPoint (chỉ admin thấy)
}

