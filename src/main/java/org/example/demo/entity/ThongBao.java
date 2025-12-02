package org.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.demo.enums.LoaiThongBao;

import java.time.LocalDateTime;

/**
 * Entity cho bảng ThongBao - Hệ thống thông báo trong ứng dụng
 */
@Entity
@Table(name = "ThongBao",
       indexes = {
           @Index(name = "idx_nguoinhan_dadoc", columnList = "NguoiNhanID, DaDoc"),
           @Index(name = "idx_nguoinhan_thoigian", columnList = "NguoiNhanID, ThoiGian"),
           @Index(name = "idx_loai", columnList = "LoaiThongBao"),
           @Index(name = "idx_datlich", columnList = "DatLichID")
       })
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ThongBao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ThongBaoID")
    private Integer thongBaoID;

    // ========== NGƯỜI NHẬN ==========

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NguoiNhanID", nullable = false)
    private NguoiDung nguoiNhan;

    // ========== NỘI DUNG ==========

    @Enumerated(EnumType.STRING)
    @Column(name = "LoaiThongBao", nullable = false, length = 30)
    private LoaiThongBao loaiThongBao;

    @Column(name = "TieuDe", nullable = false, length = 200)
    private String tieuDe;

    @Column(name = "NoiDung", nullable = false, columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "ThoiGian", nullable = false)
    private LocalDateTime thoiGian;

    // ========== TRẠNG THÁI ==========

    @Column(name = "DaDoc", nullable = false)
    private Boolean daDoc = false;

    @Column(name = "NgayDoc")
    private LocalDateTime ngayDoc;

    // ========== LIÊN KẾT ==========

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DatLichID")
    private DatLichKham datLichKham; // Liên kết đến lịch khám (nếu có)

    @Column(name = "LinkDinhKem", length = 500)
    private String linkDinhKem; // Link đến trang chi tiết (VD: /booking/123)

    // ========== EMAIL ==========

    @Column(name = "DaGuiEmail", nullable = false)
    private Boolean daGuiEmail = false;

    @Column(name = "NgayGuiEmail")
    private LocalDateTime ngayGuiEmail;

    // ========== METADATA ==========

    @Column(name = "MetaData", columnDefinition = "TEXT")
    private String metaData; // JSON string chứa dữ liệu bổ sung

    // ========== HELPER METHODS ==========

    /**
     * Đánh dấu đã đọc
     */
    public void markAsRead() {
        this.daDoc = true;
        this.ngayDoc = LocalDateTime.now();
    }

    /**
     * Đánh dấu đã gửi email
     */
    public void markEmailSent() {
        this.daGuiEmail = true;
        this.ngayGuiEmail = LocalDateTime.now();
    }

    /**
     * Kiểm tra xem thông báo có mới không (trong vòng 24h)
     */
    public boolean isNew() {
        if (thoiGian == null) {
            return false;
        }
        return !daDoc && thoiGian.isAfter(LocalDateTime.now().minusHours(24));
    }

    /**
     * Lấy màu sắc của thông báo (dựa vào loại)
     */
    public String getMauSac() {
        return loaiThongBao != null ? loaiThongBao.getMauSac() : "#9E9E9E";
    }

    /**
     * Kiểm tra xem có cần gửi email không
     */
    public boolean shouldSendEmail() {
        return !daGuiEmail && loaiThongBao != null && loaiThongBao.shouldSendEmail();
    }
}
