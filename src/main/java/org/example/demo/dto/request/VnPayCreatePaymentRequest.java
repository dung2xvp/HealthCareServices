package org.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VnPayCreatePaymentRequest {

    @NotNull
    @Schema(description = "ID lịch khám", example = "123")
    private Integer datLichID;

    @Schema(description = "Địa chỉ IP của client", example = "127.0.0.1")
    private String clientIp;
}

