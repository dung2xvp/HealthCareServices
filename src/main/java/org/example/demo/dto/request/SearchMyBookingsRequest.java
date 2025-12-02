package org.example.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo.enums.TrangThaiDatLich;

import java.time.LocalDate;

/**
 * DTO Request tìm kiếm lịch khám của bệnh nhân/bác sĩ
 * 
 * Use cases:
 * 1. Bệnh nhân xem lịch sử khám của mình
 * 2. Bác sĩ xem danh sách lịch khám
 * 3. Admin xem tất cả lịch khám
 * 
 * Filters:
 * - Theo trạng thái (CHO_XAC_NHAN, DA_XAC_NHAN, HOAN_THANH...)
 * - Theo khoảng thời gian (fromDate - toDate)
 * - Theo bác sĩ (bacSiID)
 * - Pagination (page, size)
 * 
 * @author Healthcare System Team
 * @version 1.0 - Phase 2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request tìm kiếm lịch khám")
public class SearchMyBookingsRequest {
    
    // ===== FILTERS =====
    
    @Schema(
        description = "Trạng thái lịch khám (optional - nếu null thì lấy tất cả)", 
        example = "DA_XAC_NHAN",
        allowableValues = {
            "CHO_XAC_NHAN_BAC_SI", "TU_CHOI", "CHO_THANH_TOAN", "DA_XAC_NHAN",
            "DANG_KHAM", "HOAN_THANH", "HUY_BOI_BENH_NHAN", "HUY_BOI_BAC_SI",
            "HUY_BOI_ADMIN", "KHONG_DEN", "QUA_HAN"
        }
    )
    private TrangThaiDatLich trangThai;
    
    @Positive(message = "BacSiID phải > 0")
    @Schema(description = "Lọc theo bác sĩ (optional)", example = "5")
    private Integer bacSiID;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(
        description = "Từ ngày (optional)", 
        example = "2025-12-01",
        type = "string",
        format = "date"
    )
    private LocalDate fromDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(
        description = "Đến ngày (optional)", 
        example = "2025-12-31",
        type = "string",
        format = "date"
    )
    private LocalDate toDate;
    
    @Schema(description = "Chỉ lấy lịch sắp tới (ngày khám >= hôm nay)", example = "true")
    private Boolean upcomingOnly;
    
    @Schema(description = "Chỉ lấy lịch đã qua (ngày khám < hôm nay)", example = "false")
    private Boolean pastOnly;
    
    // ===== PAGINATION =====
    
    @Min(value = 0, message = "Page phải >= 0")
    @Builder.Default
    @Schema(description = "Số trang (bắt đầu từ 0)", example = "0", defaultValue = "0")
    private Integer page = 0;
    
    @Min(value = 1, message = "Size phải >= 1")
    @Builder.Default
    @Schema(description = "Số lượng mỗi trang", example = "10", defaultValue = "10")
    private Integer size = 10;
    
    @Schema(description = "Sắp xếp (field,direction)", example = "ngayKham,desc")
    @Builder.Default
    private String sort = "ngayKham,desc"; // Mặc định: mới nhất trước
    
    // ===== HELPER METHODS =====
    
    /**
     * Validate logic nghiệp vụ
     */
    public void validate() {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate không được sau toDate");
        }
        
        if (Boolean.TRUE.equals(upcomingOnly) && Boolean.TRUE.equals(pastOnly)) {
            throw new IllegalArgumentException("Không thể chọn cả upcomingOnly và pastOnly");
        }
    }
    
    /**
     * Check có filter theo ngày không
     */
    public boolean hasDateFilter() {
        return fromDate != null || toDate != null || upcomingOnly != null || pastOnly != null;
    }
    
    /**
     * Check có filter theo bác sĩ không
     */
    public boolean hasDoctorFilter() {
        return bacSiID != null;
    }
    
    /**
     * Check có filter theo trạng thái không
     */
    public boolean hasStatusFilter() {
        return trangThai != null;
    }
}

