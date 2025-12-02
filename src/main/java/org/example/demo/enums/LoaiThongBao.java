package org.example.demo.enums;

/**
 * Loại thông báo trong hệ thống
 */
public enum LoaiThongBao {
    // Booking related
    DAT_LICH_MOI("Lịch đặt khám mới", "booking", "#007BFF"),
    BAC_SI_XAC_NHAN("Bác sĩ xác nhận lịch khám", "booking", "#28A745"),
    BAC_SI_TU_CHOI("Bác sĩ từ chối lịch khám", "booking", "#DC3545"),
    HUY_LICH("Lịch khám bị hủy", "booking", "#FFC107"),
    NHAC_LICH_KHAM("Nhắc lịch khám", "reminder", "#17A2B8"),
    LICH_KHAM_HON_THANH("Lịch khám hoàn thành", "booking", "#6C757D"),
    
    // Payment related
    THANH_TOAN_THANH_CONG("Thanh toán thành công", "payment", "#28A745"),
    THANH_TOAN_THAT_BAI("Thanh toán thất bại", "payment", "#DC3545"),
    HOAN_TIEN("Hoàn tiền", "payment", "#6C757D"),
    
    // Day-off related
    NGAY_NGHI_MOI("Yêu cầu nghỉ phép mới", "dayoff", "#FFA500"),
    NGAY_NGHI_DUYET("Yêu cầu nghỉ được duyệt", "dayoff", "#28A745"),
    NGAY_NGHI_TU_CHOI("Yêu cầu nghỉ bị từ chối", "dayoff", "#DC3545"),
    
    // System
    HE_THONG("Thông báo hệ thống", "system", "#9E9E9E"),
    KHAC("Khác", "other", "#607D8B");

    private final String moTa;
    private final String category; // For grouping/filtering
    private final String mauSac; // Hex color for UI

    LoaiThongBao(String moTa, String category, String mauSac) {
        this.moTa = moTa;
        this.category = category;
        this.mauSac = mauSac;
    }

    public String getMoTa() {
        return moTa;
    }

    public String getCategory() {
        return category;
    }

    public String getMauSac() {
        return mauSac;
    }

    /**
     * Kiểm tra xem có cần gửi email không
     */
    public boolean shouldSendEmail() {
        return this == DAT_LICH_MOI || 
               this == BAC_SI_XAC_NHAN || 
               this == BAC_SI_TU_CHOI ||
               this == HUY_LICH ||
               this == NHAC_LICH_KHAM ||
               this == THANH_TOAN_THANH_CONG ||
               this == THANH_TOAN_THAT_BAI ||
               this == NGAY_NGHI_DUYET ||
               this == NGAY_NGHI_TU_CHOI;
    }
}

