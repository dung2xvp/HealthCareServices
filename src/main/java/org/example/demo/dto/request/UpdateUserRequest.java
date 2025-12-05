package org.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Yêu cầu cập nhật thông tin người dùng")
public class UpdateUserRequest {

    @Schema(description = "Họ và tên", example = "Nguyễn Văn A")
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String hoTen;

    @Schema(description = "Số điện thoại", example = "0912345678")
    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String soDienThoai;

    @Schema(description = "Địa chỉ", example = "123 Lê Lợi, Q.1, TP.HCM")
    @Size(max = 200, message = "Địa chỉ tối đa 200 ký tự")
    private String diaChi;

    @Schema(description = "Ngày sinh", example = "1995-10-01")
    @PastOrPresent(message = "Ngày sinh không hợp lệ")
    private LocalDate ngaySinh;

    @Schema(description = "Giới tính (0=khác/1=nam/2=nữ)", example = "1")
    private Integer gioiTinh;

    @Schema(description = "Ảnh đại diện (URL)", example = "https://example.com/avatar.png")
    @Size(max = 255, message = "Avatar URL tối đa 255 ký tự")
    private String avatarUrl;

    @Schema(description = "Email (chỉ hiển thị, không cho phép đổi)", example = "user@example.com")
    @Email(message = "Email không hợp lệ")
    private String email; // optional, sẽ bị bỏ qua khi update
}

