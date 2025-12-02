package org.example.demo.enums;

import lombok.Getter;

/**
 * Enum cho loại nghỉ
 * Dùng cho BacSiNgayNghi
 */
@Getter
public enum LoaiNghi {
    NGAY_CU_THE("Nghỉ cả ngày cụ thể", "Nghỉ cả ngày trong một ngày cụ thể (VD: 25/12/2025)"),
    CA_CU_THE("Nghỉ một ca trong ngày cụ thể", "Nghỉ một ca (SANG/CHIEU/TOI) trong một ngày cụ thể"),
    CA_HANG_TUAN("Nghỉ một ca hàng tuần", "Nghỉ một ca hoặc cả ngày mỗi tuần (VD: Mỗi Thứ 7)");
    
    private final String moTa;
    private final String chiTiet;
    
    LoaiNghi(String moTa, String chiTiet) {
        this.moTa = moTa;
        this.chiTiet = chiTiet;
    }
}

