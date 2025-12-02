package org.example.demo.enums;

/**
 * Phương thức thanh toán cho đặt lịch khám
 */
public enum PhuongThucThanhToan {
    TIEN_MAT("Tiền mặt tại phòng khám", false),
    CHUYEN_KHOAN("Chuyển khoản ngân hàng", false),
    VNPAY("VNPay", true),
    MOMO("MoMo", true),
    ZALO_PAY("ZaloPay", true);

    private final String moTa;
    private final boolean isOnlinePayment; // Có phải thanh toán online không

    PhuongThucThanhToan(String moTa, boolean isOnlinePayment) {
        this.moTa = moTa;
        this.isOnlinePayment = isOnlinePayment;
    }

    public String getMoTa() {
        return moTa;
    }

    public boolean isOnlinePayment() {
        return isOnlinePayment;
    }

    /**
     * Kiểm tra xem phương thức này có cần xác nhận thanh toán online không
     */
    public boolean requiresPaymentConfirmation() {
        return isOnlinePayment;
    }
}
