package org.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu cập nhật thông tin cơ sở y tế (các trường null sẽ được bỏ qua)")
public class UpdateCoSoYTeRequest {

    @Schema(description = "Tên cơ sở", example = "Bệnh viện Đa khoa ABC")
    @Size(max = 200)
    private String tenCoSo;

    @Schema(description = "Địa chỉ", example = "123 Lê Lợi, Quận 1, TP.HCM")
    @Size(max = 255)
    private String diaChi;

    @Schema(description = "Số điện thoại", example = "02812345678")
    @Size(max = 20)
    private String soDienThoai;

    @Schema(description = "Email", example = "contact@abc-hospital.com")
    @Email
    @Size(max = 100)
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
    @Size(max = 100)
    private String gioLamViec;

    @Schema(description = "Ngày làm việc", example = "Thứ 2 - Thứ 7")
    @Size(max = 100)
    private String ngayLamViec;
}


