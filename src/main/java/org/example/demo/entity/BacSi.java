package org.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "BacSi")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BacSi extends BaseEntity {
    @Id
    @Column(name = "BacSiID")
    private Integer bacSiID;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "BacSiID")
    private NguoiDung nguoiDung;
    
    @ManyToOne
    @JoinColumn(name = "ChuyenKhoaID", nullable = false)
    private ChuyenKhoa chuyenKhoa;

    @ManyToOne
    @JoinColumn(name = "TrinhDoID", nullable = false)
    private TrinhDo trinhDo;

    @Column(name = "SoNamKinhNghiem")
    private Integer soNamKinhNghiem = 0;

    @Column(name = "GioiThieu", columnDefinition = "TEXT")
    private String gioiThieu;

    @Column(name = "QuaTrinhDaoTao", columnDefinition = "TEXT")
    private String quaTrinhDaoTao;

    @Column(name = "KinhNghiemLamViec", columnDefinition = "TEXT")
    private String kinhNghiemLamViec;

    @Column(name = "ThanhTich", columnDefinition = "TEXT")
    private String thanhTich;

    @Column(name = "ChungChi", columnDefinition = "TEXT")
    private String chungChi;

    @Column(name = "GiaKham", precision = 10, scale = 2)
    private BigDecimal giaKham;

    @Column(name = "SoBenhNhanToiDaMotNgay")
    private Integer soBenhNhanToiDaMotNgay = 20;

    @Column(name = "ThoiGianKhamMotCa")
    private Integer thoiGianKhamMotCa = 30;

    @Column(name = "TrangThaiCongViec")
    private Boolean trangThaiCongViec = true;
    
    // ===== QUẢN LÝ NGÀY PHÉP =====
    
    /**
     * Tổng số ngày phép trong năm
     * Mặc định: 12 ngày theo quy định
     */
    @Column(name = "SoNgayPhepNam")
    private Integer soNgayPhepNam = 12;
    
    /**
     * Số ngày phép đã sử dụng trong năm hiện tại
     * Reset về 0 đầu năm
     */
    @Column(name = "SoNgayPhepDaSuDung")
    private Integer soNgayPhepDaSuDung = 0;
    
    /**
     * Năm áp dụng (để reset đầu năm)
     * Mặc định: Năm hiện tại
     */
    @Column(name = "NamApDung")
    private Integer namApDung;

    // ===== RELATIONSHIPS =====

    /**
     * Danh sách yêu cầu nghỉ của bác sĩ
     * Bác sĩ đăng ký nghỉ ngày/ca nào đó
     */
    @OneToMany(mappedBy = "bacSi", cascade = CascadeType.ALL)
    private List<BacSiNgayNghi> ngayNghis;

    /**
     * Danh sách lịch đặt khám của bác sĩ
     */
    @OneToMany(mappedBy = "bacSi", cascade = CascadeType.ALL)
    private List<DatLichKham> datLichKhams;
    
    // ===== HELPER METHODS =====
    
    /**
     * Tính số ngày phép còn lại
     */
    public Integer getSoNgayPhepConLai() {
        if (soNgayPhepNam == null || soNgayPhepDaSuDung == null) {
            return 0;
        }
        return soNgayPhepNam - soNgayPhepDaSuDung;
    }
    
    /**
     * Kiểm tra có đủ ngày phép không
     */
    public boolean hasSufficientDaysOff(Integer soNgayCanNghi) {
        return getSoNgayPhepConLai() >= soNgayCanNghi;
    }
    
    /**
     * Sử dụng ngày phép
     */
    public void useDaysOff(Integer soNgay) {
        if (!hasSufficientDaysOff(soNgay)) {
            throw new IllegalArgumentException(
                "Không đủ ngày phép. Còn lại: " + getSoNgayPhepConLai() + " ngày"
            );
        }
        this.soNgayPhepDaSuDung += soNgay;
    }
    
    /**
     * Hoàn trả ngày phép (khi hủy yêu cầu nghỉ)
     */
    public void refundDaysOff(Integer soNgay) {
        this.soNgayPhepDaSuDung = Math.max(0, this.soNgayPhepDaSuDung - soNgay);
    }
}
