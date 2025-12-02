package org.example.demo.enums;

import lombok.Getter;

/**
 * Enum cho loại nghỉ phép
 * Dùng để phân loại yêu cầu nghỉ
 */
@Getter
public enum LoaiNghiPhep {
    PHEP_NAM("Nghỉ phép năm", true, "Nghỉ phép năm theo quy định, sẽ trừ vào số ngày phép"),
    OM("Nghỉ ốm", false, "Nghỉ ốm (có giấy khám bệnh), không trừ ngày phép"),
    CONG_TAC("Công tác", false, "Đi công tác, hội nghị, đào tạo"),
    KHAC("Khác", false, "Lý do khác");
    
    private final String moTa;
    private final boolean canGiamNgayPhep; // Có trừ ngày phép không
    private final String chiTiet;
    
    LoaiNghiPhep(String moTa, boolean canGiamNgayPhep, String chiTiet) {
        this.moTa = moTa;
        this.canGiamNgayPhep = canGiamNgayPhep;
        this.chiTiet = chiTiet;
    }
    
    /**
     * Check xem loại này có cần giảm ngày phép không
     */
    public boolean isCanGiamNgayPhep() {
        return canGiamNgayPhep;
    }
}

