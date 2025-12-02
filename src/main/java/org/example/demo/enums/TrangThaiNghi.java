package org.example.demo.enums;

import lombok.Getter;

/**
 * Enum cho trạng thái yêu cầu nghỉ
 * Workflow: CHO_DUYET → DA_DUYET / TU_CHOI / HUY
 */
@Getter
public enum TrangThaiNghi {
    CHO_DUYET("Chờ duyệt", "yellow", "Yêu cầu đang chờ Admin duyệt"),
    DA_DUYET("Đã duyệt", "green", "Yêu cầu đã được duyệt, có hiệu lực"),
    TU_CHOI("Từ chối", "red", "Yêu cầu bị Admin từ chối"),
    HUY("Đã hủy", "gray", "Bác sĩ tự hủy yêu cầu");
    
    private final String moTa;
    private final String mauSac; // Cho UI: yellow, green, red, gray
    private final String chiTiet;
    
    TrangThaiNghi(String moTa, String mauSac, String chiTiet) {
        this.moTa = moTa;
        this.mauSac = mauSac;
        this.chiTiet = chiTiet;
    }
    
    /**
     * Check xem trạng thái có được duyệt không
     */
    public boolean isApproved() {
        return this == DA_DUYET;
    }
    
    /**
     * Check xem có thể hủy không (chỉ CHO_DUYET mới hủy được)
     */
    public boolean canCancel() {
        return this == CHO_DUYET;
    }
}

