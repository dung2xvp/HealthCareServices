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

    @OneToMany(mappedBy = "bacSi", cascade = CascadeType.ALL)
    private List<BacSiLichLamViec> lichLamViecs;

    @OneToMany(mappedBy = "bacSi", cascade = CascadeType.ALL)
    private List<BacSiNgayNghi> ngayNghis;

    @OneToMany(mappedBy = "bacSi", cascade = CascadeType.ALL)
    private List<LichDatKham> lichDatKhams;
}
