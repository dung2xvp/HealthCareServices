package org.example.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO Request hoàn thành lịch khám (bác sĩ điền kết quả khám)
 * 
 * Flow:
 * 1. Bác sĩ khám bệnh xong
 * 2. Bác sĩ điền form kết quả khám:
 *    - Chẩn đoán
 *    - Đơn thuốc (optional)
 *    - Lời dặn
 *    - Ngày tái khám (optional)
 * 3. Hệ thống:
 *    - Cập nhật TrangThai = HOAN_THANH
 *    - Lưu NgayHoanThanh = now()
 *    - Gửi email kết quả cho bệnh nhân
 *    - Cho phép bệnh nhân đánh giá
 * 
 * Business Rules:
 * - Chỉ bác sĩ mới điền được
 * - TrangThai phải là DANG_KHAM
 * - Chẩn đoán là bắt buộc
 * - Ngày tái khám phải > ngày khám hiện tại
 * 
 * @author Healthcare System Team
 * @version 1.0 - Phase 2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request hoàn thành lịch khám và điền kết quả")
public class CompleteAppointmentRequest {
    
    @NotNull(message = "DatLichID không được null")
    @Positive(message = "DatLichID phải > 0")
    @Schema(description = "ID lịch khám", example = "123", required = true)
    private Integer datLichID;
    
    // ===== KẾT QUẢ KHÁM =====
    
    @NotBlank(message = "Chẩn đoán không được để trống")
    @Size(min = 5, max = 500, message = "Chẩn đoán phải từ 5-500 ký tự")
    @Schema(
        description = "Chẩn đoán bệnh", 
        example = "Viêm amidan cấp độ 2",
        required = true,
        minLength = 5,
        maxLength = 500
    )
    private String chanDoan;
    
    @Size(max = 2000, message = "Kết quả khám không được quá 2000 ký tự")
    @Schema(
        description = "Kết quả khám chi tiết (optional)", 
        example = "Amidan sưng đỏ cả hai bên, có mủ. Huyết áp: 120/80. Nhiệt độ: 38.5°C"
    )
    private String ketQuaKham;
    
    @Size(max = 2000, message = "Đơn thuốc không được quá 2000 ký tự")
    @Schema(
        description = "Đơn thuốc (optional)", 
        example = "1. Amoxicillin 500mg, uống 3 lần/ngày sau ăn\n2. Paracetamol 500mg, uống khi sốt"
    )
    private String donThuoc;
    
    @Size(max = 1000, message = "Lời dặn không được quá 1000 ký tự")
    @Schema(
        description = "Lời dặn của bác sĩ (optional)", 
        example = "Uống đủ nước, nghỉ ngơi nhiều, tránh thức khuya. Tái khám sau 7 ngày."
    )
    private String loiDanBacSi;
    
    // ===== TÁI KHÁM =====
    
    @Future(message = "Ngày tái khám phải là ngày trong tương lai")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(
        description = "Ngày hẹn tái khám (optional)", 
        example = "2025-12-22",
        type = "string",
        format = "date"
    )
    private LocalDate ngayTaiKham;
    
    // ===== HELPER METHODS =====
    
    /**
     * Check có kê đơn thuốc không
     */
    public boolean hasPrescription() {
        return donThuoc != null && !donThuoc.isBlank();
    }
    
    /**
     * Check có hẹn tái khám không
     */
    public boolean hasFollowUp() {
        return ngayTaiKham != null;
    }
    
    /**
     * Check có lời dặn không
     */
    public boolean hasAdvice() {
        return loiDanBacSi != null && !loiDanBacSi.isBlank();
    }
}

