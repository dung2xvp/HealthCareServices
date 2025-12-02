package org.example.demo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import org.example.demo.enums.CaLamViec;
import java.time.LocalTime;

/** 
 * DTO cho request tạo/ cập nhật 1 ca làm việc mặc định
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request tạo/ cập nhật 1 ca làm việc mặc định")
public class LichLamViecRequest {
    
    /**
     * Thứ trong tuần (2-8)
     * Required nếu loaiNghi = CA_HANG_TUAN
     * 2 = Thứ 2, 3 = Thứ 3, ..., 8 = Chủ nhật
     */
    @Min(value = 2, message = "Thứ trong tuần phải từ 2-8 (2=Thứ 2, 8=Chủ nhật)")
    @Max(value = 8, message = "Thứ trong tuần phải từ 2-8 (2=Thứ 2, 8=Chủ nhật)")
    @Schema(description = "Thứ trong tuần (2-8)", example = "2")
    private Integer thuTrongTuan;
    
    /**
     * Ca làm việc: SANG, CHIEU, TOI
     */
    @NotNull(message = "Ca làm việc không được để trống")
    @Schema(description = "Ca làm việc", example = "SANG")
    private CaLamViec ca;
    
    /**
     * Thời gian bắt đầu
     */
    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @Schema(description = "Thời gian bắt đầu", example = "08:00:00")
    private LocalTime thoiGianBatDau;
    
    /**
     * Thời gian kết thúc
     */
    @NotNull(message = "Thời gian kết thúc không được để trống")
    @Schema(description = "Thời gian kết thúc", example = "12:00:00")
    private LocalTime thoiGianKetThuc;
    
    /**
     * Đang áp dụng
     */
    @Schema(description = "Đang áp dụng", example = "true")
    private Boolean isActive = true;
    
    /**
     * Ghi chú
     */
    @Size(max = 255, message = "Ghi chú không vượt quá 255 ký tự")
    @Schema(description = "Ghi chú", example = "Lịch làm việc buổi sáng")
    private String ghiChu;
    
    /**
     * Validation: Thời gian kết thúc phải sau thời gian bắt đầu
     */
    public void validateBusinessRules() {
        if (thoiGianKetThuc.isBefore(thoiGianBatDau)) {
            throw new IllegalArgumentException(
                "Thời gian kết thúc phải sau thời gian bắt đầu"
            );
        }
    }

    /**
     * Getter cho ca làm việc
     * @return ca làm việc
     */
    public CaLamViec getCa() {
        return ca;
    }
    
    /**
     * Getter cho thời gian bắt đầu
     * @return thời gian bắt đầu
     */
    public LocalTime getThoiGianBatDau() {
        return thoiGianBatDau;
    }
    
    /**
     * Getter cho thời gian kết thúc
     * @return thời gian kết thúc
     */
    public LocalTime getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }
    
    /**
     * Getter cho đang áp dụng
     * @return đang áp dụng
     */
    public Boolean getIsActive() {
        return isActive;
    }
    
    /**
     * Getter cho ghi chú
     * @return ghi chú
     */
    public String getGhiChu() {
        return ghiChu;
    }
    
    /**
     * Kiểm tra xung đột với lịch làm việc hiện có (cùng thứ trong tuần và ca)
     * 
     * @param existingSchedules Danh sách lịch làm việc đã tồn tại (cùng bác sĩ/cơ sở tuỳ use case)
     * @throws IllegalArgumentException nếu bị trùng ca và thứ trong tuần
     */
    public void validateNoConflict(java.util.List<LichLamViecRequest> existingSchedules) {
        if (existingSchedules == null) return;
        for (LichLamViecRequest item : existingSchedules) {
            if (Boolean.FALSE.equals(item.getIsActive())) continue; // chỉ kiểm tra với lịch đang áp dụng
            if (
                // so sánh cùng ca & cùng thứ
                this.getCa() == item.getCa()
                && this.thuTrongTuan == item.thuTrongTuan
            ) {
                throw new IllegalArgumentException(
                    String.format(
                        "Đã tồn tại lịch làm việc cho ca %s vào Thứ %d",
                        this.getCa(), this.thuTrongTuan
                    )
                );
            }
        }
    }
}
