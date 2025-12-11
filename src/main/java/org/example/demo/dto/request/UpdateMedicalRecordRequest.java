package org.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request cập nhật hồ sơ bệnh án")
public class UpdateMedicalRecordRequest {

    @Size(max = 10, message = "Nhóm máu tối đa 10 ký tự")
    @Schema(description = "Nhóm máu", example = "O+")
    private String nhomMau;

    @DecimalMin(value = "0.0", inclusive = false, message = "Chiều cao phải > 0")
    @DecimalMax(value = "300.0", message = "Chiều cao không hợp lệ")
    @Schema(description = "Chiều cao (cm)", example = "170.5")
    private BigDecimal chieuCao;

    @DecimalMin(value = "0.0", inclusive = false, message = "Cân nặng phải > 0")
    @DecimalMax(value = "400.0", message = "Cân nặng không hợp lệ")
    @Schema(description = "Cân nặng (kg)", example = "65.2")
    private BigDecimal canNang;

    @Schema(description = "Dị ứng", example = "Penicillin")
    private String diUng;

    @Schema(description = "Bệnh mãn tính / tiền sử", example = "Tăng huyết áp, đái tháo đường")
    private String benhManTinh;

    @Schema(description = "Thuốc đang dùng", example = "Metformin 500mg, Amlodipine 5mg")
    private String thuocDangDung;

    @Schema(description = "Phẫu thuật đã qua", example = "Mổ ruột thừa 2018")
    private String phauThuatDaQua;

    @Schema(description = "Tiền sử gia đình", example = "Gia đình có người mắc bệnh tim")
    private String tienSuGiaDinh;

    @Schema(description = "Hút thuốc", example = "false")
    private Boolean hutThuoc;

    @Schema(description = "Uống rượu", example = "false")
    private Boolean uongRuou;
}

