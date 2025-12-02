package org.example.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo.enums.CaLamViec;

import java.time.LocalDate;

/**
 * DTO Request tìm kiếm slot trống (available slots) cho đặt lịch khám
 * 
 * Flow:
 * 1. Bệnh nhân chọn: Bác sĩ + Ngày + Ca
 * 2. Hệ thống trả về: Danh sách slot 30 phút còn trống
 * 
 * Business Rules:
 * - Ngày khám phải >= hôm nay
 * - Bác sĩ phải đang làm việc (TrangThaiCongViec = true)
 * - Bác sĩ không nghỉ vào ngày/ca đó
 * - Slot chưa bị đặt (hoặc đã bị hủy)
 * 
 * @author Healthcare System Team
 * @version 1.0 - Phase 2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request tìm slot trống cho đặt lịch")
public class SearchAvailableSlotsRequest {
    
    @NotNull(message = "BacSiID không được null")
    @Positive(message = "BacSiID phải > 0")
    @Schema(description = "ID bác sĩ", example = "5", required = true)
    private Integer bacSiID;
    
    @NotNull(message = "Ngày khám không được null")
    @FutureOrPresent(message = "Ngày khám phải từ hôm nay trở đi")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(
        description = "Ngày khám (phải >= hôm nay)", 
        example = "2025-12-15",
        type = "string",
        format = "date",
        required = true
    )
    private LocalDate ngayKham;
    
    @NotNull(message = "Ca làm việc không được null")
    @Schema(
        description = "Ca làm việc: SANG (08:00-12:00), CHIEU (14:00-17:00), TOI (18:00-21:00)", 
        example = "SANG",
        allowableValues = {"SANG", "CHIEU", "TOI"},
        required = true
    )
    private CaLamViec ca;
    
    // ===== OPTIONAL: Lọc theo giá =====
    
    @Min(value = 0, message = "Giá tối thiểu phải >= 0")
    @Schema(description = "Lọc theo giá tối thiểu (optional)", example = "200000")
    private Integer giaMin;
    
    @Min(value = 0, message = "Giá tối đa phải >= 0")
    @Schema(description = "Lọc theo giá tối đa (optional)", example = "500000")
    private Integer giaMax;
    
    // ===== HELPER METHODS =====
    
    /**
     * Validate logic nghiệp vụ
     */
    public void validate() {
        if (giaMin != null && giaMax != null && giaMin > giaMax) {
            throw new IllegalArgumentException("Giá tối thiểu không được lớn hơn giá tối đa");
        }
    }
    
    /**
     * Check có filter theo giá không
     */
    public boolean hasGiaFilter() {
        return giaMin != null || giaMax != null;
    }
}

