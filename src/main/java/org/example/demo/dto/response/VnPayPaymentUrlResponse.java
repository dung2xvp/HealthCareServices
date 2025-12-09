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
public class VnPayPaymentUrlResponse {

    @Schema(description = "URL redirect tới VNPay", example = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...") 
    private String paymentUrl;
}

