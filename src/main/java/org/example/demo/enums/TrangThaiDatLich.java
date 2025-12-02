package org.example.demo.enums;

/**
 * Trạng thái của lịch đặt khám
 * 
 * Workflow:
 * 1. Bệnh nhân đặt → CHO_XAC_NHAN_BAC_SI
 * 2. Bác sĩ xác nhận → CHO_THANH_TOAN (nếu cần) hoặc DA_XAC_NHAN (nếu thanh toán tại chỗ)
 * 3. Thanh toán thành công → DA_XAC_NHAN
 * 4. Check-in → DANG_KHAM
 * 5. Hoàn thành → HOAN_THANH
 * 6. Các trạng thái hủy/không đến → HUY_*, KHONG_DEN, QUA_HAN
 */
public enum TrangThaiDatLich {
    CHO_XAC_NHAN_BAC_SI("Chờ bác sĩ xác nhận", "#FFA500", true),
    TU_CHOI("Bác sĩ từ chối", "#DC3545", false),
    CHO_THANH_TOAN("Chờ thanh toán", "#17A2B8", true),
    DA_XAC_NHAN("Đã xác nhận", "#28A745", true),
    DANG_KHAM("Đang khám", "#007BFF", true),
    HOAN_THANH("Hoàn thành", "#6C757D", false),
    HUY_BOI_BENH_NHAN("Hủy bởi bệnh nhân", "#FFC107", false),
    HUY_BOI_BAC_SI("Hủy bởi bác sĩ", "#FF5722", false),
    HUY_BOI_ADMIN("Hủy bởi admin", "#9E9E9E", false),
    KHONG_DEN("Không đến", "#E91E63", false),
    QUA_HAN("Quá hạn", "#795548", false);

    private final String moTa;
    private final String mauSac; // Hex color for UI
    private final boolean canCancel; // Có thể hủy được không

    TrangThaiDatLich(String moTa, String mauSac, boolean canCancel) {
        this.moTa = moTa;
        this.mauSac = mauSac;
        this.canCancel = canCancel;
    }

    public String getMoTa() {
        return moTa;
    }

    public String getMauSac() {
        return mauSac;
    }

    public boolean isCanCancel() {
        return canCancel;
    }

    /**
     * Kiểm tra xem trạng thái này có đang active (chưa hoàn thành/hủy) không
     */
    public boolean isActive() {
        return this == CHO_XAC_NHAN_BAC_SI || 
               this == CHO_THANH_TOAN || 
               this == DA_XAC_NHAN || 
               this == DANG_KHAM;
    }

    /**
     * Kiểm tra xem trạng thái này có được tính vào slot đã đặt không
     */
    public boolean isOccupyingSlot() {
        return isActive();
    }

    /**
     * Kiểm tra xem có thể reschedule không
     */
    public boolean canReschedule() {
        return this == CHO_XAC_NHAN_BAC_SI || 
               this == CHO_THANH_TOAN || 
               this == DA_XAC_NHAN;
    }
}

