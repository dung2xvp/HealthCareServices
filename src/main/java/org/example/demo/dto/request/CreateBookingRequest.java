package org.example.demo.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo.enums.CaLamViec;
import org.example.demo.enums.PhuongThucThanhToan;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO Request tạo lịch khám mới
 * 
 * Flow:
 * 1. Bệnh nhân điền form đặt lịch
 * 2. Hệ thống validate:
 *    - Slot còn trống không?
 *    - Bác sĩ có làm việc không?
 *    - Bác sĩ có nghỉ không?
 *    - Bệnh nhân có BadPoint quá cao không?
 * 3. Tạo booking với TrangThai = CHO_XAC_NHAN_BAC_SI
 * 4. Gửi notification cho bác sĩ
 * 
 * Business Rules:
 * - Ngày khám phải >= hôm nay + 1 (đặt trước ít nhất 1 ngày)
 * - Giờ khám phải chia hết cho 30 phút (08:00, 08:30, 09:00...)
 * - Lý do khám tối thiểu 10 ký tự
 * - Nếu thanh toán online → redirect to VNPay
 * - Nếu thanh toán tiền mặt → chờ xác nhận bác sĩ trực tiếp
 * 
 * @author Healthcare System Team
 * @version 1.0 - Phase 2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request tạo lịch khám mới")
public class CreateBookingRequest {
    
    // ===== THÔNG TIN BÁC SĨ & THỜI GIAN =====
    
    @NotNull(message = "BacSiID không được null")
    @Positive(message = "BacSiID phải > 0")
    @Schema(description = "ID bác sĩ", example = "5", required = true)
    private Integer bacSiID;
    
    @NotNull(message = "Ngày khám không được null")
    @Future(message = "Ngày khám phải là ngày trong tương lai (đặt trước ít nhất 1 ngày)")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(
        description = "Ngày khám (phải là ngày tương lai)", 
        example = "2025-12-15",
        type = "string",
        format = "date",
        required = true
    )
    private LocalDate ngayKham;
    
    @NotNull(message = "Ca làm việc không được null")
    @Schema(
        description = "Ca làm việc: SANG, CHIEU, TOI", 
        example = "SANG",
        allowableValues = {"SANG", "CHIEU", "TOI"},
        required = true
    )
    private CaLamViec ca;
    
    @NotNull(message = "Giờ khám không được null")
    @JsonFormat(pattern = "HH:mm")
    @Schema(
        description = "Giờ khám cụ thể trong ca (mỗi slot 30 phút: 08:00, 08:30, 09:00...)", 
        example = "08:30",
        type = "string",
        format = "time",
        required = true
    )
    private LocalTime gioKham;
    
    // ===== LÝ DO KHÁM =====
    
    @NotBlank(message = "Lý do khám không được để trống")
    @Size(min = 10, max = 1000, message = "Lý do khám phải từ 10-1000 ký tự")
    @Schema(
        description = "Lý do khám bệnh", 
        example = "Đau đầu kéo dài 3 ngày, chóng mặt, buồn nôn",
        required = true,
        minLength = 10,
        maxLength = 1000
    )
    private String lyDoKham;
    
    @Size(max = 500, message = "Ghi chú không được quá 500 ký tự")
    @Schema(
        description = "Ghi chú thêm (optional)", 
        example = "Muốn khám vào buổi sáng sớm nếu được"
    )
    private String ghiChu;
    
    // ===== THANH TOÁN =====
    
    @NotNull(message = "Phương thức thanh toán không được null")
    @Schema(
        description = "Phương thức thanh toán", 
        example = "VNPAY",
        allowableValues = {"TIEN_MAT", "CHUYEN_KHOAN", "VNPAY", "MOMO", "ZALO_PAY"},
        required = true
    )
    private PhuongThucThanhToan phuongThucThanhToan;
    
    // ===== OPTIONAL: HỒ SƠ BỆNH ÁN =====
    
    @Schema(description = "Tiền sử bệnh (optional)", example = "Tiểu đường type 2")
    private String tienSuBenh;
    
    @Schema(description = "Thuốc đang dùng (optional)", example = "Metformin 500mg")
    private String thuocDangDung;
    
    @Schema(description = "Dị ứng thuốc (optional)", example = "Penicillin")
    private String diUng;
    
    // ===== HELPER METHODS =====
    
    /**
     * Validate logic nghiệp vụ
     */
    public void validate() {
        // Validate giờ khám phải chia hết cho 30 phút
        if (gioKham != null) {
            int minute = gioKham.getMinute();
            if (minute != 0 && minute != 30) {
                throw new IllegalArgumentException(
                    "Giờ khám phải chia hết cho 30 phút (VD: 08:00, 08:30, 09:00...)"
                );
            }
        }
        
        // Validate giờ khám nằm trong ca làm việc
        if (ca != null && gioKham != null) {
            LocalTime startTime = ca.getThoiGianMacDinhBatDau();
            LocalTime endTime = ca.getThoiGianMacDinhKetThuc();
            
            if (gioKham.isBefore(startTime) || gioKham.isAfter(endTime)) {
                throw new IllegalArgumentException(
                    String.format(
                        "Giờ khám %s không nằm trong ca %s (%s - %s)",
                        gioKham, ca.name(), startTime, endTime
                    )
                );
            }
        }
    }
    
    /**
     * Check có thanh toán online không
     */
    public boolean isOnlinePayment() {
        return phuongThucThanhToan != null && phuongThucThanhToan.isOnlinePayment();
    }
    
    /**
     * Check có thông tin y tế kèm theo không
     */
    public boolean hasMedicalInfo() {
        return (tienSuBenh != null && !tienSuBenh.isBlank()) ||
               (thuocDangDung != null && !thuocDangDung.isBlank()) ||
               (diUng != null && !diUng.isBlank());
    }
}

