package org.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Doanh thu theo chuyên khoa")
public class SpecialtyRevenueResponse {

    @Schema(description = "ID chuyên khoa")
    private Integer chuyenKhoaID;

    @Schema(description = "Tên chuyên khoa")
    private String tenChuyenKhoa;

    @Schema(description = "Tổng doanh thu")
    private BigDecimal doanhThu;

    @Schema(description = "Số ca hoàn thành")
    private Long soCaHoanThanh;
}

