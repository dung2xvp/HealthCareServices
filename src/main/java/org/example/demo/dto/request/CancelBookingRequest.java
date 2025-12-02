package org.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Request hủy lịch khám
 * 
 * Flow:
 * 1. User (Bệnh nhân/Bác sĩ/Admin) request hủy lịch
 * 2. Hệ thống validate:
 *    - Lịch còn có thể hủy không? (TrangThai.canCancel())
 *    - User có quyền hủy không?
 * 3. Cập nhật TrangThai:
 *    - Bệnh nhân hủy → HUY_BOI_BENH_NHAN
 *    - Bác sĩ hủy → HUY_BOI_BAC_SI
 *    - Admin hủy → HUY_BOI_ADMIN
 * 4. Nếu đã thanh toán online → Tự động hoàn tiền
 * 5. Gửi notification cho cả 2 bên
 * 
 * Business Rules:
 * - Chỉ hủy được nếu TrangThai.canCancel() = true
 * - Bệnh nhân hủy → tăng BadPoint
 * - Hủy trước 24h → không tăng BadPoint
 * - Hủy sau 24h → tăng 1 BadPoint
 * - BadPoint >= 3 → Khóa tài khoản tạm thời
 * 
 * @author Healthcare System Team
 * @version 1.0 - Phase 2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request hủy lịch khám")
public class CancelBookingRequest {
    
    @NotNull(message = "DatLichID không được null")
    @Positive(message = "DatLichID phải > 0")
    @Schema(description = "ID lịch khám cần hủy", example = "123", required = true)
    private Integer datLichID;
    
    @NotBlank(message = "Lý do hủy không được để trống")
    @Size(min = 10, max = 500, message = "Lý do hủy phải từ 10-500 ký tự")
    @Schema(
        description = "Lý do hủy lịch", 
        example = "Có việc đột xuất không thể sắp xếp được",
        required = true,
        minLength = 10,
        maxLength = 500
    )
    private String lyDoHuy;
    
    // ===== HELPER METHODS =====
    
    /**
     * Validate lý do hủy có đủ chi tiết không
     */
    public boolean isDetailedReason() {
        return lyDoHuy != null && lyDoHuy.length() >= 20;
    }
}

