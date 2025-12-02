package org.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho request bật/tắt nhiều ca làm việc cùng lúc
 * 
 * Use Cases:
 * 1. Tắt tất cả ca Chủ nhật (nghỉ cuối tuần)
 * 2. Tắt ca TOI trong tất cả các ngày
 * 3. Tắt tất cả ca trong kỳ nghỉ lễ
 * 4. Bật lại sau kỳ nghỉ lễ
 * 
 * Business Rules:
 * 1. Chỉ Admin mới có quyền toggle
 * 2. Có thể toggle nhiều ca cùng lúc (batch operation)
 * 3. Không xóa ca, chỉ đánh dấu isActive = true/false
 * 4. Có thể undo (bật lại các ca đã tắt)
 * 
 * @author Healthcare System Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request bật/tắt nhiều ca làm việc cùng lúc")
public class ToggleScheduleActiveRequest {
    
    /**
     * Danh sách ID cấu hình lịch làm việc cần toggle
     * Required, không được rỗng
     */
    @NotEmpty(message = "Danh sách cấu hình không được rỗng")
    @Schema(
        description = "Danh sách ID cấu hình lịch làm việc",
        example = "[1, 2, 3]"
    )
    private List<Integer> configIDs;
    
    /**
     * Trạng thái mới: true = Bật, false = Tắt
     * Required
     */
    @NotNull(message = "Trạng thái không được để trống")
    @Schema(
        description = "Trạng thái mới (true = Bật, false = Tắt)",
        example = "false"
    )
    private Boolean isActive;
    
    /**
     * Lý do toggle (optional)
     * VD: "Nghỉ lễ Tết", "Nghỉ cuối tuần"
     */
    @Schema(
        description = "Lý do toggle (optional)",
        example = "Nghỉ lễ Tết",
        maxLength = 255
    )
    private String reason;
    
    /**
     * Custom validation method
     */
    public void validateBusinessRules() {
        // Rule 1: Danh sách không được rỗng
        if (configIDs == null || configIDs.isEmpty()) {
            throw new IllegalArgumentException(
                "Danh sách cấu hình không được rỗng"
            );
        }
        
        // Rule 2: isActive required
        if (isActive == null) {
            throw new IllegalArgumentException(
                "Trạng thái (bật/tắt) không được để trống"
            );
        }
        
        // Rule 3: Không cho phép list quá lớn (max 50 items)
        if (configIDs.size() > 50) {
            throw new IllegalArgumentException(
                "Không thể xử lý quá 50 cấu hình cùng lúc"
            );
        }
        
        // Rule 4: Không có ID trùng lặp
        long distinctCount = configIDs.stream().distinct().count();
        if (distinctCount < configIDs.size()) {
            throw new IllegalArgumentException(
                "Danh sách cấu hình có ID trùng lặp"
            );
        }
    }
    
    /**
     * Helper method: Check xem có phải bật không
     */
    public boolean isEnable() {
        return Boolean.TRUE.equals(isActive);
    }
    
    /**
     * Helper method: Check xem có phải tắt không
     */
    public boolean isDisable() {
        return Boolean.FALSE.equals(isActive);
    }
    
    /**
     * Helper method: Lấy số lượng cấu hình
     */
    public int getTotalConfigs() {
        return configIDs != null ? configIDs.size() : 0;
    }
    
    /**
     * Helper method: Lấy action display
     */
    public String getActionDisplay() {
        return isEnable() ? "Bật" : "Tắt";
    }
    
    /**
     * Helper method: Lấy summary
     */
    public String getSummary() {
        String action = getActionDisplay();
        String reasonText = (reason != null && !reason.isBlank()) 
            ? " (" + reason + ")" 
            : "";
        
        return String.format(
            "%s %d ca làm việc%s",
            action,
            getTotalConfigs(),
            reasonText
        );
    }
    
    /**
     * Static factory: Tạo request tắt ca
     */
    public static ToggleScheduleActiveRequest disable(List<Integer> configIDs, String reason) {
        ToggleScheduleActiveRequest request = new ToggleScheduleActiveRequest();
        request.setConfigIDs(configIDs);
        request.setIsActive(false);
        request.setReason(reason);
        return request;
    }
    
    /**
     * Static factory: Tạo request bật ca
     */
    public static ToggleScheduleActiveRequest enable(List<Integer> configIDs, String reason) {
        ToggleScheduleActiveRequest request = new ToggleScheduleActiveRequest();
        request.setConfigIDs(configIDs);
        request.setIsActive(true);
        request.setReason(reason);
        return request;
    }
}

