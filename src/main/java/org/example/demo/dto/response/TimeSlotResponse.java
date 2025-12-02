package org.example.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO Response cho 1 slot thời gian (30 phút)
 * Dùng trong AvailableSlotsResponse
 * 
 * Example:
 * - gioKham: "08:00"
 * - gioBatDau: "08:00"
 * - gioKetThuc: "08:30"
 * - available: true
 * - label: "08:00 - 08:30"
 * 
 * @author Healthcare System Team
 * @version 1.0 - Phase 2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin 1 slot thời gian 30 phút")
public class TimeSlotResponse {
    
    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Giờ khám (dùng để book)", example = "08:00")
    private LocalTime gioKham;
    
    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Giờ bắt đầu slot", example = "08:00")
    private LocalTime gioBatDau;
    
    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Giờ kết thúc slot", example = "08:30")
    private LocalTime gioKetThuc;
    
    @Schema(description = "Slot có còn trống không?", example = "true")
    private Boolean available;
    
    @Schema(description = "Label hiển thị UI", example = "08:00 - 08:30")
    private String label;
    
    // ===== FACTORY METHOD =====
    
    public static TimeSlotResponse from(LocalTime gioKham, boolean available) {
        return TimeSlotResponse.builder()
                .gioKham(gioKham)
                .gioBatDau(gioKham)
                .gioKetThuc(gioKham.plusMinutes(30))
                .available(available)
                .label(String.format("%s - %s", 
                    gioKham.toString(), 
                    gioKham.plusMinutes(30).toString()))
                .build();
    }
}

