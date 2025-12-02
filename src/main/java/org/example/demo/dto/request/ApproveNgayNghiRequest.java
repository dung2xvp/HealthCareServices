package org.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho request Admin duyệt/từ chối yêu cầu nghỉ của bác sĩ
 * 
 * Business Rules:
 * 1. Chỉ Admin mới có quyền duyệt/từ chối
 * 2. Chỉ duyệt được yêu cầu có status = CHO_DUYET
 * 3. Có thể duyệt nhiều yêu cầu cùng lúc (batch approval)
 * 4. Nếu action = REJECT thì lyDoTuChoi là bắt buộc
 * 5. Nếu action = APPROVE thì check:
 *    - Bác sĩ còn đủ ngày phép không (nếu loaiNghiPhep = PHEP_NAM)
 *    - Không conflict với lịch đã được duyệt
 * 
 * @author Healthcare System Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request Admin duyệt/từ chối yêu cầu nghỉ")
public class ApproveNgayNghiRequest {
    
    /**
     * Danh sách ID yêu cầu nghỉ cần xử lý
     * Required, không được rỗng
     * Có thể duyệt nhiều yêu cầu cùng lúc
     */
    @NotEmpty(message = "Danh sách yêu cầu nghỉ không được rỗng")
    @Schema(
        description = "Danh sách ID yêu cầu nghỉ cần xử lý",
        example = "[1, 2, 3]"
    )
    private List<Integer> nghiIDs;
    
    /**
     * Hành động: APPROVE hoặc REJECT
     * Required
     */
    @NotNull(message = "Hành động không được để trống")
    @Schema(
        description = "Hành động (APPROVE = Duyệt, REJECT = Từ chối)",
        example = "APPROVE",
        allowableValues = {"APPROVE", "REJECT"}
    )
    private ApprovalAction action;
    
    /**
     * Lý do từ chối
     * Required nếu action = REJECT
     * Optional nếu action = APPROVE
     */
    @Size(min = 10, max = 500, message = "Lý do từ chối phải từ 10-500 ký tự")
    @Schema(
        description = "Lý do từ chối (required nếu action = REJECT)",
        example = "Không đủ số ngày phép còn lại",
        minLength = 10,
        maxLength = 500
    )
    private String lyDoTuChoi;
    
    /**
     * Ghi chú của Admin (optional)
     */
    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    @Schema(
        description = "Ghi chú của Admin",
        example = "Đã xác nhận với trưởng khoa",
        maxLength = 500
    )
    private String ghiChu;
    
    /**
     * Enum cho approval action
     */
    public enum ApprovalAction {
        APPROVE,  // Duyệt
        REJECT    // Từ chối
    }
    
    /**
     * Custom validation method
     * Gọi trong Service để validate business logic
     */
    public void validateBusinessRules() {
        // Rule 1: Danh sách không được rỗng
        if (nghiIDs == null || nghiIDs.isEmpty()) {
            throw new IllegalArgumentException(
                "Danh sách yêu cầu nghỉ không được rỗng"
            );
        }
        
        // Rule 2: Action required
        if (action == null) {
            throw new IllegalArgumentException(
                "Hành động (APPROVE/REJECT) không được để trống"
            );
        }
        
        // Rule 3: Nếu REJECT thì phải có lý do
        if (action == ApprovalAction.REJECT) {
            if (lyDoTuChoi == null || lyDoTuChoi.trim().isEmpty()) {
                throw new IllegalArgumentException(
                    "Lý do từ chối không được để trống khi từ chối yêu cầu"
                );
            }
            
            if (lyDoTuChoi.trim().length() < 10) {
                throw new IllegalArgumentException(
                    "Lý do từ chối phải ít nhất 10 ký tự"
                );
            }
        }
        
        // Rule 4: Không cho phép list quá lớn (max 50 items)
        if (nghiIDs.size() > 50) {
            throw new IllegalArgumentException(
                "Không thể xử lý quá 50 yêu cầu cùng lúc"
            );
        }
        
        // Rule 5: Không có ID trùng lặp
        long distinctCount = nghiIDs.stream().distinct().count();
        if (distinctCount < nghiIDs.size()) {
            throw new IllegalArgumentException(
                "Danh sách yêu cầu có ID trùng lặp"
            );
        }
    }
    
    /**
     * Helper method: Check xem có phải approve không
     */
    public boolean isApprove() {
        return action == ApprovalAction.APPROVE;
    }
    
    /**
     * Helper method: Check xem có phải reject không
     */
    public boolean isReject() {
        return action == ApprovalAction.REJECT;
    }
    
    /**
     * Helper method: Lấy số lượng yêu cầu cần xử lý
     */
    public int getTotalRequests() {
        return nghiIDs != null ? nghiIDs.size() : 0;
    }
    
    /**
     * Helper method: Lấy mô tả action
     */
    public String getActionDisplay() {
        if (action == null) return "Không xác định";
        return switch (action) {
            case APPROVE -> "Duyệt yêu cầu";
            case REJECT -> "Từ chối yêu cầu";
        };
    }
    
    /**
     * Helper method: Lấy summary
     */
    public String getSummary() {
        return String.format(
            "%s %d yêu cầu nghỉ",
            getActionDisplay(),
            getTotalRequests()
        );
    }
}

