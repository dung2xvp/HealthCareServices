package org.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Request bác sĩ xác nhận/từ chối lịch khám
 * 
 * Flow - Bác sĩ XÁC NHẬN:
 * 1. Bác sĩ nhận notification có lịch mới
 * 2. Bác sĩ xem chi tiết lịch (lý do khám, thông tin bệnh nhân)
 * 3. Bác sĩ xác nhận: duyet = true, lyDoTuChoi = null
 * 4. Hệ thống:
 *    - Cập nhật TrangThai = DA_XAC_NHAN (nếu đã thanh toán)
 *    - Hoặc CHO_THANH_TOAN (nếu chưa thanh toán online)
 *    - Gửi email cho bệnh nhân
 * 
 * Flow - Bác sĩ TỪ CHỐI:
 * 1. Bác sĩ từ chối: duyet = false, lyDoTuChoi = "..."
 * 2. Hệ thống:
 *    - Cập nhật TrangThai = TU_CHOI
 *    - Hoàn tiền nếu đã thanh toán
 *    - Gửi email cho bệnh nhân
 *    - Giải phóng slot
 * 
 * Business Rules:
 * - Chỉ xử lý được nếu TrangThai = CHO_XAC_NHAN_BAC_SI
 * - Nếu từ chối phải có lý do (tối thiểu 10 ký tự)
 * - Bác sĩ chỉ xử lý được lịch của chính mình
 * 
 * @author Healthcare System Team
 * @version 1.0 - Phase 2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request bác sĩ xác nhận/từ chối lịch khám")
public class DoctorConfirmBookingRequest {
    
    @NotNull(message = "DatLichID không được null")
    @Positive(message = "DatLichID phải > 0")
    @Schema(description = "ID lịch khám cần xử lý", example = "123", required = true)
    private Integer datLichID;
    
    @NotNull(message = "Trạng thái duyệt không được null")
    @Schema(
        description = "Bác sĩ có đồng ý lịch khám này không?", 
        example = "true",
        required = true
    )
    private Boolean duyet;
    
    @Size(max = 500, message = "Lý do từ chối không được quá 500 ký tự")
    @Schema(
        description = "Lý do từ chối (bắt buộc nếu duyet = false)", 
        example = "Lịch làm việc đã đầy, xin lỗi quý khách"
    )
    private String lyDoTuChoi;
    
    // ===== HELPER METHODS =====
    
    /**
     * Validate logic nghiệp vụ
     */
    public void validate() {
        // Nếu từ chối phải có lý do
        if (duyet != null && !duyet) {
            if (lyDoTuChoi == null || lyDoTuChoi.isBlank()) {
                throw new IllegalArgumentException("Lý do từ chối không được để trống");
            }
            if (lyDoTuChoi.length() < 10) {
                throw new IllegalArgumentException("Lý do từ chối phải từ 10 ký tự trở lên");
            }
        }
        
        // Nếu đồng ý thì không cần lý do
        if (duyet != null && duyet && lyDoTuChoi != null && !lyDoTuChoi.isBlank()) {
            throw new IllegalArgumentException("Không cần lý do từ chối khi đồng ý lịch khám");
        }
    }
    
    /**
     * Check có phải là xác nhận không (approve)
     */
    public boolean isApprove() {
        return duyet != null && duyet;
    }
    
    /**
     * Check có phải là từ chối không (reject)
     */
    public boolean isReject() {
        return duyet != null && !duyet;
    }
}

