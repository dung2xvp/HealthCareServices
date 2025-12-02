package org.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DTO cho response tổng quan lịch làm việc (Dashboard/Statistics)
 * 
 * Use Cases:
 * - Admin dashboard
 * - Quick overview
 * - Statistics
 * 
 * @author Healthcare System Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response tổng quan lịch làm việc")
public class LichLamViecSummaryResponse {
    
    // ===== THỐNG KÊ CƠ BẢN =====
    
    @Schema(description = "Tổng số ca làm việc", example = "14")
    private Integer tongSoCa;
    
    @Schema(description = "Số ca đang áp dụng", example = "14")
    private Integer soCaActive;
    
    @Schema(description = "Số ca đã tắt", example = "0")
    private Integer soCaInactive;
    
    @Schema(description = "Tỷ lệ ca đang áp dụng (%)", example = "100.0")
    private Double tyLeActive;
    
    // ===== THỐNG KÊ THỜI GIAN =====
    
    @Schema(description = "Tổng giờ làm việc/tuần", example = "56.0")
    private Double tongGioLamViecTuan;
    
    @Schema(description = "Trung bình giờ/ngày", example = "8.0")
    private Double trungBinhGioMotNgay;
    
    @Schema(description = "Số ngày làm việc/tuần", example = "7")
    private Integer soNgayLamViec;
    
    // ===== THỐNG KÊ THEO CA =====
    
    @Schema(description = "Số ca SANG", example = "7")
    private Integer soCaSang;
    
    @Schema(description = "Số ca CHIEU", example = "7")
    private Integer soCaChieu;
    
    @Schema(description = "Số ca TOI", example = "0")
    private Integer soCaToi;
    
    // ===== DANH SÁCH NGÀY NGHỈ =====
    
    @Schema(description = "Danh sách ngày không có ca nào hoặc tất cả ca đã tắt")
    private List<String> danhSachNgayNghi;
    
    @Schema(description = "Có ngày nghỉ không", example = "false")
    private Boolean hasNgayNghi;
    
    // ===== DANH SÁCH NGÀY CÓ CA =====
    
    @Schema(description = "Danh sách ngày có ca làm việc")
    private List<String> danhSachNgayLamViec;
    
    // ===== CẢNH BÁO =====
    
    @Schema(description = "Danh sách cảnh báo (nếu có)")
    private List<String> warnings;
    
    @Schema(description = "Có cảnh báo không", example = "false")
    private Boolean hasWarnings;
    
    /**
     * Static Factory Method: Tạo từ list entities
     */
    public static LichLamViecSummaryResponse fromEntities(
        List<org.example.demo.entity.LichLamViecMacDinh> entities
    ) {
        if (entities == null || entities.isEmpty()) {
            return createEmpty();
        }
        
        // Thống kê cơ bản
        int total = entities.size();
        long activeCount = entities.stream()
            .filter(org.example.demo.entity.LichLamViecMacDinh::getIsActive)
            .count();
        int active = (int) activeCount;
        int inactive = total - active;
        double tyLe = total > 0 ? (active * 100.0 / total) : 0.0;
        
        // Thống kê theo ca
        Map<org.example.demo.enums.CaLamViec, Long> byCa = entities.stream()
            .filter(org.example.demo.entity.LichLamViecMacDinh::getIsActive)
            .collect(Collectors.groupingBy(
                org.example.demo.entity.LichLamViecMacDinh::getCa,
                Collectors.counting()
            ));
        
        int soCaSang = byCa.getOrDefault(org.example.demo.enums.CaLamViec.SANG, 0L).intValue();
        int soCaChieu = byCa.getOrDefault(org.example.demo.enums.CaLamViec.CHIEU, 0L).intValue();
        int soCaToi = byCa.getOrDefault(org.example.demo.enums.CaLamViec.TOI, 0L).intValue();
        
        // Tổng giờ làm việc
        double tongGio = entities.stream()
            .filter(org.example.demo.entity.LichLamViecMacDinh::getIsActive)
            .mapToDouble(e -> {
                java.time.Duration duration = java.time.Duration.between(
                    e.getThoiGianBatDau(),
                    e.getThoiGianKetThuc()
                );
                return duration.toMinutes() / 60.0;
            })
            .sum();
        
        // Số ngày làm việc (số ngày có ít nhất 1 ca active)
        int soNgayLamViec = (int) entities.stream()
            .filter(org.example.demo.entity.LichLamViecMacDinh::getIsActive)
            .map(org.example.demo.entity.LichLamViecMacDinh::getThuTrongTuan)
            .distinct()
            .count();
        
        double trungBinhGio = soNgayLamViec > 0 ? tongGio / soNgayLamViec : 0.0;
        
        // Danh sách ngày làm việc & ngày nghỉ
        List<String> ngayLamViec = new ArrayList<>();
        List<String> ngayNghi = new ArrayList<>();
        
        for (int thu = 2; thu <= 8; thu++) {
            final int finalThu = thu;
            boolean hasActiveCa = entities.stream()
                .anyMatch(e -> 
                    e.getThuTrongTuan().equals(finalThu) && e.getIsActive()
                );
            
            String tenThu = getTenThu(thu);
            if (hasActiveCa) {
                ngayLamViec.add(tenThu);
            } else {
                ngayNghi.add(tenThu);
            }
        }
        
        // Warnings
        List<String> warnings = new ArrayList<>();
        if (ngayNghi.size() >= 3) {
            warnings.add("Có " + ngayNghi.size() + " ngày nghỉ trong tuần");
        }
        if (soCaToi == 0) {
            warnings.add("Chưa có ca TOI nào được thiết lập");
        }
        if (tongGio < 40) {
            warnings.add("Tổng giờ làm việc/tuần thấp (" + Math.round(tongGio * 10) / 10.0 + " giờ)");
        }
        
        return LichLamViecSummaryResponse.builder()
            .tongSoCa(total)
            .soCaActive(active)
            .soCaInactive(inactive)
            .tyLeActive(Math.round(tyLe * 10) / 10.0)
            .tongGioLamViecTuan(Math.round(tongGio * 10) / 10.0)
            .trungBinhGioMotNgay(Math.round(trungBinhGio * 10) / 10.0)
            .soNgayLamViec(soNgayLamViec)
            .soCaSang(soCaSang)
            .soCaChieu(soCaChieu)
            .soCaToi(soCaToi)
            .danhSachNgayNghi(ngayNghi)
            .hasNgayNghi(!ngayNghi.isEmpty())
            .danhSachNgayLamViec(ngayLamViec)
            .warnings(warnings)
            .hasWarnings(!warnings.isEmpty())
            .build();
    }
    
    /**
     * Create empty summary
     */
    private static LichLamViecSummaryResponse createEmpty() {
        return LichLamViecSummaryResponse.builder()
            .tongSoCa(0)
            .soCaActive(0)
            .soCaInactive(0)
            .tyLeActive(0.0)
            .tongGioLamViecTuan(0.0)
            .trungBinhGioMotNgay(0.0)
            .soNgayLamViec(0)
            .soCaSang(0)
            .soCaChieu(0)
            .soCaToi(0)
            .danhSachNgayNghi(List.of("Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"))
            .hasNgayNghi(true)
            .danhSachNgayLamViec(List.of())
            .warnings(List.of("Chưa có lịch làm việc nào được thiết lập"))
            .hasWarnings(true)
            .build();
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

