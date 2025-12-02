package org.example.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo.enums.CaLamViec;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO Response danh sách slot trống cho đặt lịch
 * 
 * Structure:
 * {
 *   "bacSiID": 5,
 *   "tenBacSi": "BS. Nguyễn Văn A",
 *   "ngayKham": "2025-12-15",
 *   "ca": "SANG",
 *   "tenCa": "Ca sáng (08:00 - 12:00)",
 *   "giaKham": 300000,
 *   "slots": [
 *     {"gioKham": "08:00", "available": true, "label": "08:00 - 08:30"},
 *     {"gioKham": "08:30", "available": false, "label": "08:30 - 09:00"},
 *     ...
 *   ],
 *   "totalSlots": 8,
 *   "availableSlots": 5,
 *   "bookedSlots": 3
 * }
 * 
 * @author Healthcare System Team
 * @version 1.0 - Phase 2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response danh sách slot trống")
public class AvailableSlotsResponse {
    
    // ===== THÔNG TIN BÁC SĨ =====
    
    @Schema(description = "ID bác sĩ", example = "5")
    private Integer bacSiID;
    
    @Schema(description = "Tên bác sĩ", example = "BS. Nguyễn Văn A")
    private String tenBacSi;
    
    @Schema(description = "Chuyên khoa", example = "Nội Khoa")
    private String tenChuyenKhoa;
    
    @Schema(description = "Trình độ", example = "Thạc sĩ")
    private String tenTrinhDo;
    
    @Schema(description = "Avatar bác sĩ", example = "https://storage.example.com/avatar/5.jpg")
    private String avatarUrl;
    
    // ===== THỜI GIAN =====
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày khám", example = "2025-12-15", type = "string", format = "date")
    private LocalDate ngayKham;
    
    @Schema(description = "Ca làm việc", example = "SANG")
    private CaLamViec ca;
    
    @Schema(description = "Tên ca làm việc (UI-friendly)", example = "Ca sáng (08:00 - 12:00)")
    private String tenCa;
    
    // ===== GIÁ KHÁM =====
    
    @Schema(description = "Giá khám", example = "300000")
    private BigDecimal giaKham;
    
    @Schema(description = "Giá khám (formatted)", example = "300.000 đ")
    private String giaKhamDisplay;
    
    // ===== DANH SÁCH SLOT =====
    
    @Builder.Default
    @Schema(description = "Danh sách slot (mỗi slot 30 phút)")
    private List<TimeSlotResponse> slots = new ArrayList<>();
    
    @Schema(description = "Tổng số slot", example = "8")
    private Integer totalSlots;
    
    @Schema(description = "Số slot còn trống", example = "5")
    private Integer availableSlots;
    
    @Schema(description = "Số slot đã đặt", example = "3")
    private Integer bookedSlots;
    
    // ===== COMPUTED FIELDS =====
    
    @Schema(description = "Còn slot trống không?", example = "true")
    private Boolean hasAvailableSlots;
    
    @Schema(description = "Tỷ lệ đã đặt (%)", example = "37.5")
    private Double occupancyRate;
    
    // ===== HELPER METHODS =====
    
    /**
     * Calculate computed fields
     */
    public void calculate() {
        if (slots != null) {
            this.totalSlots = slots.size();
            this.availableSlots = (int) slots.stream()
                    .filter(TimeSlotResponse::getAvailable)
                    .count();
            this.bookedSlots = totalSlots - availableSlots;
            this.hasAvailableSlots = availableSlots > 0;
            this.occupancyRate = totalSlots > 0 
                    ? (bookedSlots * 100.0 / totalSlots) 
                    : 0.0;
        }
        
        if (ca != null) {
            this.tenCa = ca.getLabel();
        }
        
        if (giaKham != null) {
            this.giaKhamDisplay = String.format("%,.0f đ", giaKham);
        }
    }
}

