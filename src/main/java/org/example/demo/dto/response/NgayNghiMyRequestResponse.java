package org.example.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo.enums.LoaiNghi;
import org.example.demo.enums.LoaiNghiPhep;
import org.example.demo.enums.TrangThaiNghi;

import java.time.LocalDateTime;

/**
 * DTO cho response yêu cầu nghỉ CỦA BÁC SĨ (dành cho Bác sĩ xem yêu cầu của mình)
 * 
 * Tối ưu cho Doctor workflow:
 * - Hiển thị trạng thái yêu cầu
 * - Lý do từ chối (nếu có)
 * - Actions có thể thực hiện (edit, cancel)
 * - Simplified info (không cần thông tin bác sĩ vì đã biết)
 * 
 * @author Healthcare System Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response yêu cầu nghỉ của bác sĩ (xem yêu cầu của mình)")
public class NgayNghiMyRequestResponse {
    
    // ===== THÔNG TIN CƠ BẢN =====
    
    @Schema(description = "ID yêu cầu nghỉ", example = "1")
    private Integer nghiID;
    
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
    
    // ===== TRẠNG THÁI =====
    
    @Schema(
        description = "Trạng thái yêu cầu",
        example = "CHO_DUYET",
        allowableValues = {"CHO_DUYET", "DA_DUYET", "TU_CHOI", "HUY"}
    )
    private TrangThaiNghi trangThai;
    
    @Schema(description = "Mô tả trạng thái", example = "Chờ duyệt")
    private String moTaTrangThai;
    
    @Schema(description = "Màu sắc trạng thái (cho UI)", example = "#FFA500")
    private String mauSacTrangThai;
    
    @Schema(description = "Icon trạng thái", example = "clock")
    private String iconTrangThai;
    
    // ===== THÔNG TIN DUYỆT =====
    
    @Schema(description = "Tên người duyệt", example = "Admin")
    private String tenNguoiDuyet;
    
    @Schema(description = "Ngày duyệt/từ chối", example = "2025-11-28T14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime ngayDuyet;
    
    @Schema(description = "Lý do từ chối (nếu bị từ chối)", example = "Không đủ số ngày phép")
    private String lyDoTuChoi;
    
    // ===== METADATA =====
    
    @Schema(description = "Ngày tạo yêu cầu", example = "2025-11-20T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Schema(description = "Ngày cập nhật cuối", example = "2025-11-28T14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // ===== ACTIONS =====
    
    @Schema(description = "Có thể chỉnh sửa không (true nếu status = CHO_DUYET)", example = "true")
    private Boolean canEdit;
    
    @Schema(description = "Có thể hủy không (true nếu status = CHO_DUYET hoặc DA_DUYET)", example = "true")
    private Boolean canCancel;
    
    @Schema(description = "Có thể xem lại không (true nếu status = TU_CHOI hoặc HUY)", example = "false")
    private Boolean canResubmit;
    
    @Schema(description = "Message hướng dẫn action", example = "Bạn có thể chỉnh sửa hoặc hủy yêu cầu này")
    private String actionMessage;
    
    /**
     * Static Factory Method: Tạo từ Entity
     */
    public static NgayNghiMyRequestResponse fromEntity(
        org.example.demo.entity.BacSiNgayNghi entity
    ) {
        NgayNghiMyRequestResponseBuilder builder = NgayNghiMyRequestResponse.builder()
            .nghiID(entity.getNghiID())
            .loaiNghi(entity.getLoaiNghi())
            .moTaLoaiNghi(getMoTaLoaiNghi(entity.getLoaiNghi()))
            .thoiGianNghi(buildThoiGianNghi(entity))
            .lyDo(entity.getLyDo())
            .loaiNghiPhep(entity.getLoaiNghiPhep())
            .moTaLoaiPhep(getMoTaLoaiPhep(entity.getLoaiNghiPhep()))
            .fileDinhKem(entity.getFileDinhKem())
            .trangThai(entity.getTrangThai())
            .moTaTrangThai(entity.getTrangThai().getMoTa())
            .mauSacTrangThai(entity.getTrangThai().getMauSac())
            .iconTrangThai(getIconTrangThai(entity.getTrangThai()))
            .ngayDuyet(entity.getNgayDuyet())
            .lyDoTuChoi(entity.getLyDoTuChoi())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt());
        
        // Người duyệt
        if (entity.getNguoiDuyet() != null) {
            builder.tenNguoiDuyet(entity.getNguoiDuyet().getHoTen());
        }
        
        // Actions
        TrangThaiNghi status = entity.getTrangThai();
        builder.canEdit(status == TrangThaiNghi.CHO_DUYET);
        builder.canCancel(status == TrangThaiNghi.CHO_DUYET || status == TrangThaiNghi.DA_DUYET);
        builder.canResubmit(status == TrangThaiNghi.TU_CHOI || status == TrangThaiNghi.HUY);
        builder.actionMessage(buildActionMessage(status));
        
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
    
    private static String getIconTrangThai(TrangThaiNghi trangThai) {
        return switch (trangThai) {
            case CHO_DUYET -> "clock";
            case DA_DUYET -> "check-circle";
            case TU_CHOI -> "x-circle";
            case HUY -> "slash";
        };
    }
    
    private static String buildActionMessage(TrangThaiNghi trangThai) {
        return switch (trangThai) {
            case CHO_DUYET -> "Bạn có thể chỉnh sửa hoặc hủy yêu cầu này";
            case DA_DUYET -> "Yêu cầu đã được duyệt. Bạn có thể hủy nếu cần";
            case TU_CHOI -> "Yêu cầu bị từ chối. Bạn có thể tạo yêu cầu mới";
            case HUY -> "Yêu cầu đã bị hủy. Bạn có thể tạo yêu cầu mới";
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

