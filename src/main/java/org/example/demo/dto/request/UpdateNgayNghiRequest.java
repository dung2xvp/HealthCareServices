package org.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request Bác sĩ chỉnh sửa yêu cầu nghỉ
 * 
 * Business Rules:
 * 1. CHỈ được update khi trangThai = CHO_DUYET
 * 2. Nếu đã duyệt/từ chối thì KHÔNG cho phép update
 * 3. CHỈ cho phép update các field: lyDo, fileDinhKem
 * 4. KHÔNG cho phép thay đổi: loaiNghi, ngayNghiCuThe, thuTrongTuan, ca, loaiNghiPhep
 * 5. Nếu muốn thay đổi thông tin quan trọng → Hủy request cũ + Tạo request mới
 * 
 * @author Healthcare System Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request Bác sĩ chỉnh sửa yêu cầu nghỉ (chỉ khi status = CHO_DUYET)")
public class UpdateNgayNghiRequest {
    
    /**
     * Lý do xin nghỉ (mới)
     * Optional - nếu null thì không thay đổi
     * Nếu có giá trị thì phải từ 10-500 ký tự
     */
    @Size(min = 10, max = 500, message = "Lý do phải từ 10-500 ký tự")
    @Schema(
        description = "Lý do xin nghỉ (mới)",
        example = "Cập nhật lý do: Về quê ăn tết cùng gia đình và thăm bệnh nhân cũ",
        minLength = 10,
        maxLength = 500
    )
    private String lyDo;
    
    /**
     * File đính kèm (mới)
     * Optional - nếu null thì không thay đổi
     * Nếu có giá trị thì max 255 ký tự
     */
    @Size(max = 255, message = "URL file đính kèm không được vượt quá 255 ký tự")
    @Schema(
        description = "URL file đính kèm (mới)",
        example = "https://storage.example.com/files/don-xin-nghi-updated.pdf",
        maxLength = 255
    )
    private String fileDinhKem;
    
    /**
     * Custom validation method
     * Gọi trong Service để validate business logic
     */
    public void validateBusinessRules() {
        // Rule 1: Ít nhất 1 field phải được cập nhật
        if (!hasAnyUpdate()) {
            throw new IllegalArgumentException(
                "Phải cập nhật ít nhất 1 trường (lý do hoặc file đính kèm)"
            );
        }
        
        // Rule 2: Nếu cập nhật lý do thì phải hợp lệ
        if (lyDo != null && !lyDo.isBlank()) {
            if (lyDo.trim().length() < 10) {
                throw new IllegalArgumentException(
                    "Lý do phải ít nhất 10 ký tự"
                );
            }
        }
    }
    
    /**
     * Helper method: Check xem có update gì không
     */
    public boolean hasAnyUpdate() {
        return hasLyDoUpdate() || hasFileDinhKemUpdate();
    }
    
    /**
     * Helper method: Check xem có update lý do không
     */
    public boolean hasLyDoUpdate() {
        return lyDo != null && !lyDo.isBlank();
    }
    
    /**
     * Helper method: Check xem có update file đính kèm không
     */
    public boolean hasFileDinhKemUpdate() {
        return fileDinhKem != null && !fileDinhKem.isBlank();
    }
    
    /**
     * Helper method: Lấy summary của update
     */
    public String getUpdateSummary() {
        if (!hasAnyUpdate()) {
            return "Không có thay đổi";
        }
        
        StringBuilder sb = new StringBuilder("Cập nhật: ");
        if (hasLyDoUpdate()) {
            sb.append("Lý do");
        }
        if (hasFileDinhKemUpdate()) {
            if (hasLyDoUpdate()) {
                sb.append(", ");
            }
            sb.append("File đính kèm");
        }
        
        return sb.toString();
    }
    
    /**
     * Static method: Tạo request chỉ update lý do
     */
    public static UpdateNgayNghiRequest updateLyDoOnly(String lyDo) {
        UpdateNgayNghiRequest request = new UpdateNgayNghiRequest();
        request.setLyDo(lyDo);
        return request;
    }
    
    /**
     * Static method: Tạo request chỉ update file đính kèm
     */
    public static UpdateNgayNghiRequest updateFileDinhKemOnly(String fileDinhKem) {
        UpdateNgayNghiRequest request = new UpdateNgayNghiRequest();
        request.setFileDinhKem(fileDinhKem);
        return request;
    }
}

