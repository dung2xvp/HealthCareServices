package org.example.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.demo.config.VnPayProperties;
import org.example.demo.dto.response.VnPayReturnResponse;
import org.example.demo.entity.DatLichKham;
import org.example.demo.enums.PhuongThucThanhToan;
import org.example.demo.enums.TrangThaiDatLich;
import org.example.demo.enums.TrangThaiThanhToan;
import org.example.demo.exception.BadRequestException;
import org.example.demo.exception.ResourceNotFoundException;
import org.example.demo.exception.UnauthorizedException;
import org.example.demo.repository.DatLichKhamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VnPayService {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter VNP_TIME_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final DatLichKhamRepository datLichKhamRepository;
    private final VnPayProperties vnPayProperties;

    /**
     * Tạo URL thanh toán VNPay cho 1 booking
     */
    @Transactional
    public String createPaymentUrl(Integer datLichID, Integer currentUserId, String clientIp) {
        DatLichKham booking = datLichKhamRepository.findById(datLichID)
            .orElseThrow(() -> new ResourceNotFoundException("Lịch khám không tồn tại"));

        if (Boolean.TRUE.equals(booking.getIsDeleted())) {
            throw new BadRequestException("Lịch khám đã bị xóa");
        }
        if (!booking.getBenhNhan().getNguoiDungID().equals(currentUserId)) {
            throw new UnauthorizedException("Bạn không sở hữu lịch khám này");
        }
        if (booking.getPhuongThucThanhToan() != PhuongThucThanhToan.VNPAY) {
            throw new BadRequestException("Lịch này không chọn phương thức VNPay");
        }
        if (booking.getTrangThaiThanhToan() == TrangThaiThanhToan.THANH_CONG) {
            throw new BadRequestException("Lịch đã được thanh toán");
        }

        String txnRef = booking.getMaGiaoDich();
        if (txnRef == null || txnRef.isBlank()) {
            txnRef = "VNP" + booking.getDatLichID() + System.currentTimeMillis();
            booking.setMaGiaoDich(txnRef);
        }

        LocalDateTime now = LocalDateTime.now(VN_ZONE);
        LocalDateTime expire = now.plusMinutes(
            vnPayProperties.getExpireMinutes() != null ? vnPayProperties.getExpireMinutes() : 15
        );

        long amount = booking.getGiaKham()
            .multiply(BigDecimal.valueOf(100))
            .longValue();

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", vnPayProperties.getVersion());
        params.put("vnp_Command", vnPayProperties.getCommand());
        params.put("vnp_TmnCode", vnPayProperties.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", vnPayProperties.getCurrCode());
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", "Thanh toan lich kham #" + booking.getDatLichID());
        params.put("vnp_OrderType", vnPayProperties.getOrderType());
        params.put("vnp_Locale", vnPayProperties.getLocale());
        params.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl());
        params.put("vnp_IpAddr", clientIp != null ? clientIp : "127.0.0.1");
        params.put("vnp_CreateDate", VNP_TIME_FMT.format(now));
        params.put("vnp_ExpireDate", VNP_TIME_FMT.format(expire));
        if (vnPayProperties.getIpnUrl() != null) {
            params.put("vnp_IpnUrl", vnPayProperties.getIpnUrl());
        }

        String query = buildQuery(params);
        String secureHash = hmacSHA512(vnPayProperties.getHashSecret(), query);
        String paymentUrl = vnPayProperties.getPayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;

        datLichKhamRepository.save(booking);
        return paymentUrl;
    }

    /**
     * Xử lý màn hình return (redirect). Không thay đổi trạng thái, chỉ trả kết quả tham khảo.
     */
    @Transactional(readOnly = true)
    public VnPayReturnResponse handleReturn(Map<String, String> params) {
        String rspCode = params.get("vnp_ResponseCode");
        String txnStatus = params.get("vnp_TransactionStatus");
        String txnRef = params.get("vnp_TxnRef");

        boolean valid = verifySignature(params);
        boolean success = valid && "00".equals(rspCode) && "00".equals(txnStatus);

        Integer datLichId = txnRef != null
            ? datLichKhamRepository.findByMaGiaoDich(txnRef).map(DatLichKham::getDatLichID).orElse(null)
            : null;

        String message;
        if (!valid) {
            message = "Chữ ký không hợp lệ";
        } else if (success) {
            message = "Thanh toán thành công (đang chờ IPN xác nhận)";
        } else {
            message = "Thanh toán thất bại hoặc bị hủy";
        }

        return VnPayReturnResponse.builder()
            .responseCode(rspCode)
            .transactionStatus(txnStatus)
            .success(success)
            .message(message)
            .datLichID(datLichId)
            .build();
    }

    /**
     * Xử lý IPN từ VNPay (server to server)
     */
    @Transactional
    public Map<String, String> handleIpn(Map<String, String> params) {
        Map<String, String> response = new HashMap<>();

        if (!verifySignature(params)) {
            response.put("RspCode", "97");
            response.put("Message", "Invalid signature");
            return response;
        }

        String txnRef = params.get("vnp_TxnRef");
        String rspCode = params.get("vnp_ResponseCode");
        String txnStatus = params.get("vnp_TransactionStatus");
        String amountStr = params.get("vnp_Amount");

        DatLichKham booking = datLichKhamRepository.findByMaGiaoDich(txnRef)
            .orElse(null);
        if (booking == null) {
            response.put("RspCode", "01");
            response.put("Message", "Order not found");
            return response;
        }

        long amount = parseLongSafe(amountStr);
        long expected = booking.getGiaKham().multiply(BigDecimal.valueOf(100)).longValue();
        if (amount != expected) {
            response.put("RspCode", "04");
            response.put("Message", "Invalid amount");
            return response;
        }

        if (booking.getTrangThaiThanhToan() == TrangThaiThanhToan.THANH_CONG) {
            response.put("RspCode", "02");
            response.put("Message", "Order already confirmed");
            return response;
        }

        boolean success = "00".equals(rspCode) && "00".equals(txnStatus);
        booking.setThongTinThanhToan(serialize(params));
        booking.setNgayThanhToan(LocalDateTime.now());

        if (success) {
            booking.setTrangThaiThanhToan(TrangThaiThanhToan.THANH_CONG);
            if (booking.getTrangThai() == TrangThaiDatLich.CHO_THANH_TOAN) {
                booking.setTrangThai(TrangThaiDatLich.CHO_XAC_NHAN_BAC_SI);
            }
            datLichKhamRepository.save(booking);
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
        } else {
            booking.setTrangThaiThanhToan(TrangThaiThanhToan.THAT_BAI);
            datLichKhamRepository.save(booking);
            response.put("RspCode", "00");
            response.put("Message", "Payment Failed");
        }

        return response;
    }

    private boolean verifySignature(Map<String, String> params) {
        String secureHash = params.get("vnp_SecureHash");
        if (secureHash == null) return false;

        Map<String, String> sorted = new TreeMap<>();
        params.forEach((k, v) -> {
            if (!"vnp_SecureHash".equalsIgnoreCase(k)) {
                sorted.put(k, v);
            }
        });

        String query = buildQuery(sorted);
        String calculated = hmacSHA512(vnPayProperties.getHashSecret(), query);
        return secureHash.equalsIgnoreCase(calculated);
    }

    private String buildQuery(Map<String, String> params) {
        Map<String, String> sorted = new TreeMap<>(params);
        StringBuilder sb = new StringBuilder();
        for (Iterator<Map.Entry<String, String>> it = sorted.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, String> entry = it.next();
            sb.append(encode(entry.getKey()))
                .append("=")
                .append(encode(entry.getValue()));
            if (it.hasNext()) sb.append("&");
        }
        return sb.toString();
    }

    private String encode(String input) {
        return URLEncoder.encode(input, StandardCharsets.UTF_8);
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKeySpec);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot generate HMAC", e);
        }
    }

    private long parseLongSafe(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return -1;
        }
    }

    private String serialize(Map<String, String> data) {
        try {
            return OBJECT_MAPPER.writeValueAsString(data);
        } catch (Exception e) {
            return data.toString();
        }
    }
}

