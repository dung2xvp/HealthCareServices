package org.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.demo.enums.CaLamViec;
import org.example.demo.enums.LoaiNghi;
import org.example.demo.enums.LoaiNghiPhep;
import org.example.demo.enums.TrangThaiNghi;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity lưu yêu cầu nghỉ của bác sĩ
 * Workflow: Bác sĩ tạo yêu cầu → Admin duyệt/từ chối
 */
@Entity
@Table(name = "BacSiNgayNghi",
       indexes = {
           @Index(name = "idx_bacsi_trangthai", columnList = "BacSiID, TrangThai"),
           @Index(name = "idx_pending", columnList = "TrangThai, CreatedAt"),
           @Index(name = "idx_check_nghi_ngay", columnList = "BacSiID, TrangThai, NgayNghiCuThe"),
           @Index(name = "idx_check_nghi_tuan", columnList = "BacSiID, TrangThai, ThuTrongTuan")
       })
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BacSiNgayNghi extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NghiID")
    private Integer nghiID;
    
    /**
     * Bác sĩ xin nghỉ
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BacSiID", nullable = false)
    private BacSi bacSi;
    
    // ===== LOẠI NGHỈ =====
    
    /**
     * Loại nghỉ:
     * - NGAY_CU_THE: Nghỉ cả ngày 25/12/2025
     * - CA_CU_THE: Nghỉ ca CHIEU ngày 25/12/2025
     * - CA_HANG_TUAN: Nghỉ ca SANG mỗi Thứ 7
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "LoaiNghi", nullable = false, length = 20)
    private LoaiNghi loaiNghi;
    
    // ===== THỜI GIAN NGHỈ =====
    
    /**
     * Ngày nghỉ cụ thể
     * Dùng cho: NGAY_CU_THE, CA_CU_THE
     * NULL nếu loaiNghi = CA_HANG_TUAN
     */
    @Column(name = "NgayNghiCuThe")
    private LocalDate ngayNghiCuThe;
    
    /**
     * Thứ trong tuần (2-8)
     * Dùng cho: CA_HANG_TUAN
     * NULL nếu loaiNghi = NGAY_CU_THE, CA_CU_THE
     */
    @Column(name = "ThuTrongTuan")
    private Integer thuTrongTuan;
    
    /**
     * Ca nghỉ: SANG, CHIEU, TOI
     * NULL nếu nghỉ cả ngày
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "Ca", length = 20)
    private CaLamViec ca;
    
    // ===== THÔNG TIN ĐƠN =====
    
    /**
     * Lý do xin nghỉ
     * Required, min 10 chars
     */
    @Column(name = "LyDo", nullable = false, length = 500)
    private String lyDo;
    
    /**
     * Loại nghỉ phép
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "LoaiNghiPhep", nullable = false, length = 20)
    private LoaiNghiPhep loaiNghiPhep = LoaiNghiPhep.PHEP_NAM;
    
    /**
     * File đính kèm (đơn xin nghỉ, giấy khám bệnh...)
     * Lưu path hoặc URL
     */
    @Column(name = "FileDinhKem", length = 255)
    private String fileDinhKem;
    
    // ===== APPROVAL WORKFLOW =====
    
    /**
     * Trạng thái yêu cầu
     * CHO_DUYET → DA_DUYET / TU_CHOI / HUY
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "TrangThai", nullable = false, length = 20)
    private TrangThaiNghi trangThai = TrangThaiNghi.CHO_DUYET;
    
    /**
     * Admin duyệt/từ chối
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NguoiDuyet")
    private NguoiDung nguoiDuyet;
    
    /**
     * Thời điểm duyệt/từ chối
     */
    @Column(name = "NgayDuyet")
    private LocalDateTime ngayDuyet;
    
    /**
     * Lý do từ chối (nếu TrangThai = TU_CHOI)
     */
    @Column(name = "LyDoTuChoi", length = 500)
    private String lyDoTuChoi;
    
    /**
     * Validation trước khi persist/update
     */
    @PrePersist
    @PreUpdate
    private void validate() {
        // Validate logic theo LoaiNghi
        if (loaiNghi == LoaiNghi.NGAY_CU_THE || loaiNghi == LoaiNghi.CA_CU_THE) {
            if (ngayNghiCuThe == null) {
                throw new IllegalArgumentException(
                    "Phải chọn ngày nghỉ cụ thể cho loại nghỉ " + loaiNghi
                );
            }
        }
        
        if (loaiNghi == LoaiNghi.CA_HANG_TUAN) {
            if (thuTrongTuan == null) {
                throw new IllegalArgumentException(
                    "Phải chọn thứ trong tuần cho loại nghỉ CA_HANG_TUAN"
                );
            }
            if (thuTrongTuan < 2 || thuTrongTuan > 8) {
                throw new IllegalArgumentException(
                    "Thứ trong tuần phải từ 2-8"
                );
            }
        }
        
        // Validate Ca
        if (loaiNghi == LoaiNghi.CA_CU_THE || loaiNghi == LoaiNghi.CA_HANG_TUAN) {
            // Nếu nghỉ ca cụ thể thì phải chọn ca (trừ khi nghỉ cả ngày)
            // Nhưng cho phép ca = NULL để nghỉ cả ngày
        }
        
        if (loaiNghi == LoaiNghi.NGAY_CU_THE && ca != null) {
            throw new IllegalArgumentException(
                "Nghỉ cả ngày thì không cần chọn ca"
            );
        }
        
        // Validate ngày nghỉ phải trong tương lai (khi tạo mới)
        if (ngayNghiCuThe != null && nghiID == null) { // Chỉ check khi tạo mới
            if (ngayNghiCuThe.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException(
                    "Không thể đăng ký nghỉ ngày đã qua"
                );
            }
        }
    }
}
