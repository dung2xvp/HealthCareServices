package org.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.demo.enums.CaLamViec;

import java.time.LocalTime;

/**
 * Entity lưu lịch làm việc mặc định cho toàn bệnh viện
 * Áp dụng cho TẤT CẢ bác sĩ, trừ khi bác sĩ đăng ký nghỉ
 */
@Entity
@Table(name = "LichLamViecMacDinh", 
       uniqueConstraints = {
           @UniqueConstraint(
               name = "unique_schedule", 
               columnNames = {"CoSoID", "ThuTrongTuan", "Ca"}
           )
       },
       indexes = {
           @Index(name = "idx_active", columnList = "IsActive"),
           @Index(name = "idx_thu", columnList = "ThuTrongTuan")
       })
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class LichLamViecMacDinh extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ConfigID")
    private Integer configID;
    
    /**
     * Cơ sở y tế (nếu có nhiều chi nhánh)
     * Mặc định: 1
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CoSoID", nullable = false)
    private CoSoYTe coSoYTe;
    
    /**
     * Thứ trong tuần
     * 2 = Thứ 2, 3 = Thứ 3, ..., 7 = Thứ 7, 8 = Chủ nhật
     */
    @Column(name = "ThuTrongTuan", nullable = false)
    private Integer thuTrongTuan;
    
    /**
     * Ca làm việc: SANG, CHIEU, TOI
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "Ca", nullable = false, length = 20)
    private CaLamViec ca;
    
    /**
     * Thời gian bắt đầu ca
     * VD: 08:00:00
     */
    @Column(name = "ThoiGianBatDau", nullable = false)
    private LocalTime thoiGianBatDau;
    
    /**
     * Thời gian kết thúc ca
     * VD: 12:00:00
     */
    @Column(name = "ThoiGianKetThuc", nullable = false)
    private LocalTime thoiGianKetThuc;
    
    /**
     * Trạng thái: Đang áp dụng hay không
     * Có thể tạm tắt một ca mà không xóa
     */
    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;
    
    /**
     * Ghi chú
     * VD: "Lịch chuẩn", "Giờ hành chính"
     */
    @Column(name = "GhiChu", length = 500)
    private String ghiChu;
    
    /**
     * Validation: Thời gian kết thúc phải sau thời gian bắt đầu
     */
    @PrePersist
    @PreUpdate
    private void validate() {
        if (thoiGianKetThuc != null && thoiGianBatDau != null) {
            if (!thoiGianKetThuc.isAfter(thoiGianBatDau)) {
                throw new IllegalArgumentException(
                    "Thời gian kết thúc phải sau thời gian bắt đầu"
                );
            }
        }
        
        if (thuTrongTuan != null && (thuTrongTuan < 2 || thuTrongTuan > 8)) {
            throw new IllegalArgumentException(
                "Thứ trong tuần phải từ 2-8 (Thứ 2 - Chủ nhật)"
            );
        }
    }
}

