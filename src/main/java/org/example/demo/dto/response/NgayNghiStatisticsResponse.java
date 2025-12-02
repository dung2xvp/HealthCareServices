package org.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DTO cho response thống kê yêu cầu nghỉ
 * 
 * Use Cases:
 * - Admin dashboard: Xem overview yêu cầu nghỉ
 * - Bác sĩ dashboard: Xem thống kê yêu cầu của mình
 * - Reports & Analytics
 * 
 * @author Healthcare System Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response thống kê yêu cầu nghỉ")
public class NgayNghiStatisticsResponse {
    
    // ===== THỐNG KÊ CƠ BẢN =====
    
    @Schema(description = "Tổng số yêu cầu", example = "25")
    private Integer tongYeuCau;
    
    @Schema(description = "Số yêu cầu chờ xử lý", example = "5")
    private Integer choXuLy;
    
    @Schema(description = "Số yêu cầu đã duyệt", example = "18")
    private Integer daDuyet;
    
    @Schema(description = "Số yêu cầu bị từ chối", example = "2")
    private Integer tuChoi;
    
    @Schema(description = "Số yêu cầu đã bị hủy", example = "0")
    private Integer daBiHuy;
    
    @Schema(description = "Tỷ lệ duyệt (%)", example = "90.0")
    private Double tyLeDuyet;
    
    // ===== THỐNG KÊ THEO LOẠI PHÉP =====
    
    @Schema(description = "Thống kê theo loại nghỉ phép")
    private Map<String, Integer> thongKeTheoLoaiPhep;
    
    @Schema(description = "Loại phép phổ biến nhất", example = "PHEP_NAM")
    private String loaiPhepPhoBienNhat;
    
    // ===== THỐNG KÊ THEO LOẠI NGHỈ =====
    
    @Schema(description = "Thống kê theo loại nghỉ")
    private Map<String, Integer> thongKeTheoLoaiNghi;
    
    @Schema(description = "Loại nghỉ phổ biến nhất", example = "NGAY_CU_THE")
    private String loaiNghiPhoBienNhat;
    
    // ===== THỐNG KÊ THEO THỜI GIAN =====
    
    @Schema(description = "Thống kê theo tháng")
    private List<MonthlyStats> thongKeTheoThang;
    
    @Schema(description = "Tháng có nhiều yêu cầu nhất", example = "12")
    private Integer thangNhieuYeuCauNhat;
    
    // ===== THỐNG KÊ BÁC SĨ =====
    
    @Schema(description = "Số bác sĩ đã tạo yêu cầu", example = "10")
    private Integer soBacSiCoYeuCau;
    
    @Schema(description = "Trung bình yêu cầu/bác sĩ", example = "2.5")
    private Double trungBinhYeuCauMotBacSi;
    
    // ===== THỜI GIAN XỬ LÝ =====
    
    @Schema(description = "Thời gian xử lý trung bình (ngày)", example = "1.5")
    private Double thoiGianXuLyTrungBinh;
    
    @Schema(description = "Yêu cầu chờ lâu nhất (ngày)", example = "5")
    private Integer yeuCauChoLauNhat;
    
    /**
     * Nested DTO: Thống kê theo tháng
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Thống kê theo tháng")
    public static class MonthlyStats {
        
        @Schema(description = "Tháng (1-12)", example = "12")
        private Integer thang;
        
        @Schema(description = "Tên tháng", example = "Tháng 12")
        private String tenThang;
        
        @Schema(description = "Số yêu cầu", example = "8")
        private Integer soYeuCau;
        
        @Schema(description = "Số đã duyệt", example = "7")
        private Integer soDaDuyet;
        
        @Schema(description = "Số bị từ chối", example = "1")
        private Integer soTuChoi;
        
        @Schema(description = "Số chờ xử lý", example = "0")
        private Integer soChoXuLy;
    }
    
    /**
     * Static Factory Method: Tạo từ list entities
     */
    public static NgayNghiStatisticsResponse fromEntities(
        List<org.example.demo.entity.BacSiNgayNghi> entities
    ) {
        if (entities == null || entities.isEmpty()) {
            return createEmpty();
        }
        
        // Thống kê cơ bản
        int total = entities.size();
        
        Map<org.example.demo.enums.TrangThaiNghi, Long> byStatus = entities.stream()
            .collect(Collectors.groupingBy(
                org.example.demo.entity.BacSiNgayNghi::getTrangThai,
                Collectors.counting()
            ));
        
        int choXuLy = byStatus.getOrDefault(org.example.demo.enums.TrangThaiNghi.CHO_DUYET, 0L).intValue();
        int daDuyet = byStatus.getOrDefault(org.example.demo.enums.TrangThaiNghi.DA_DUYET, 0L).intValue();
        int tuChoi = byStatus.getOrDefault(org.example.demo.enums.TrangThaiNghi.TU_CHOI, 0L).intValue();
        int huy = byStatus.getOrDefault(org.example.demo.enums.TrangThaiNghi.HUY, 0L).intValue();
        
        // Tỷ lệ duyệt
        int processed = daDuyet + tuChoi; // Đã xử lý (duyệt + từ chối)
        double tyLeDuyet = processed > 0 ? (daDuyet * 100.0 / processed) : 0.0;
        
        // Thống kê theo loại phép
        Map<String, Integer> byLoaiPhep = entities.stream()
            .collect(Collectors.groupingBy(
                e -> e.getLoaiNghiPhep().name(),
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
        
        String loaiPhepPhoBien = byLoaiPhep.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        // Thống kê theo loại nghỉ
        Map<String, Integer> byLoaiNghi = entities.stream()
            .collect(Collectors.groupingBy(
                e -> e.getLoaiNghi().name(),
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
        
        String loaiNghiPhoBien = byLoaiNghi.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        // Thống kê theo tháng
        Map<Integer, List<org.example.demo.entity.BacSiNgayNghi>> byMonth = entities.stream()
            .collect(Collectors.groupingBy(
                e -> e.getCreatedAt().getMonthValue()
            ));
        
        List<MonthlyStats> monthlyStats = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            List<org.example.demo.entity.BacSiNgayNghi> monthEntities = 
                byMonth.getOrDefault(month, List.of());
            
            if (!monthEntities.isEmpty()) {
                int totalMonth = monthEntities.size();
                long duyet = monthEntities.stream()
                    .filter(e -> e.getTrangThai() == org.example.demo.enums.TrangThaiNghi.DA_DUYET)
                    .count();
                long reject = monthEntities.stream()
                    .filter(e -> e.getTrangThai() == org.example.demo.enums.TrangThaiNghi.TU_CHOI)
                    .count();
                long pending = monthEntities.stream()
                    .filter(e -> e.getTrangThai() == org.example.demo.enums.TrangThaiNghi.CHO_DUYET)
                    .count();
                
                monthlyStats.add(MonthlyStats.builder()
                    .thang(month)
                    .tenThang("Tháng " + month)
                    .soYeuCau(totalMonth)
                    .soDaDuyet((int) duyet)
                    .soTuChoi((int) reject)
                    .soChoXuLy((int) pending)
                    .build());
            }
        }
        
        Integer thangNhieu = monthlyStats.stream()
            .max((a, b) -> Integer.compare(a.getSoYeuCau(), b.getSoYeuCau()))
            .map(MonthlyStats::getThang)
            .orElse(null);
        
        // Thống kê bác sĩ
        long soBacSi = entities.stream()
            .map(e -> e.getBacSi().getBacSiID())
            .distinct()
            .count();
        
        double trungBinhYeuCau = soBacSi > 0 ? (total * 1.0 / soBacSi) : 0.0;
        
        // Thời gian xử lý
        List<org.example.demo.entity.BacSiNgayNghi> processedList = entities.stream()
            .filter(e -> e.getNgayDuyet() != null)
            .toList();
        
        double avgProcessTime = 0.0;
        if (!processedList.isEmpty()) {
            avgProcessTime = processedList.stream()
                .mapToLong(e -> 
                    java.time.temporal.ChronoUnit.DAYS.between(
                        e.getCreatedAt().toLocalDate(),
                        e.getNgayDuyet().toLocalDate()
                    )
                )
                .average()
                .orElse(0.0);
        }
        
        // Yêu cầu chờ lâu nhất
        Integer maxWaitDays = entities.stream()
            .filter(e -> e.getTrangThai() == org.example.demo.enums.TrangThaiNghi.CHO_DUYET)
            .mapToInt(e -> 
                (int) java.time.temporal.ChronoUnit.DAYS.between(
                    e.getCreatedAt().toLocalDate(),
                    java.time.LocalDate.now()
                )
            )
            .max()
            .orElse(0);
        
        return NgayNghiStatisticsResponse.builder()
            .tongYeuCau(total)
            .choXuLy(choXuLy)
            .daDuyet(daDuyet)
            .tuChoi(tuChoi)
            .daBiHuy(huy)
            .tyLeDuyet(Math.round(tyLeDuyet * 10) / 10.0)
            .thongKeTheoLoaiPhep(byLoaiPhep)
            .loaiPhepPhoBienNhat(loaiPhepPhoBien)
            .thongKeTheoLoaiNghi(byLoaiNghi)
            .loaiNghiPhoBienNhat(loaiNghiPhoBien)
            .thongKeTheoThang(monthlyStats)
            .thangNhieuYeuCauNhat(thangNhieu)
            .soBacSiCoYeuCau((int) soBacSi)
            .trungBinhYeuCauMotBacSi(Math.round(trungBinhYeuCau * 10) / 10.0)
            .thoiGianXuLyTrungBinh(Math.round(avgProcessTime * 10) / 10.0)
            .yeuCauChoLauNhat(maxWaitDays)
            .build();
    }
    
    /**
     * Create empty statistics
     */
    private static NgayNghiStatisticsResponse createEmpty() {
        return NgayNghiStatisticsResponse.builder()
            .tongYeuCau(0)
            .choXuLy(0)
            .daDuyet(0)
            .tuChoi(0)
            .daBiHuy(0)
            .tyLeDuyet(0.0)
            .thongKeTheoLoaiPhep(new HashMap<>())
            .loaiPhepPhoBienNhat(null)
            .thongKeTheoLoaiNghi(new HashMap<>())
            .loaiNghiPhoBienNhat(null)
            .thongKeTheoThang(new ArrayList<>())
            .thangNhieuYeuCauNhat(null)
            .soBacSiCoYeuCau(0)
            .trungBinhYeuCauMotBacSi(0.0)
            .thoiGianXuLyTrungBinh(0.0)
            .yeuCauChoLauNhat(0)
            .build();
    }
}

