package org.example.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo.enums.CaLamViec;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO cho response lịch làm việc mặc định (thông tin cơ bản)
 * Dùng cho: Danh sách lịch, single schedule detail
 * 
 * @author Healthcare System Team
 * @version 1.0
 * @see org.example.demo.entity.LichLamViecMacDinh
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response lịch làm việc mặc định")
public class LichLamViecResponse {
    
    // ===== THÔNG TIN CƠ BẢN =====
    
    @Schema(description = "ID cấu hình", example = "1")
    private Integer configID;
    
    @Schema(
        description = "Thứ trong tuần (2-8)",
        example = "2",
        minimum = "2",
        maximum = "8"
    )
    private Integer thuTrongTuan;
    
    @Schema(description = "Tên thứ", example = "Thứ 2")
    private String tenThu;
    
    @Schema(
        description = "Ca làm việc",
        example = "SANG",
        allowableValues = {"SANG", "CHIEU", "TOI"}
    )
    private CaLamViec ca;
    
    @Schema(description = "Tên ca", example = "Ca sáng")
    private String tenCa;
    
    // ===== THỜI GIAN =====
    
    @Schema(description = "Thời gian bắt đầu", example = "08:00")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime thoiGianBatDau;
    
    @Schema(description = "Thời gian kết thúc", example = "12:00")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime thoiGianKetThuc;
    
    @Schema(description = "Thời gian hiển thị", example = "08:00 - 12:00")
    private String thoiGianDisplay;
    
    @Schema(description = "Số giờ làm việc", example = "4.0")
    private Double soGioLamViec;
    
    // ===== TRẠNG THÁI =====
    
    @Schema(description = "Đang áp dụng", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Trạng thái hiển thị", example = "Đang áp dụng")
    private String trangThaiDisplay;
    
    @Schema(description = "Màu sắc trạng thái (cho UI)", example = "#4CAF50")
    private String mauSacTrangThai;
    
    // ===== THÔNG TIN KHÁC =====
    
    @Schema(description = "Ghi chú", example = "Lịch làm việc buổi sáng")
    private String ghiChu;
    
    // ===== METADATA =====
    
    @Schema(description = "ID người tạo", example = "1")
    private Integer createdByID;
    
    @Schema(description = "Ngày tạo", example = "2025-11-20T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Schema(description = "ID người cập nhật cuối", example = "1")
    private Integer updatedByID;
    
    @Schema(description = "Ngày cập nhật cuối", example = "2025-11-28T14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /**
     * Static Factory Method: Tạo từ Entity
     * Dùng trong Service để convert Entity → Response DTO
     */
    public static LichLamViecResponse fromEntity(
        org.example.demo.entity.LichLamViecMacDinh entity
    ) {
        LichLamViecResponseBuilder builder = LichLamViecResponse.builder()
            .configID(entity.getConfigID())
            .thuTrongTuan(entity.getThuTrongTuan())
            .tenThu(getTenThu(entity.getThuTrongTuan()))
            .ca(entity.getCa())
            .tenCa(entity.getCa().getTenCa())
            .thoiGianBatDau(entity.getThoiGianBatDau())
            .thoiGianKetThuc(entity.getThoiGianKetThuc())
            .isActive(entity.getIsActive())
            .ghiChu(entity.getGhiChu())
            .createdByID(entity.getCreatedBy())
            .createdAt(entity.getCreatedAt())
            .updatedByID(entity.getUpdatedBy())
            .updatedAt(entity.getUpdatedAt());
        
        // Computed fields
        builder.thoiGianDisplay(buildThoiGianDisplay(entity));
        builder.soGioLamViec(calculateSoGioLamViec(entity));
        builder.trangThaiDisplay(entity.getIsActive() ? "Đang áp dụng" : "Đã tắt");
        builder.mauSacTrangThai(entity.getIsActive() ? "#4CAF50" : "#9E9E9E");
        
        return builder.build();
    }
    
    // ===== HELPER METHODS =====
    
    /**
     * Helper: Convert thuTrongTuan (2-8) sang tên thứ
     */
    private static String getTenThu(Integer thu) {
        if (thu == null) return "";
        return switch (thu) {
            case 2 -> "Thứ 2";
            case 3 -> "Thứ 3";
            case 4 -> "Thứ 4";
            case 5 -> "Thứ 5";
            case 6 -> "Thứ 6";
            case 7 -> "Thứ 7";
            case 8 -> "Chủ nhật";
            default -> "";
        };
    }
    
    /**
     * Helper: Build thời gian display "08:00 - 12:00"
     */
    private static String buildThoiGianDisplay(
        org.example.demo.entity.LichLamViecMacDinh entity
    ) {
        return String.format("%s - %s",
            formatTime(entity.getThoiGianBatDau()),
            formatTime(entity.getThoiGianKetThuc())
        );
    }
    
    /**
     * Helper: Format LocalTime to "HH:mm"
     */
    private static String formatTime(LocalTime time) {
        if (time == null) return "";
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }
    
    /**
     * Helper: Calculate số giờ làm việc
     */
    private static Double calculateSoGioLamViec(
        org.example.demo.entity.LichLamViecMacDinh entity
    ) {
        if (entity.getThoiGianBatDau() == null || entity.getThoiGianKetThuc() == null) {
            return 0.0;
        }
        
        Duration duration = Duration.between(
            entity.getThoiGianBatDau(),
            entity.getThoiGianKetThuc()
        );
        
        long minutes = duration.toMinutes();
        return Math.round(minutes / 60.0 * 10) / 10.0; // Round to 1 decimal place
    }
}
