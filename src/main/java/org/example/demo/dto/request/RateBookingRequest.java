package org.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO Request đánh giá lịch khám (bệnh nhân đánh giá bác sĩ sau khi khám xong)
 * 
 * Flow:
 * 1. Lịch khám hoàn thành (TrangThai = HOAN_THANH)
 * 2. Hệ thống gửi email mời đánh giá
 * 3. Bệnh nhân điền form đánh giá:
 *    - Số sao (1-5)
 *    - Nhận xét (optional)
 * 4. Hệ thống lưu đánh giá và cập nhật rating bác sĩ
 * 
 * Business Rules:
 * - Chỉ đánh giá được nếu TrangThai = HOAN_THANH
 * - Chỉ bệnh nhân mới đánh giá được
 * - Chỉ đánh giá được 1 lần (canRate() = true)
 * - Số sao: 1-5 (1 = rất tệ, 5 = rất tốt)
 * - Nhận xét không bắt buộc nhưng nên có
 * 
 * @author Healthcare System Team
 * @version 1.0 - Phase 2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request đánh giá lịch khám")
public class RateBookingRequest {
    
    @NotNull(message = "DatLichID không được null")
    @Positive(message = "DatLichID phải > 0")
    @Schema(description = "ID lịch khám", example = "123", required = true)
    private Integer datLichID;
    
    @NotNull(message = "Số sao không được null")
    @Min(value = 1, message = "Số sao tối thiểu là 1")
    @Max(value = 5, message = "Số sao tối đa là 5")
    @Schema(
        description = "Đánh giá số sao (1-5)", 
        example = "5",
        minimum = "1",
        maximum = "5",
        required = true
    )
    private Integer soSao;
    
    @Size(max = 1000, message = "Nhận xét không được quá 1000 ký tự")
    @Schema(
        description = "Nhận xét về bác sĩ/dịch vụ (optional nhưng nên có)", 
        example = "Bác sĩ tận tình, khám kỹ càng. Phòng khám sạch sẽ, nhân viên nhiệt tình."
    )
    private String nhanXet;
    
    // ===== HELPER METHODS =====
    
    /**
     * Check có nhận xét không
     */
    public boolean hasReview() {
        return nhanXet != null && !nhanXet.isBlank();
    }
    
    /**
     * Validate nếu rating thấp (1-2 sao) thì nên có nhận xét
     */
    public void validate() {
        if (soSao != null && soSao <= 2 && !hasReview()) {
            throw new IllegalArgumentException(
                "Nếu đánh giá dưới 3 sao, vui lòng để lại nhận xét để chúng tôi cải thiện dịch vụ"
            );
        }
    }
    
    /**
     * Check rating level
     */
    public String getRatingLevel() {
        if (soSao == null) return "UNKNOWN";
        if (soSao == 5) return "EXCELLENT";
        if (soSao == 4) return "GOOD";
        if (soSao == 3) return "AVERAGE";
        if (soSao == 2) return "BAD";
        return "VERY_BAD";
    }
}

