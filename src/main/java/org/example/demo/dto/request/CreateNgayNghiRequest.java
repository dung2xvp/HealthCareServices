package org.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo.enums.CaLamViec;
import org.example.demo.enums.LoaiNghi;
import org.example.demo.enums.LoaiNghiPhep;

import java.time.LocalDate;

/**
 * DTO cho request tạo yêu cầu nghỉ của bác sĩ
 * 
 * Business Rules:
 * 1. Nếu loaiNghi = NGAY_CU_THE hoặc CA_CU_THE → ngayNghiCuThe required
 * 2. Nếu loaiNghi = CA_HANG_TUAN → thuTrongTuan required
 * 3. Ca = null → nghỉ cả ngày; Ca = SANG/CHIEU/TOI → nghỉ ca cụ thể
 * 4. Nếu loaiNghiPhep = PHEP_NAM → Check soNgayPhepConLai > 0
 * 5. Check conflict với lịch nghỉ đã được duyệt
 * 
 * @author Healthcare System Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request tạo yêu cầu nghỉ của bác sĩ")
public class CreateNgayNghiRequest {
    
    /**
     * Loại nghỉ:
     * - NGAY_CU_THE: Nghỉ cả ngày 25/12/2025
     * - CA_CU_THE: Nghỉ ca CHIEU ngày 25/12/2025
     * - CA_HANG_TUAN: Nghỉ ca SANG mỗi Thứ 7
     */
    @NotNull(message = "Loại nghỉ không được để trống")
    @Schema(
        description = "Loại nghỉ",
        example = "NGAY_CU_THE",
        allowableValues = {"NGAY_CU_THE", "CA_CU_THE", "CA_HANG_TUAN"}
    )
    private LoaiNghi loaiNghi;
    
    /**
     * Ngày nghỉ cụ thể
     * Required nếu loaiNghi = NGAY_CU_THE hoặc CA_CU_THE
     * NULL nếu loaiNghi = CA_HANG_TUAN
     */
    @Schema(
        description = "Ngày nghỉ cụ thể (required nếu loaiNghi = NGAY_CU_THE hoặc CA_CU_THE)",
        example = "2025-12-25"
    )
    private LocalDate ngayNghiCuThe;
    
    /**
     * Thứ trong tuần (2-8)
     * Required nếu loaiNghi = CA_HANG_TUAN
     * 2 = Thứ 2, 3 = Thứ 3, ..., 8 = Chủ nhật
     */
    @Min(value = 2, message = "Thứ trong tuần phải từ 2-8 (2=Thứ 2, 8=Chủ nhật)")
    @Max(value = 8, message = "Thứ trong tuần phải từ 2-8 (2=Thứ 2, 8=Chủ nhật)")
    @Schema(
        description = "Thứ trong tuần (2-8, required nếu loaiNghi = CA_HANG_TUAN)",
        example = "6",
        minimum = "2",
        maximum = "8"
    )
    private Integer thuTrongTuan;
    
    /**
     * Ca nghỉ: SANG, CHIEU, TOI
     * NULL = nghỉ cả ngày
     * Có giá trị = nghỉ ca cụ thể
     */
    @Schema(
        description = "Ca nghỉ (null = nghỉ cả ngày, SANG/CHIEU/TOI = nghỉ ca cụ thể)",
        example = "CHIEU",
        allowableValues = {"SANG", "CHIEU", "TOI"}
    )
    private CaLamViec ca;
    
    /**
     * Lý do xin nghỉ
     * Required, độ dài 10-500 ký tự
     */
    @NotBlank(message = "Lý do xin nghỉ không được để trống")
    @Size(min = 10, max = 500, message = "Lý do phải từ 10-500 ký tự")
    @Schema(
        description = "Lý do xin nghỉ",
        example = "Về quê ăn tết cùng gia đình",
        minLength = 10,
        maxLength = 500
    )
    private String lyDo;
    
    /**
     * Loại nghỉ phép:
     * - PHEP_NAM: Nghỉ phép năm (trừ ngày phép)
     * - OM: Nghỉ ốm (cần giấy khám bệnh)
     * - CONG_TAC: Công tác (không trừ phép)
     * - KHAC: Lý do khác
     */
    @NotNull(message = "Loại nghỉ phép không được để trống")
    @Schema(
        description = "Loại nghỉ phép",
        example = "PHEP_NAM",
        allowableValues = {"PHEP_NAM", "OM", "CONG_TAC", "KHAC"}
    )
    private LoaiNghiPhep loaiNghiPhep;
    
    /**
     * File đính kèm (đơn xin nghỉ, giấy khám bệnh...)
     * Lưu URL hoặc path sau khi upload
     * Optional, max 255 chars
     */
    @Size(max = 255, message = "URL file đính kèm không được vượt quá 255 ký tự")
    @Schema(
        description = "URL file đính kèm (đơn xin nghỉ, giấy khám bệnh)",
        example = "https://storage.example.com/files/don-xin-nghi-123.pdf",
        maxLength = 255
    )
    private String fileDinhKem;
    
    /**
     * Custom validation method
     * Gọi trong Service để validate business logic
     */
    public void validateBusinessRules() {
        // Rule 1: Nghỉ ngày/ca cụ thể phải có ngày
        if ((loaiNghi == LoaiNghi.NGAY_CU_THE || loaiNghi == LoaiNghi.CA_CU_THE) 
            && ngayNghiCuThe == null) {
            throw new IllegalArgumentException(
                "Phải chọn ngày nghỉ cụ thể cho loại nghỉ " + loaiNghi.name()
            );
        }
        
        // Rule 2: Nghỉ hàng tuần phải có thứ
        if (loaiNghi == LoaiNghi.CA_HANG_TUAN && thuTrongTuan == null) {
            throw new IllegalArgumentException(
                "Phải chọn thứ trong tuần cho loại nghỉ hàng tuần"
            );
        }
        
        // Rule 3: Nghỉ cả ngày thì không cần chọn ca
        if (loaiNghi == LoaiNghi.NGAY_CU_THE && ca != null) {
            throw new IllegalArgumentException(
                "Nghỉ cả ngày (NGAY_CU_THE) thì không cần chọn ca"
            );
        }
        
        // Rule 4: Ngày nghỉ phải trong tương lai
        if (ngayNghiCuThe != null && ngayNghiCuThe.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                "Không thể đăng ký nghỉ ngày đã qua"
            );
        }
        
        // Rule 5: Nghỉ ốm cần file đính kèm
        if (loaiNghiPhep == LoaiNghiPhep.OM && 
            (fileDinhKem == null || fileDinhKem.isBlank())) {
            throw new IllegalArgumentException(
                "Nghỉ ốm cần đính kèm giấy khám bệnh"
            );
        }
    }
    
    /**
     * Helper method: Kiểm tra có nghỉ cả ngày không
     */
    public boolean isNghiCaNgay() {
        return ca == null;
    }
    
    /**
     * Helper method: Lấy mô tả thời gian nghỉ
     */
    public String getMoTaThoiGianNghi() {
        StringBuilder sb = new StringBuilder();
        
        switch (loaiNghi) {
            case NGAY_CU_THE:
                sb.append("Ngày ").append(ngayNghiCuThe);
                break;
                
            case CA_CU_THE:
                sb.append("Ca ").append(ca != null ? ca.getTenCa() : "cả ngày")
                  .append(" - Ngày ").append(ngayNghiCuThe);
                break;
                
            case CA_HANG_TUAN:
                String tenThu = getTenThu(thuTrongTuan);
                sb.append("Mỗi ").append(tenThu);
                if (ca != null) {
                    sb.append(" - Ca ").append(ca.getTenCa());
                } else {
                    sb.append(" (cả ngày)");
                }
                break;
        }
        
        return sb.toString();
    }
    
    /**
     * Helper: Convert thuTrongTuan (2-8) sang tên thứ
     */
    private String getTenThu(Integer thu) {
        if (thu == null) return "";
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
}

