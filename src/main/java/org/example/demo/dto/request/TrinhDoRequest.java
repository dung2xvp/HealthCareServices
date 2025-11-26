package org.example.demo.dto.request;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrinhDoRequest {  
    @NotBlank(message = "Tên trình độ không được để trống")
    @Size(min = 2, max = 50, message = "Tên trình độ phải từ 2-50 ký tự")
    private String tenTrinhDo;
    @Size(max = 200, message = "Mô tả không được quá 200 ký tự")
    private String moTa;
    @Min(value = 0, message = "Giá khám phải lớn hơn 0")
    private BigDecimal giaKham;
    @Min(value = 0, message = "Thứ tự uu tiên phải lớn hơn 0")
    private Integer thuTuUuTien;
}
