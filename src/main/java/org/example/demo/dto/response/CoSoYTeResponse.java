package org.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.example.demo.entity.CoSoYTe;

@Data
@Builder
@Schema(description = "Thông tin cơ sở y tế")
public class CoSoYTeResponse {

    @Schema(description = "ID cơ sở", example = "1")
    private Integer coSoID;

    @Schema(description = "Tên cơ sở", example = "Bệnh viện Đa khoa ABC")
    private String tenCoSo;

    @Schema(description = "Địa chỉ", example = "123 Lê Lợi, Quận 1, TP.HCM")
    private String diaChi;

    @Schema(description = "Số điện thoại", example = "02812345678")
    private String soDienThoai;

    @Schema(description = "Email", example = "contact@abc-hospital.com")
    private String email;

    @Schema(description = "Website", example = "https://abc-hospital.com")
    private String website;

    @Schema(description = "Mô tả")
    private String moTa;

    @Schema(description = "Ảnh đại diện")
    private String anhDaiDien;

    @Schema(description = "Logo")
    private String logo;

    @Schema(description = "Giờ làm việc", example = "08:00 - 17:00")
    private String gioLamViec;

    @Schema(description = "Ngày làm việc", example = "Thứ 2 - Thứ 7")
    private String ngayLamViec;

    public static CoSoYTeResponse fromEntity(CoSoYTe entity) {
        return CoSoYTeResponse.builder()
            .coSoID(entity.getCoSoID())
            .tenCoSo(entity.getTenCoSo())
            .diaChi(entity.getDiaChi())
            .soDienThoai(entity.getSoDienThoai())
            .email(entity.getEmail())
            .website(entity.getWebsite())
            .moTa(entity.getMoTa())
            .anhDaiDien(entity.getAnhDaiDien())
            .logo(entity.getLogo())
            .gioLamViec(entity.getGioLamViec())
            .ngayLamViec(entity.getNgayLamViec())
            .build();
    }
}


