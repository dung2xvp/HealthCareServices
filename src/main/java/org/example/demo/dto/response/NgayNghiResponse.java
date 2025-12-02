package org.example.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo.enums.CaLamViec;
import org.example.demo.enums.LoaiNghi;
import org.example.demo.enums.LoaiNghiPhep;
import org.example.demo.enums.TrangThaiNghi;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO cho response yêu cầu nghỉ của bác sĩ (thông tin cơ bản)
 * Dùng cho: Danh sách yêu cầu, tìm kiếm
 * 
 * @author Healthcare System Team
 * @version 1.0
 * @see NgayNghiDetailResponse Để lấy thông tin chi tiết đầy đủ
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response yêu cầu nghỉ của bác sĩ")
public class NgayNghiResponse {
    
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
    
    // ===== LOẠI NGHỈ =====
    
    @Schema(
        description = "Loại nghỉ",
        example = "NGAY_CU_THE",
        allowableValues = {"NGAY_CU_THE", "CA_CU_THE", "CA_HANG_TUAN"}
    )
    private LoaiNghi loaiNghi;
    
    @Schema(description = "Mô tả loại nghỉ", example = "Nghỉ ngày cụ thể")
    private String moTaLoaiNghi;
    
    // ===== THỜI GIAN NGHỈ =====
    
    @Schema(description = "Ngày nghỉ cụ thể (nếu có)", example = "2025-12-25")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngayNghiCuThe;
    
    @Schema(
        description = "Thứ trong tuần (2-8, nếu nghỉ hàng tuần)",
        example = "6",
        minimum = "2",
        maximum = "8"
    )
    private Integer thuTrongTuan;
    
    @Schema(description = "Tên thứ", example = "Thứ 6")
    private String tenThu;
    
    @Schema(
        description = "Ca nghỉ (null = cả ngày)",
        example = "CHIEU",
        allowableValues = {"SANG", "CHIEU", "TOI"}
    )
    private CaLamViec ca;
    
    @Schema(description = "Tên ca", example = "Ca chiều")
    private String tenCa;
    
    @Schema(description = "Mô tả thời gian nghỉ tổng hợp", example = "Ca chiều - Ngày 25/12/2025")
    private String thoiGianNghiDisplay;
    
    // ===== THÔNG TIN ĐƠN =====
    
    @Schema(description = "Lý do xin nghỉ", example = "Về quê ăn tết cùng gia đình")
    private String lyDo;
    
    @Schema(
        description = "Loại nghỉ phép",
        example = "PHEP_NAM",
        allowableValues = {"PHEP_NAM", "OM", "CONG_TAC", "KHAC"}
    )
    private LoaiNghiPhep loaiNghiPhep;
    
    @Schema(description = "Mô tả loại nghỉ phép", example = "Nghỉ phép năm")
    private String moTaLoaiPhep;
    
    @Schema(description = "URL file đính kèm", example = "https://storage.example.com/files/don-xin-nghi-123.pdf")
    private String fileDinhKem;
    
    // ===== TRẠNG THÁI & DUYỆT =====
    
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
    
    @Schema(description = "ID người duyệt", example = "1")
    private Integer nguoiDuyetID;
    
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
    
    // ===== COMPUTED FIELDS =====
    
    @Schema(description = "Số ngày chờ xử lý (nếu status = CHO_DUYET)", example = "3")
    private Integer soNgayChoXuLy;
    
    @Schema(description = "Có nghỉ cả ngày không", example = "true")
    private Boolean isNghiCaNgay;
    
    /**
     * Static Factory Method: Tạo từ Entity
     * Dùng trong Service để convert Entity → Response DTO
     */
    public static NgayNghiResponse fromEntity(
        org.example.demo.entity.BacSiNgayNghi entity
    ) {
        NgayNghiResponseBuilder builder = NgayNghiResponse.builder()
            .nghiID(entity.getNghiID())
            .bacSiID(entity.getBacSi().getBacSiID())
            .loaiNghi(entity.getLoaiNghi())
            .ngayNghiCuThe(entity.getNgayNghiCuThe())
            .thuTrongTuan(entity.getThuTrongTuan())
            .ca(entity.getCa())
            .lyDo(entity.getLyDo())
            .loaiNghiPhep(entity.getLoaiNghiPhep())
            .fileDinhKem(entity.getFileDinhKem())
            .trangThai(entity.getTrangThai())
            .ngayDuyet(entity.getNgayDuyet())
            .lyDoTuChoi(entity.getLyDoTuChoi())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .isNghiCaNgay(entity.getCa() == null);
        
        // Computed fields
        builder.moTaLoaiNghi(getMoTaLoaiNghi(entity.getLoaiNghi()));
        builder.moTaLoaiPhep(getMoTaLoaiPhep(entity.getLoaiNghiPhep()));
        builder.moTaTrangThai(entity.getTrangThai().getMoTa());
        builder.mauSacTrangThai(entity.getTrangThai().getMauSac());
        
        if (entity.getThuTrongTuan() != null) {
            builder.tenThu(getTenThu(entity.getThuTrongTuan()));
        }
        
        if (entity.getCa() != null) {
            builder.tenCa(entity.getCa().getTenCa());
        }
        
        builder.thoiGianNghiDisplay(buildThoiGianNghiDisplay(entity));
        
        if (entity.getTrangThai() == TrangThaiNghi.CHO_DUYET) {
            builder.soNgayChoXuLy(
                (int) java.time.temporal.ChronoUnit.DAYS.between(
                    entity.getCreatedAt().toLocalDate(), 
                    LocalDate.now()
                )
            );
        }
        
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
    
    private static String getTenThu(Integer thu) {
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
    
    private static String buildThoiGianNghiDisplay(
        org.example.demo.entity.BacSiNgayNghi entity
    ) {
        StringBuilder sb = new StringBuilder();
        
        switch (entity.getLoaiNghi()) {
            case NGAY_CU_THE:
                sb.append("Ngày ").append(entity.getNgayNghiCuThe());
                break;
                
            case CA_CU_THE:
                if (entity.getCa() != null) {
                    sb.append("Ca ").append(entity.getCa().getTenCa()).append(" - ");
                } else {
                    sb.append("Cả ngày - ");
                }
                sb.append("Ngày ").append(entity.getNgayNghiCuThe());
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
}

