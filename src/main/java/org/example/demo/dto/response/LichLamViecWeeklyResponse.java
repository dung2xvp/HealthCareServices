package org.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo.enums.CaLamViec;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DTO cho response lịch làm việc theo tuần (Calendar view)
 * 
 * Tối ưu cho UI Calendar:
 * - Group theo ngày trong tuần
 * - Hiển thị tất cả ca của mỗi ngày
 * - Easy to render table/grid layout
 * 
 * @author Healthcare System Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response lịch làm việc theo tuần (Calendar view)")
public class LichLamViecWeeklyResponse {
    
    @Schema(
        description = "Thứ trong tuần (2-8)",
        example = "2",
        minimum = "2",
        maximum = "8"
    )
    private Integer thu;
    
    @Schema(description = "Tên thứ", example = "Thứ 2")
    private String tenThu;
    
    @Schema(description = "Danh sách ca làm việc trong ngày")
    private List<CaLamViecDetail> danhSachCa;
    
    @Schema(description = "Tổng số ca trong ngày", example = "2")
    private Integer tongSoCa;
    
    @Schema(description = "Tổng số giờ làm việc trong ngày", example = "7.0")
    private Double tongGioLamViec;
    
    @Schema(description = "Có ca nào đang active không", example = "true")
    private Boolean hasActiveCa;
    
    /**
     * Nested DTO: Chi tiết 1 ca làm việc
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Chi tiết 1 ca làm việc")
    public static class CaLamViecDetail {
        
        @Schema(description = "ID cấu hình", example = "1")
        private Integer configID;
        
        @Schema(
            description = "Ca làm việc",
            example = "SANG",
            allowableValues = {"SANG", "CHIEU", "TOI"}
        )
        private CaLamViec ca;
        
        @Schema(description = "Tên ca", example = "Ca sáng")
        private String tenCa;
        
        @Schema(description = "Thời gian bắt đầu", example = "08:00")
        private String thoiGianBatDau;
        
        @Schema(description = "Thời gian kết thúc", example = "12:00")
        private String thoiGianKetThuc;
        
        @Schema(description = "Thời gian hiển thị", example = "08:00 - 12:00")
        private String thoiGianDisplay;
        
        @Schema(description = "Số giờ làm việc", example = "4.0")
        private Double soGioLamViec;
        
        @Schema(description = "Đang áp dụng", example = "true")
        private Boolean isActive;
        
        @Schema(description = "Ghi chú", example = "Lịch làm việc buổi sáng")
        private String ghiChu;
        
        /**
         * Create from LichLamViecMacDinh entity
         */
        public static CaLamViecDetail fromEntity(
            org.example.demo.entity.LichLamViecMacDinh entity
        ) {
            return CaLamViecDetail.builder()
                .configID(entity.getConfigID())
                .ca(entity.getCa())
                .tenCa(entity.getCa().getTenCa())
                .thoiGianBatDau(formatTime(entity.getThoiGianBatDau()))
                .thoiGianKetThuc(formatTime(entity.getThoiGianKetThuc()))
                .thoiGianDisplay(buildThoiGianDisplay(entity))
                .soGioLamViec(calculateSoGioLamViec(entity))
                .isActive(entity.getIsActive())
                .ghiChu(entity.getGhiChu())
                .build();
        }
        
        private static String formatTime(LocalTime time) {
            if (time == null) return "";
            return String.format("%02d:%02d", time.getHour(), time.getMinute());
        }
        
        private static String buildThoiGianDisplay(
            org.example.demo.entity.LichLamViecMacDinh entity
        ) {
            return String.format("%s - %s",
                formatTime(entity.getThoiGianBatDau()),
                formatTime(entity.getThoiGianKetThuc())
            );
        }
        
        private static Double calculateSoGioLamViec(
            org.example.demo.entity.LichLamViecMacDinh entity
        ) {
            if (entity.getThoiGianBatDau() == null || entity.getThoiGianKetThuc() == null) {
                return 0.0;
            }
            
            java.time.Duration duration = java.time.Duration.between(
                entity.getThoiGianBatDau(),
                entity.getThoiGianKetThuc()
            );
            
            long minutes = duration.toMinutes();
            return Math.round(minutes / 60.0 * 10) / 10.0;
        }
    }
    
    /**
     * Static Factory Method: Tạo từ list entities
     * Group entities theo thuTrongTuan
     */
    public static List<LichLamViecWeeklyResponse> fromEntities(
        List<org.example.demo.entity.LichLamViecMacDinh> entities
    ) {
        // Group by thuTrongTuan
        Map<Integer, List<org.example.demo.entity.LichLamViecMacDinh>> groupedByDay = 
            entities.stream()
                .collect(Collectors.groupingBy(
                    org.example.demo.entity.LichLamViecMacDinh::getThuTrongTuan
                ));
        
        // Convert to WeeklyResponse list
        List<LichLamViecWeeklyResponse> result = new ArrayList<>();
        
        // Loop through all days (2-8)
        for (int thu = 2; thu <= 8; thu++) {
            List<org.example.demo.entity.LichLamViecMacDinh> daySchedules = 
                groupedByDay.getOrDefault(thu, new ArrayList<>());
            
            // Convert to CaLamViecDetail
            List<CaLamViecDetail> caDetails = daySchedules.stream()
                .map(CaLamViecDetail::fromEntity)
                .sorted((a, b) -> {
                    // Sort by ca order: SANG -> CHIEU -> TOI
                    int orderA = getCaOrder(a.getCa());
                    int orderB = getCaOrder(b.getCa());
                    return Integer.compare(orderA, orderB);
                })
                .collect(Collectors.toList());
            
            // Calculate tổng giờ làm việc
            double tongGio = caDetails.stream()
                .filter(ca -> ca.getIsActive())
                .mapToDouble(CaLamViecDetail::getSoGioLamViec)
                .sum();
            
            // Check có ca active không
            boolean hasActive = caDetails.stream()
                .anyMatch(CaLamViecDetail::getIsActive);
            
            result.add(LichLamViecWeeklyResponse.builder()
                .thu(thu)
                .tenThu(getTenThu(thu))
                .danhSachCa(caDetails)
                .tongSoCa(caDetails.size())
                .tongGioLamViec(Math.round(tongGio * 10) / 10.0)
                .hasActiveCa(hasActive)
                .build());
        }
        
        return result;
    }
    
    /**
     * Helper: Get ca order for sorting
     */
    private static int getCaOrder(CaLamViec ca) {
        return switch (ca) {
            case SANG -> 1;
            case CHIEU -> 2;
            case TOI -> 3;
        };
    }
    
    /**
     * Helper: Convert thuTrongTuan to tên thứ
     */
    private static String getTenThu(Integer thu) {
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
}

