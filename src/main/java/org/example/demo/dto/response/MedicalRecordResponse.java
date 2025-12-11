package org.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo.entity.HoSoBenhAn;
import org.example.demo.entity.NguoiDung;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin hồ sơ bệnh án")
public class MedicalRecordResponse {

    @Schema(description = "ID hồ sơ")
    private Integer hoSoID;

    @Schema(description = "ID bệnh nhân")
    private Integer benhNhanID;

    @Schema(description = "Tên bệnh nhân")
    private String hoTenBenhNhan;

    @Schema(description = "Email bệnh nhân")
    private String emailBenhNhan;

    @Schema(description = "Số điện thoại bệnh nhân")
    private String soDienThoaiBenhNhan;

    @Schema(description = "Nhóm máu")
    private String nhomMau;

    @Schema(description = "Chiều cao (cm)")
    private BigDecimal chieuCao;

    @Schema(description = "Cân nặng (kg)")
    private BigDecimal canNang;

    @Schema(description = "Dị ứng")
    private String diUng;

    @Schema(description = "Bệnh mãn tính / tiền sử")
    private String benhManTinh;

    @Schema(description = "Thuốc đang dùng")
    private String thuocDangDung;

    @Schema(description = "Phẫu thuật đã qua")
    private String phauThuatDaQua;

    @Schema(description = "Tiền sử gia đình")
    private String tienSuGiaDinh;

    @Schema(description = "Hút thuốc")
    private Boolean hutThuoc;

    @Schema(description = "Uống rượu")
    private Boolean uongRuou;

    @Schema(description = "Ngày tạo")
    private LocalDateTime createdAt;

    @Schema(description = "Ngày cập nhật")
    private LocalDateTime updatedAt;

    public static MedicalRecordResponse fromEntity(HoSoBenhAn record) {
        NguoiDung patient = record.getBenhNhan();
        return MedicalRecordResponse.builder()
                .hoSoID(record.getHoSoID())
                .benhNhanID(patient != null ? patient.getNguoiDungID() : null)
                .hoTenBenhNhan(patient != null ? patient.getHoTen() : null)
                .emailBenhNhan(patient != null ? patient.getEmail() : null)
                .soDienThoaiBenhNhan(patient != null ? patient.getSoDienThoai() : null)
                .nhomMau(record.getNhomMau())
                .chieuCao(record.getChieuCao())
                .canNang(record.getCanNang())
                .diUng(record.getDiUng())
                .benhManTinh(record.getBenhManTinh())
                .thuocDangDung(record.getThuocDangDung())
                .phauThuatDaQua(record.getPhauThuatDaQua())
                .tienSuGiaDinh(record.getTienSuGiaDinh())
                .hutThuoc(record.getHutThuoc())
                .uongRuou(record.getUongRuou())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}

