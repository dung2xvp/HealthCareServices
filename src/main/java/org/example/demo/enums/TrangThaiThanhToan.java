package org.example.demo.enums;

/**
 * Trạng thái thanh toán cho đặt lịch khám
 */
public enum TrangThaiThanhToan {
    CHUA_THANH_TOAN("Chưa thanh toán", "#FFC107"),
    DANG_XU_LY("Đang xử lý", "#17A2B8"),
    THANH_CONG("Thanh toán thành công", "#28A745"),
    THAT_BAI("Thanh toán thất bại", "#DC3545"),
    HOAN_TIEN("Đã hoàn tiền", "#6C757D");

    private final String moTa;
    private final String mauSac; // Hex color for UI

    TrangThaiThanhToan(String moTa, String mauSac) {
        this.moTa = moTa;
        this.mauSac = mauSac;
    }

    public String getMoTa() {
        return moTa;
    }

    public String getMauSac() {
        return mauSac;
    }

    /**
     * Kiểm tra xem đã thanh toán thành công chưa
     */
    public boolean isPaid() {
        return this == THANH_CONG;
    }

    /**
     * Kiểm tra xem có thể hoàn tiền không
     */
    public boolean canRefund() {
        return this == THANH_CONG;
    }
}
