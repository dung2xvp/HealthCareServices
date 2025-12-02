package org.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.demo.enums.CaLamViec;
import org.example.demo.enums.PhuongThucThanhToan;
import org.example.demo.enums.TrangThaiDatLich;
import org.example.demo.enums.TrangThaiThanhToan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity cho bảng DatLichKham - Lưu thông tin đặt lịch khám bệnh
 */
@Entity
@Table(name = "DatLichKham",
       indexes = {
           @Index(name = "idx_benhnhan_trangthai", columnList = "BenhNhanID, TrangThai"),
           @Index(name = "idx_bacsi_ngay", columnList = "BacSiID, NgayKham, Ca"),
           @Index(name = "idx_ngaykham", columnList = "NgayKham, Ca, TrangThai"),
           @Index(name = "idx_maxacnhan", columnList = "MaXacNhan"),
           @Index(name = "idx_trangthai_ngay", columnList = "TrangThai, NgayKham"),
           @Index(name = "idx_bacsi_trangthai", columnList = "BacSiID, TrangThai")
       },
       uniqueConstraints = {
           @UniqueConstraint(
               name = "unique_booking_slot",
               columnNames = {"BacSiID", "NgayKham", "Ca", "GioKham", "TrangThai"}
           )
       })
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DatLichKham extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DatLichID")
    private Integer datLichID;

    // ========== THÔNG TIN CƠ BẢN ==========
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BenhNhanID", nullable = false)
    private NguoiDung benhNhan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BacSiID", nullable = false)
    private BacSi bacSi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CoSoID", nullable = false)
    private CoSoYTe coSoYTe;

    @Column(name = "NgayKham", nullable = false)
    private LocalDate ngayKham;

    @Enumerated(EnumType.STRING)
    @Column(name = "Ca", nullable = false, length = 20)
    private CaLamViec ca;

    @Column(name = "GioKham", nullable = false)
    private LocalTime gioKham;

    @Column(name = "LyDoKham", nullable = false, length = 1000)
    private String lyDoKham;

    @Column(name = "GhiChu", length = 500)
    private String ghiChu;

    // ========== TRẠNG THÁI ==========

    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThai", nullable = false, length = 30)
    private TrangThaiDatLich trangThai = TrangThaiDatLich.CHO_XAC_NHAN_BAC_SI;

    @Column(name = "MaXacNhan", unique = true, nullable = false, length = 8)
    private String maXacNhan;

    @Column(name = "NgayDat", nullable = false)
    private LocalDateTime ngayDat;

    // ========== THANH TOÁN ==========

    @Column(name = "GiaKham", nullable = false, precision = 10, scale = 2)
    private BigDecimal giaKham;

    @Enumerated(EnumType.STRING)
    @Column(name = "PhuongThucThanhToan", length = 20)
    private PhuongThucThanhToan phuongThucThanhToan;

    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThaiThanhToan", nullable = false, length = 20)
    private TrangThaiThanhToan trangThaiThanhToan = TrangThaiThanhToan.CHUA_THANH_TOAN;

    @Column(name = "MaGiaoDich", length = 100)
    private String maGiaoDich; // Transaction ID from payment gateway (VNPay, MoMo, etc.)

    @Column(name = "NgayThanhToan")
    private LocalDateTime ngayThanhToan;

    @Column(name = "ThongTinThanhToan", columnDefinition = "TEXT")
    private String thongTinThanhToan; // JSON string chứa chi tiết thanh toán

    // ========== XÁC NHẬN BÁC SĨ ==========

    @Column(name = "NgayBacSiXacNhan")
    private LocalDateTime ngayBacSiXacNhan;

    @Column(name = "LyDoTuChoi", length = 500)
    private String lyDoTuChoi; // Lý do bác sĩ từ chối

    // ========== HỦY LỊCH ==========

    @Column(name = "NgayHuy")
    private LocalDateTime ngayHuy;

    @Column(name = "LyDoHuy", length = 500)
    private String lyDoHuy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NguoiHuy")
    private NguoiDung nguoiHuy;

    // ========== KHÁM BỆNH ==========

    @Column(name = "NgayCheckIn")
    private LocalDateTime ngayCheckIn; // Thời gian check-in tại phòng khám

    @Column(name = "NgayKhamThucTe")
    private LocalDateTime ngayKhamThucTe; // Thời gian bắt đầu khám

    @Column(name = "NgayHoanThanh")
    private LocalDateTime ngayHoanThanh; // Thời gian hoàn thành khám

    @Column(name = "KetQuaKham", columnDefinition = "TEXT")
    private String ketQuaKham; // Kết quả khám bệnh

    @Column(name = "DonThuoc", columnDefinition = "TEXT")
    private String donThuoc; // Đơn thuốc

    @Column(name = "ChanDoan", length = 500)
    private String chanDoan; // Chẩn đoán

    @Column(name = "LoiDanBacSi", columnDefinition = "TEXT")
    private String loiDanBacSi; // Lời dặn của bác sĩ

    // ========== REMINDER ==========

    @Column(name = "DaNhacNho", nullable = false)
    private Boolean daNhacNho = false;

    @Column(name = "NgayNhacNho")
    private LocalDateTime ngayNhacNho; // Thời gian gửi email nhắc nhở

    // ========== HOÀN TIỀN ==========

    @Column(name = "NgayHoanTien")
    private LocalDateTime ngayHoanTien;

    @Column(name = "SoTienHoan", precision = 10, scale = 2)
    private BigDecimal soTienHoan;

    @Column(name = "LyDoHoanTien", length = 500)
    private String lyDoHoanTien;

    // ========== ĐÁNH GIÁ SAU KHÁM ==========

    @Column(name = "SoSao")
    private Integer soSao; // Rating từ 1-5 sao

    @Column(name = "NhanXet", columnDefinition = "TEXT")
    private String nhanXet; // Nhận xét của bệnh nhân về bác sĩ/dịch vụ

    @Column(name = "NgayDanhGia")
    private LocalDateTime ngayDanhGia; // Thời gian đánh giá

    // ========== TÁI KHÁM ==========

    @Column(name = "NgayTaiKham")
    private LocalDate ngayTaiKham; // Ngày hẹn tái khám (bác sĩ chỉ định)

    // ========== VALIDATION ==========

    @PrePersist
    @PreUpdate
    private void validate() {
        // Validate ngày khám phải >= ngày hiện tại
        if (ngayKham != null && ngayKham.isBefore(LocalDate.now())) {
            throw new IllegalStateException("Ngày khám phải từ hôm nay trở đi");
        }

        // Validate giờ khám phải nằm trong ca làm việc
        if (ca != null && gioKham != null) {
            LocalTime startTime = ca.getThoiGianMacDinhBatDau();
            LocalTime endTime = ca.getThoiGianMacDinhKetThuc();
            if (gioKham.isBefore(startTime) || gioKham.isAfter(endTime)) {
                throw new IllegalStateException(
                    String.format("Giờ khám %s không nằm trong ca %s (%s - %s)",
                        gioKham, ca.name(), startTime, endTime)
                );
            }
        }

        // Validate giá khám > 0
        if (giaKham != null && giaKham.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Giá khám phải lớn hơn 0");
        }

        // Validate trạng thái thanh toán
        if (phuongThucThanhToan != null && 
            phuongThucThanhToan.isOnlinePayment() && 
            trangThaiThanhToan == TrangThaiThanhToan.CHUA_THANH_TOAN &&
            trangThai == TrangThaiDatLich.DA_XAC_NHAN) {
            throw new IllegalStateException(
                "Phải thanh toán online thành công trước khi xác nhận lịch"
            );
        }

        // Validate rating (1-5 sao)
        if (soSao != null && (soSao < 1 || soSao > 5)) {
            throw new IllegalStateException("Đánh giá phải từ 1 đến 5 sao");
        }

        // Validate ngày tái khám phải > ngày khám
        if (ngayTaiKham != null && ngayKham != null && ngayTaiKham.isBefore(ngayKham)) {
            throw new IllegalStateException("Ngày tái khám phải sau ngày khám");
        }
    }

    // ========== HELPER METHODS ==========

    /**
     * Kiểm tra xem lịch khám có thể hủy được không
     */
    public boolean canCancel() {
        return trangThai != null && trangThai.isCanCancel();
    }

    /**
     * Kiểm tra xem lịch khám có thể reschedule được không
     */
    public boolean canReschedule() {
        return trangThai != null && trangThai.canReschedule();
    }

    /**
     * Kiểm tra xem có đang chiếm slot không
     */
    public boolean isOccupyingSlot() {
        return trangThai != null && trangThai.isOccupyingSlot();
    }

    /**
     * Kiểm tra xem đã thanh toán chưa
     */
    public boolean isPaid() {
        return trangThaiThanhToan != null && trangThaiThanhToan.isPaid();
    }

    /**
     * Kiểm tra xem có cần thanh toán online không
     */
    public boolean requiresOnlinePayment() {
        return phuongThucThanhToan != null && phuongThucThanhToan.isOnlinePayment();
    }

    /**
     * Kiểm tra xem đã quá thời gian nhắc nhở chưa (24 giờ trước)
     */
    public boolean shouldSendReminder() {
        if (daNhacNho || ngayKham == null || gioKham == null) {
            return false;
        }
        
        LocalDateTime appointmentTime = LocalDateTime.of(ngayKham, gioKham);
        LocalDateTime reminderTime = appointmentTime.minusHours(24);
        LocalDateTime now = LocalDateTime.now();
        
        // Gửi nhắc nhở trong khoảng từ 24h đến 23h trước giờ khám
        return now.isAfter(reminderTime) && 
               now.isBefore(appointmentTime.minusHours(23)) &&
               trangThai == TrangThaiDatLich.DA_XAC_NHAN;
    }

    /**
     * Kiểm tra xem có quá hạn không (đã qua ngày khám mà chưa hoàn thành/hủy)
     */
    public boolean isExpired() {
        if (ngayKham == null || trangThai == null) {
            return false;
        }
        
        return LocalDate.now().isAfter(ngayKham) && trangThai.isActive();
    }

    /**
     * Kiểm tra xem có thể đánh giá không
     * Chỉ cho phép đánh giá khi lịch đã hoàn thành và chưa đánh giá
     */
    public boolean canRate() {
        return trangThai == TrangThaiDatLich.HOAN_THANH && soSao == null;
    }

    /**
     * Kiểm tra xem đã đánh giá chưa
     */
    public boolean isRated() {
        return soSao != null && ngayDanhGia != null;
    }

    /**
     * Kiểm tra xem có hẹn tái khám không
     */
    public boolean hasFollowUp() {
        return ngayTaiKham != null && ngayTaiKham.isAfter(LocalDate.now());
    }
}

