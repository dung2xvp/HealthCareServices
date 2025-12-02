package org.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO cho request tạo nhiều ca làm việc mặc định cùng lúc
 * 
 * Use Cases:
 * 1. Setup lịch ban đầu: Tạo 14 ca/tuần (7 ngày x 2 ca) một lần
 * 2. Thay đổi hàng loạt: Update nhiều ca cùng lúc
 * 
 * Business Rules:
 * 1. List không được rỗng (tối thiểu 1 ca)
 * 2. Tối đa 21 ca (7 ngày x 3 ca)
 * 3. Không được trùng lặp (cùng thứ + cùng ca)
 * 4. Mỗi item phải valid (validate business rules)
 * 
 * @author Healthcare System Team
 * @version 1.0
 * @see LichLamViecRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request tạo nhiều ca làm việc mặc định cùng lúc")
public class LichLamViecBulkCreateRequest {
    
    /**
     * Danh sách các ca làm việc cần tạo
     * Required, không được rỗng, tối thiểu 1 ca, tối đa 21 ca
     */
    @NotEmpty(message = "Danh sách lịch làm việc không được rỗng")
    @Size(min = 1, max = 21, message = "Danh sách phải từ 1-21 ca làm việc")
    @Valid // ✅ Validate từng item trong list
    @Schema(
        description = "Danh sách các ca làm việc cần tạo",
        minLength = 1,
        maxLength = 21
    )
    private List<LichLamViecRequest> schedules;
    
    /**
     * Custom validation method
     * Gọi trong Service để validate business logic
     */
    public void validateBusinessRules() {
        if (schedules == null || schedules.isEmpty()) {
            throw new IllegalArgumentException(
                "Danh sách lịch làm việc không được rỗng"
            );
        }
        
        // Rule 1: Validate từng item
        for (int i = 0; i < schedules.size(); i++) {
            LichLamViecRequest item = schedules.get(i);
            try {
                item.validateBusinessRules();
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    String.format("Lịch thứ %d không hợp lệ: %s", i + 1, e.getMessage())
                );
            }
        }
        
        // Rule 2: Validate không trùng lặp trong cùng request
        validateNoDuplicates();
        
        // Rule 3: Validate logic nghiệp vụ
        validateScheduleLogic();
    }
    
    /**
     * Validate không có lịch trùng lặp (cùng thứ + cùng ca)
     */
    private void validateNoDuplicates() {
        Set<String> uniqueKeys = new HashSet<>();
        
        for (int i = 0; i < schedules.size(); i++) {
            LichLamViecRequest item = schedules.get(i);
            String key = item.getThuTrongTuan() + "_" + item.getCa();
            
            if (uniqueKeys.contains(key)) {
                throw new IllegalArgumentException(
                    String.format(
                        "Lịch bị trùng lặp: %s - Ca %s (vị trí %d)",
                        getTenThu(item.getThuTrongTuan()),
                        item.getCa().getTenCa(),
                        i + 1
                    )
                );
            }
            
            uniqueKeys.add(key);
        }
    }
    
    /**
     * Validate logic nghiệp vụ
     * - Không nên có quá nhiều ca trong 1 ngày (max 3 ca)
     * - Thời gian các ca trong cùng ngày không được overlap
     */
    private void validateScheduleLogic() {
        // Group by thuTrongTuan
        var schedulesByDay = schedules.stream()
            .collect(Collectors.groupingBy(LichLamViecRequest::getThuTrongTuan));
        
        // Check mỗi ngày
        for (var entry : schedulesByDay.entrySet()) {
            Integer thu = entry.getKey();
            List<LichLamViecRequest> daySchedules = entry.getValue();
            
            // Check không quá 3 ca/ngày
            if (daySchedules.size() > 3) {
                throw new IllegalArgumentException(
                    String.format(
                        "%s có quá nhiều ca làm việc (%d ca). Tối đa 3 ca/ngày.",
                        getTenThu(thu),
                        daySchedules.size()
                    )
                );
            }
            
            // Check thời gian không overlap
            validateNoTimeOverlap(thu, daySchedules);
        }
    }
    
    /**
     * Validate thời gian các ca trong cùng ngày không overlap
     */
    private void validateNoTimeOverlap(Integer thu, List<LichLamViecRequest> daySchedules) {
        for (int i = 0; i < daySchedules.size(); i++) {
            for (int j = i + 1; j < daySchedules.size(); j++) {
                LichLamViecRequest schedule1 = daySchedules.get(i);
                LichLamViecRequest schedule2 = daySchedules.get(j);
                
                // Check overlap: schedule1.end > schedule2.start AND schedule2.end > schedule1.start
                boolean isOverlap = 
                    schedule1.getThoiGianKetThuc().isAfter(schedule2.getThoiGianBatDau()) &&
                    schedule2.getThoiGianKetThuc().isAfter(schedule1.getThoiGianBatDau());
                
                if (isOverlap) {
                    throw new IllegalArgumentException(
                        String.format(
                            "%s: Ca %s (%s-%s) và Ca %s (%s-%s) bị trùng giờ",
                            getTenThu(thu),
                            schedule1.getCa().getTenCa(),
                            schedule1.getThoiGianBatDau(),
                            schedule1.getThoiGianKetThuc(),
                            schedule2.getCa().getTenCa(),
                            schedule2.getThoiGianBatDau(),
                            schedule2.getThoiGianKetThuc()
                        )
                    );
                }
            }
        }
    }
    
    /**
     * Helper method: Lấy tên thứ
     */
    private String getTenThu(Integer thu) {
        if (thu == null) return "Không xác định";
        return switch (thu) {
            case 2 -> "Thứ 2";
            case 3 -> "Thứ 3";
            case 4 -> "Thứ 4";
            case 5 -> "Thứ 5";
            case 6 -> "Thứ 6";
            case 7 -> "Thứ 7";
            case 8 -> "Chủ nhật";
            default -> "Không xác định";
        };
    }
    
    /**
     * Helper method: Lấy tổng số ca trong request
     */
    public int getTotalSchedules() {
        return schedules != null ? schedules.size() : 0;
    }
    
    /**
     * Helper method: Lấy số ngày có lịch làm việc
     */
    public int getTotalDays() {
        if (schedules == null || schedules.isEmpty()) {
            return 0;
        }
        
        return (int) schedules.stream()
            .map(LichLamViecRequest::getThuTrongTuan)
            .distinct()
            .count();
    }
    
    /**
     * Helper method: Lấy tóm tắt request
     */
    public String getSummary() {
        return String.format(
            "Tạo %d ca làm việc cho %d ngày trong tuần",
            getTotalSchedules(),
            getTotalDays()
        );
    }
    
    /**
     * Helper method: Check xem có lịch cho ngày cụ thể không
     */
    public boolean hasScheduleForDay(Integer thuTrongTuan) {
        if (schedules == null) {
            return false;
        }
        
        return schedules.stream()
            .anyMatch(s -> s.getThuTrongTuan().equals(thuTrongTuan));
    }
    
    /**
     * Helper method: Lấy danh sách ca cho một ngày cụ thể
     */
    public List<LichLamViecRequest> getSchedulesForDay(Integer thuTrongTuan) {
        if (schedules == null) {
            return List.of();
        }
        
        return schedules.stream()
            .filter(s -> s.getThuTrongTuan().equals(thuTrongTuan))
            .collect(Collectors.toList());
    }
    
    /**
     * Static factory method: Tạo request cho 1 tuần full (7 ngày x 2 ca = 14 ca)
     * Use case: Setup lịch mặc định ban đầu
     */
    public static LichLamViecBulkCreateRequest createDefaultWeekSchedule() {
        List<LichLamViecRequest> schedules = new java.util.ArrayList<>();
        
        // 7 ngày trong tuần (2-8)
        for (int thu = 2; thu <= 8; thu++) {
            // Ca SANG: 8:00 - 12:00
            schedules.add(createSchedule(
                thu, 
                org.example.demo.enums.CaLamViec.SANG,
                java.time.LocalTime.of(8, 0),
                java.time.LocalTime.of(12, 0),
                "Lịch làm việc buổi sáng"
            ));
            
            // Ca CHIEU: 14:00 - 17:00
            schedules.add(createSchedule(
                thu,
                org.example.demo.enums.CaLamViec.CHIEU,
                java.time.LocalTime.of(14, 0),
                java.time.LocalTime.of(17, 0),
                "Lịch làm việc buổi chiều"
            ));
        }
        
        return new LichLamViecBulkCreateRequest(schedules);
    }
    
    /**
     * Helper: Tạo 1 lịch làm việc
     */
    private static LichLamViecRequest createSchedule(
        Integer thu,
        org.example.demo.enums.CaLamViec ca,
        java.time.LocalTime start,
        java.time.LocalTime end,
        String note
    ) {
        LichLamViecRequest request = new LichLamViecRequest();
        request.setThuTrongTuan(thu);
        request.setCa(ca);
        request.setThoiGianBatDau(start);
        request.setThoiGianKetThuc(end);
        request.setIsActive(true);
        request.setGhiChu(note);
        return request;
    }
}
