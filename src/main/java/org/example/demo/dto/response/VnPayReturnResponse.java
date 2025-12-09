package org.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VnPayReturnResponse {

    @Schema(description = "Mã phản hồi VNPay", example = "00")
    private String responseCode;

    @Schema(description = "Mã trạng thái giao dịch VNPay", example = "00")
    private String transactionStatus;

    @Schema(description = "Thông điệp hiển thị")
    private String message;

    @Schema(description = "Thanh toán thành công hay không")
    private boolean success;

    @Schema(description = "ID lịch khám nếu tra được")
    private Integer datLichID;
}

