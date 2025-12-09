package org.example.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.demo.dto.request.VnPayCreatePaymentRequest;
import org.example.demo.dto.response.ApiResponseDTO;
import org.example.demo.dto.response.VnPayPaymentUrlResponse;
import org.example.demo.dto.response.VnPayReturnResponse;
import org.example.demo.security.CustomUserDetails;
import org.example.demo.service.VnPayService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/vnpay")
@Tag(name = "Payments - VNPay", description = "Tạo URL thanh toán, xử lý return và IPN từ VNPay")
@RequiredArgsConstructor
public class PaymentController {

    private final VnPayService vnPayService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('BenhNhan')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tạo URL thanh toán VNPay cho lịch khám")
    public ResponseEntity<ApiResponseDTO<VnPayPaymentUrlResponse>> createPaymentUrl(
        @Valid @RequestBody VnPayCreatePaymentRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String url = vnPayService.createPaymentUrl(
            request.getDatLichID(),
            userDetails.getNguoiDungID(),
            request.getClientIp()
        );
        return ResponseEntity.ok(ApiResponseDTO.success(
            VnPayPaymentUrlResponse.builder().paymentUrl(url).build(),
            "Tạo URL thanh toán thành công"
        ));
    }

    @GetMapping("/return")
    @Operation(
        summary = "VNPay return URL",
        description = "VNPay redirect người dùng về URL này sau khi thanh toán (chỉ kiểm tra chữ ký, không thay đổi trạng thái)",
        responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = VnPayReturnResponse.class)))
    )
    public ResponseEntity<ApiResponseDTO<VnPayReturnResponse>> handleReturn(
        @RequestParam Map<String, String> params
    ) {
        VnPayReturnResponse result = vnPayService.handleReturn(params);
        return ResponseEntity.ok(ApiResponseDTO.success(result, "Xử lý return thành công"));
    }

    @GetMapping("/ipn")
    @Operation(
        summary = "VNPay IPN",
        description = "VNPay server gọi vào để xác nhận thanh toán"
    )
    public Map<String, String> handleIpn(
        @RequestParam Map<String, String> params
    ) {
        return vnPayService.handleIpn(params);
    }
}

