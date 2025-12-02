package org.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Request check-in lịch khám (bệnh nhân đến phòng khám)
 * 
 * Flow:
 * 1. Bệnh nhân đến phòng khám đúng ngày khám
 * 2. Lễ tân/Bệnh nhân quét mã QR hoặc nhập mã xác nhận
 * 3. Hệ thống validate:
 *    - Mã xác nhận đúng không?
 *    - Đúng ngày khám không?
 *    - Lịch đã được xác nhận chưa? (TrangThai = DA_XAC_NHAN)
 * 4. Check-in thành công:
 *    - Cập nhật NgayCheckIn = now()
 *    - TrangThai vẫn là DA_XAC_NHAN (chưa DANG_KHAM)
 *    - Gửi notification cho bác sĩ: "Bệnh nhân đã đến"
 * 
 * Business Rules:
 * - Chỉ check-in được trong ngày khám
 * - Chỉ check-in được nếu TrangThai = DA_XAC_NHAN
 * - Check-in trước giờ khám tối đa 2h
 * - Check-in muộn quá 30 phút → tự động chuyển sang KHONG_DEN
 * 
 * @author Healthcare System Team
 * @version 1.0 - Phase 2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request check-in lịch khám")
public class CheckInBookingRequest {
    
    // ===== OPTION 1: Check-in bằng ID (for staff) =====
    
    @Positive(message = "DatLichID phải > 0")
    @Schema(description = "ID lịch khám (dùng cho lễ tân)", example = "123")
    private Integer datLichID;
    
    // ===== OPTION 2: Check-in bằng mã xác nhận (for patient) =====
    
    @Pattern(
        regexp = "^[A-Z0-9]{8}$", 
        message = "Mã xác nhận phải gồm 8 ký tự chữ in hoa và số"
    )
    @Schema(
        description = "Mã xác nhận 8 ký tự (dùng cho bệnh nhân tự check-in)", 
        example = "ABC12345",
        pattern = "^[A-Z0-9]{8}$"
    )
    private String maXacNhan;
    
    // ===== HELPER METHODS =====
    
    /**
     * Validate ít nhất 1 trong 2 (datLichID hoặc maXacNhan)
     */
    public void validate() {
        if (datLichID == null && (maXacNhan == null || maXacNhan.isBlank())) {
            throw new IllegalArgumentException(
                "Phải cung cấp ít nhất 1 trong 2: DatLichID hoặc MaXacNhan"
            );
        }
    }
    
    /**
     * Check check-in bằng mã xác nhận (patient self check-in)
     */
    public boolean isCheckInByCode() {
        return maXacNhan != null && !maXacNhan.isBlank();
    }
    
    /**
     * Check check-in bằng ID (staff check-in)
     */
    public boolean isCheckInById() {
        return datLichID != null;
    }
}

