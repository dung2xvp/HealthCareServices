package org.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.demo.enums.PhuongThucThanhToan;
import org.example.demo.enums.TrangThaiLichDat;
import org.example.demo.enums.TrangThaiThanhToan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "LichDatKham", indexes = {
    @Index(name = "idx_ngay_kham", columnList = "NgayKham"),
    @Index(name = "idx_bac_si", columnList = "BacSiID"),
    @Index(name = "idx_benh_nhan", columnList = "BenhNhanID"),
    @Index(name = "idx_trang_thai", columnList = "TrangThai"),
    @Index(name = "idx_thanh_toan", columnList = "TrangThaiThanhToan")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LichDatKham extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LichDatID")
    private Integer lichDatID;

    @ManyToOne
    @JoinColumn(name = "BacSiID", nullable = false)
    private BacSi bacSi;

    @ManyToOne
    @JoinColumn(name = "BenhNhanID", nullable = false)
    private NguoiDung benhNhan;

    @ManyToOne
    @JoinColumn(name = "ChuyenKhoaID", nullable = false)
    private ChuyenKhoa chuyenKhoa;

    // Thông tin đặt lịch
    @Column(name = "NgayKham", nullable = false)
    private LocalDate ngayKham;

    @Column(name = "GioKham", nullable = false)
    private LocalTime gioKham;

    @Column(name = "TrieuChung", columnDefinition = "TEXT")
    private String trieuChung;

    @Column(name = "LyDoKham", columnDefinition = "TEXT")
    private String lyDoKham;

    // Trạng thái
    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThai")
    private TrangThaiLichDat trangThai = TrangThaiLichDat.ChoXacNhan;

    // Kết quả khám
    @Column(name = "ChanDoan", columnDefinition = "TEXT")
    private String chanDoan;

    @Column(name = "DonThuoc", columnDefinition = "TEXT")
    private String donThuoc;

    @Column(name = "GhiChu", columnDefinition = "TEXT")
    private String ghiChu;

    @Column(name = "NgayTaiKham")
    private LocalDate ngayTaiKham;

    // Thanh toán
    @Column(name = "GiaKham", nullable = false, precision = 10, scale = 2)
    private BigDecimal giaKham;

    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThaiThanhToan")
    private TrangThaiThanhToan trangThaiThanhToan = TrangThaiThanhToan.ChuaThanhToan;

    @Enumerated(EnumType.STRING)
    @Column(name = "PhuongThucThanhToan")
    private PhuongThucThanhToan phuongThucThanhToan;

    @Column(name = "NgayThanhToan")
    private LocalDateTime ngayThanhToan;

    // Payment Gateway
    @Column(name = "MaGiaoDich", length = 100)
    private String maGiaoDich;

    @Column(name = "MaDonHang", unique = true, length = 100)
    private String maDonHang;

    @Column(name = "NoiDungThanhToan")
    private String noiDungThanhToan;

    @Column(name = "ResponseCode", length = 50)
    private String responseCode;

    @Column(name = "ResponseMessage")
    private String responseMessage;

    @Column(name = "ResponseData", columnDefinition = "TEXT")
    private String responseData;

    @Column(name = "NgayCapNhatThanhToan")
    private LocalDateTime ngayCapNhatThanhToan;

    // Hủy lịch
    @Column(name = "NguoiHuy")
    private Integer nguoiHuy;

    @Column(name = "LyDoHuy", columnDefinition = "TEXT")
    private String lyDoHuy;

    @Column(name = "NgayHuy")
    private LocalDateTime ngayHuy;

    // Đánh giá
    @Column(name = "SoSao")
    private Integer soSao;

    @Column(name = "NhanXet", columnDefinition = "TEXT")
    private String nhanXet;

    @Column(name = "NgayDanhGia")
    private LocalDateTime ngayDanhGia;

    @PrePersist
    @PreUpdate
    private void validateRating() {
        if (soSao != null && (soSao < 1 || soSao > 5)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }
}
