package org.example.demo.dto.request;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChuyenKhoaRequest {
    
    @NotBlank(message = "Tên chuyên khoa không được để trống")
    @Size(min = 2, max = 100, message = "Tên chuyên khoa phải từ 2-100 ký tự")
    private String tenChuyenKhoa;

    @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
    private String moTa;

    @Size(max = 255, message = "Đường dẫn ảnh không được quá 255 ký tự")
    private String anhDaiDien;
    
    @Min(value = 0, message = "Thứ tự hiển thị phải >= 0")
    private Integer thuTuHienThi = 0;
}
