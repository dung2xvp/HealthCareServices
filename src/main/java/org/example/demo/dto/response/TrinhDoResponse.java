package org.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrinhDoResponse {
    private Integer trinhDoID;
    private String tenTrinhDo;
    private String moTa;
    private BigDecimal giaKham;
    private Integer thuTuUuTien;
}
