package org.example.demo.enums;

import lombok.Getter;

import java.time.LocalTime;

/**
 * Enum cho ca làm việc
 * Dùng cho LichLamViecMacDinh và BacSiNgayNghi
 */
@Getter
public enum CaLamViec {
    SANG("Ca sáng", "08:00", "12:00"),
    CHIEU("Ca chiều", "14:00", "17:00"),
    TOI("Ca tối", "18:00", "21:00");
    
    private final String tenCa;
    private final String thoiGianMacDinhBatDauStr;
    private final String thoiGianMacDinhKetThucStr;
    
    CaLamViec(String tenCa, String thoiGianMacDinhBatDau, String thoiGianMacDinhKetThuc) {
        this.tenCa = tenCa;
        this.thoiGianMacDinhBatDauStr = thoiGianMacDinhBatDau;
        this.thoiGianMacDinhKetThucStr = thoiGianMacDinhKetThuc;
    }
    
    /**
     * Lấy thời gian bắt đầu dưới dạng LocalTime
     */
    public LocalTime getThoiGianMacDinhBatDau() {
        return LocalTime.parse(thoiGianMacDinhBatDauStr);
    }
    
    /**
     * Lấy thời gian kết thúc dưới dạng LocalTime
     */
    public LocalTime getThoiGianMacDinhKetThuc() {
        return LocalTime.parse(thoiGianMacDinhKetThucStr);
    }
    
    /**
     * Lấy label đầy đủ cho UI
     * VD: "Ca sáng (08:00 - 12:00)"
     */
    public String getLabel() {
        return String.format("%s (%s - %s)", tenCa, thoiGianMacDinhBatDauStr, thoiGianMacDinhKetThucStr);
    }
}
