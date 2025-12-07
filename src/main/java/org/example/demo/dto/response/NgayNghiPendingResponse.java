package org.example.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo.enums.LoaiNghi;
import org.example.demo.enums.LoaiNghiPhep;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * DTO cho response yêu cầu nghỉ CHỜ DUYỆT (dành cho Admin)
 * 
 * Tối ưu cho Admin workflow:
 * - Hiển thị thông tin quan trọng nhất
 * - Tính toán số ngày chờ xử lý
 * - Highlight yêu cầu cần xử lý gấp
 * - Hiển thị avatar & thông tin bác sĩ
 * 
 * @author Healthcare System Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response yêu cầu nghỉ chờ duyệt (cho Admin)")
public class NgayNghiPendingResponse {
    
    // ===== THÔNG TIN CƠ BẢN =====
    
    @Schema(description = "ID yêu cầu nghỉ", example = "1")
    private Integer nghiID;
    
    @Schema(description = "ID bác sĩ", example = "10")
    private Integer bacSiID;
    
    @Schema(description = "Tên bác sĩ", example = "BS. Nguyễn Văn A")
    private String tenBacSi;
    
    @Schema(description = "Avatar bác sĩ", example = "https://storage.example.com/avatar/123.jpg")
    private String avatarBacSi;
    
    @Schema(description = "Chuyên khoa", example = "Nội Khoa")
    private String tenChuyenKhoa;
    
    @Schema(description = "Trình độ", example = "Thạc sĩ")
    private String trinhDo;
    
    // ===== THÔNG TIN YÊU CẦU NGHỈ =====
    
    @Schema(
        description = "Loại nghỉ",
        example = "NGAY_CU_THE",
        allowableValues = {"NGAY_CU_THE", "CA_CU_THE", "CA_HANG_TUAN"}
    )
    private LoaiNghi loaiNghi;
    
    @Schema(description = "Mô tả loại nghỉ", example = "Nghỉ ngày cụ thể")
    private String moTaLoaiNghi;
    
    @Schema(description = "Thời gian nghỉ (formatted)", example = "Ngày 25/12/2025 - Ca chiều")
    private String thoiGianNghi;
    
    @Schema(description = "Lý do xin nghỉ", example = "Về quê ăn tết cùng gia đình")
    private String lyDo;
    
    @Schema(
        description = "Loại nghỉ phép",
        example = "PHEP_NAM",
        allowableValues = {"PHEP_NAM", "OM", "CONG_TAC", "KHAC"}
    )
    private LoaiNghiPhep loaiNghiPhep;
    
    @Schema(description = "Mô tả loại phép", example = "Nghỉ phép năm")
    private String moTaLoaiPhep;
    
    @Schema(description = "File đính kèm", example = "https://storage.example.com/files/don-xin-nghi-123.pdf")
    private String fileDinhKem;
    
    @Schema(description = "Có file đính kèm không", example = "true")
    private Boolean hasAttachment;
    
    // ===== THÔNG TIN NGÀY PHÉP =====
    
    @Schema(description = "Tổng số ngày phép/năm", example = "12")
    private Integer soNgayPhepNam;
    
    @Schema(description = "Số ngày phép đã sử dụng", example = "5")
    private Integer soNgayPhepDaSuDung;
    
    @Schema(description = "Số ngày phép còn lại", example = "7")
    private Integer soNgayPhepConLai;
    
    @Schema(description = "Có đủ ngày phép không (nếu loaiNghiPhep = PHEP_NAM)", example = "true")
    private Boolean duNgayPhep;
    
    // ===== METADATA & STATUS =====
    
    @Schema(description = "Ngày tạo yêu cầu", example = "2025-11-20T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Schema(description = "Số ngày chờ xử lý", example = "3")
    private Integer soNgayChoXuLy;
    
    @Schema(description = "Cần xử lý gấp (> 3 ngày)", example = "true")
    private Boolean canXuLyGap;
    
    @Schema(description = "Màu sắc priority (cho UI)", example = "#FF5722")
    private String mauSacPriority;
    
    @Schema(description = "Mô tả priority", example = "Khẩn cấp")
    private String moTaPriority;
    
    /**
     * Static Factory Method: Tạo từ Entity
     */
    public static NgayNghiPendingResponse fromEntity(
        org.example.demo.entity.BacSiNgayNghi entity
    ) {
        // Get BacSi info
        org.example.demo.entity.BacSi bacSi = entity.getBacSi();
        org.example.demo.entity.NguoiDung nguoiDung =
            bacSi != null ? bacSi.getNguoiDung() : null;
        
        NgayNghiPendingResponseBuilder builder = NgayNghiPendingResponse.builder()
            .nghiID(entity.getNghiID())
            .bacSiID(entity.getBacSi().getBacSiID())
            .loaiNghi(entity.getLoaiNghi())
            .moTaLoaiNghi(getMoTaLoaiNghi(entity.getLoaiNghi()))
            .thoiGianNghi(buildThoiGianNghi(entity))
            .lyDo(entity.getLyDo())
            .loaiNghiPhep(entity.getLoaiNghiPhep())
            .moTaLoaiPhep(getMoTaLoaiPhep(entity.getLoaiNghiPhep()))
            .fileDinhKem(entity.getFileDinhKem())
            .hasAttachment(entity.getFileDinhKem() != null && !entity.getFileDinhKem().isBlank())
            .createdAt(entity.getCreatedAt());
        
        // Bac si info
        if (nguoiDung != null) {
            builder.tenBacSi(nguoiDung.getHoTen());
            builder.avatarBacSi(nguoiDung.getAvatarUrl());
        }
        
        if (bacSi != null) {
            builder.tenChuyenKhoa(
                bacSi.getChuyenKhoa() != null ? bacSi.getChuyenKhoa().getTenChuyenKhoa() : null
            );
            builder.trinhDo(
                bacSi.getTrinhDo() != null ? bacSi.getTrinhDo().getTenTrinhDo() : null
            );
            
            // Ngày phép info
            builder.soNgayPhepNam(bacSi.getSoNgayPhepNam());
            builder.soNgayPhepDaSuDung(bacSi.getSoNgayPhepDaSuDung());
            
            Integer conLai = (bacSi.getSoNgayPhepNam() != null && bacSi.getSoNgayPhepDaSuDung() != null)
                ? bacSi.getSoNgayPhepNam() - bacSi.getSoNgayPhepDaSuDung()
                : 0;
            builder.soNgayPhepConLai(conLai);
            builder.duNgayPhep(conLai > 0);
        }
        
        // Calculate số ngày chờ xử lý
        int soNgayCho = (int) ChronoUnit.DAYS.between(
            entity.getCreatedAt().toLocalDate(),
            java.time.LocalDate.now()
        );
        builder.soNgayChoXuLy(soNgayCho);
        
        // Priority
        boolean isUrgent = soNgayCho > 3;
        builder.canXuLyGap(isUrgent);
        builder.mauSacPriority(isUrgent ? "#FF5722" : "#FFC107");
        builder.moTaPriority(isUrgent ? "Khẩn cấp" : "Bình thường");
        
        return builder.build();
    }
    
    // ===== HELPER METHODS =====
    
    private static String getMoTaLoaiNghi(LoaiNghi loaiNghi) {
        return switch (loaiNghi) {
            case NGAY_CU_THE -> "Nghỉ ngày cụ thể";
            case CA_CU_THE -> "Nghỉ ca cụ thể";
            case CA_HANG_TUAN -> "Nghỉ ca hàng tuần";
        };
    }
    
    private static String getMoTaLoaiPhep(LoaiNghiPhep loaiPhep) {
        return switch (loaiPhep) {
            case PHEP_NAM -> "Nghỉ phép năm";
            case OM -> "Nghỉ ốm";
            case CONG_TAC -> "Công tác";
            case KHAC -> "Lý do khác";
        };
    }
    
    private static String buildThoiGianNghi(
        org.example.demo.entity.BacSiNgayNghi entity
    ) {
        StringBuilder sb = new StringBuilder();
        
        switch (entity.getLoaiNghi()) {
            case NGAY_CU_THE:
                sb.append("Ngày ").append(formatDate(entity.getNgayNghiCuThe()));
                break;
                
            case CA_CU_THE:
                if (entity.getCa() != null) {
                    sb.append("Ca ").append(entity.getCa().getTenCa()).append(" - ");
                } else {
                    sb.append("Cả ngày - ");
                }
                sb.append("Ngày ").append(formatDate(entity.getNgayNghiCuThe()));
                break;
                
            case CA_HANG_TUAN:
                sb.append("Mỗi ").append(getTenThu(entity.getThuTrongTuan()));
                if (entity.getCa() != null) {
                    sb.append(" - Ca ").append(entity.getCa().getTenCa());
                } else {
                    sb.append(" (cả ngày)");
                }
                break;
        }
        
        return sb.toString();
    }
    
    private static String formatDate(java.time.LocalDate date) {
        if (date == null) return "";
        return String.format("%02d/%02d/%d",
            date.getDayOfMonth(),
            date.getMonthValue(),
            date.getYear()
        );
    }
    
    private static String getTenThu(Integer thu) {
        if (thu == null) return "";
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

