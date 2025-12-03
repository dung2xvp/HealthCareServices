package org.example.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo.entity.DatLichKham;
import org.example.demo.enums.CaLamViec;
import org.example.demo.enums.PhuongThucThanhToan;
import org.example.demo.enums.TrangThaiDatLich;
import org.example.demo.enums.TrangThaiThanhToan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO Response thông tin chi tiết lịch khám
 * Dùng cho:
 * - Xem chi tiết 1 lịch khám
 * - After create booking
 * - After update booking
 * 
 * @author Healthcare System Team
 * @version 1.0 - Phase 2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response thông tin chi tiết lịch khám")
public class BookingResponse {
    
    // ===== THÔNG TIN CƠ BẢN =====
    
    @Schema(description = "ID lịch khám", example = "123")
    private Integer datLichID;
    
    @Schema(description = "Mã xác nhận (8 ký tự)", example = "ABC12345")
    private String maXacNhan;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày đặt lịch", example = "2025-12-01 14:30:00")
    private LocalDateTime ngayDat;
    
    // ===== THÔNG TIN BÁC SĨ =====
    
    @Schema(description = "ID bác sĩ", example = "5")
    private Integer bacSiID;
    
    @Schema(description = "Tên bác sĩ", example = "BS. Nguyễn Văn A")
    private String tenBacSi;
    
    @Schema(description = "Chuyên khoa", example = "Nội Khoa")
    private String tenChuyenKhoa;
    
    @Schema(description = "Trình độ", example = "Thạc sĩ")
    private String tenTrinhDo;
    
    @Schema(description = "Avatar bác sĩ", example = "https://storage.example.com/avatar/5.jpg")
    private String avatarBacSi;
    
    @Schema(description = "Số điện thoại bác sĩ", example = "0901234567")
    private String sdtBacSi;
    
    // ===== THÔNG TIN BỆNH NHÂN =====
    
    @Schema(description = "ID bệnh nhân", example = "10")
    private Integer benhNhanID;
    
    @Schema(description = "Tên bệnh nhân", example = "Nguyễn Thị B")
    private String tenBenhNhan;
    
    @Schema(description = "Số điện thoại bệnh nhân", example = "0987654321")
    private String sdtBenhNhan;
    
    @Schema(description = "Email bệnh nhân", example = "patient@example.com")
    private String emailBenhNhan;
    
    // ===== THỜI GIAN KHÁM =====
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày khám", example = "2025-12-15")
    private LocalDate ngayKham;
    
    @Schema(description = "Ca làm việc", example = "SANG")
    private CaLamViec ca;
    
    @Schema(description = "Tên ca (UI-friendly)", example = "Ca sáng (08:00 - 12:00)")
    private String tenCa;
    
    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Giờ khám", example = "08:30")
    private LocalTime gioKham;
    
    @Schema(description = "Thời gian khám (formatted)", example = "15/12/2025 - Ca sáng - 08:30")
    private String thoiGianKhamDisplay;
    
    // ===== LÝ DO KHÁM =====
    
    @Schema(description = "Lý do khám", example = "Đau đầu kéo dài 3 ngày")
    private String lyDoKham;
    
    @Schema(description = "Ghi chú", example = "Muốn khám vào buổi sáng sớm")
    private String ghiChu;
    
    // ===== TRẠNG THÁI =====
    
    @Schema(description = "Trạng thái", example = "DA_XAC_NHAN")
    private TrangThaiDatLich trangThai;
    
    @Schema(description = "Tên trạng thái (UI-friendly)", example = "Đã xác nhận")
    private String tenTrangThai;
    
    @Schema(description = "Màu sắc trạng thái (hex)", example = "#4CAF50")
    private String mauSacTrangThai;
    
    // ===== THANH TOÁN =====
    
    @Schema(description = "Giá khám", example = "300000")
    private BigDecimal giaKham;
    
    @Schema(description = "Giá khám (formatted)", example = "300.000 đ")
    private String giaKhamDisplay;
    
    @Schema(description = "Phương thức thanh toán", example = "VNPAY")
    private PhuongThucThanhToan phuongThucThanhToan;
    
    @Schema(description = "Tên phương thức thanh toán", example = "VNPay")
    private String tenPhuongThucThanhToan;
    
    @Schema(description = "Trạng thái thanh toán", example = "THANH_CONG")
    private TrangThaiThanhToan trangThaiThanhToan;
    
    @Schema(description = "Tên trạng thái thanh toán", example = "Đã thanh toán")
    private String tenTrangThaiThanhToan;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày thanh toán", example = "2025-12-01 14:35:00")
    private LocalDateTime ngayThanhToan;
    
    @Schema(description = "Mã giao dịch", example = "VNP12345678")
    private String maGiaoDich;
    
    // ===== XÁC NHẬN BÁC SĨ =====
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày bác sĩ xác nhận", example = "2025-12-01 15:00:00")
    private LocalDateTime ngayBacSiXacNhan;
    
    @Schema(description = "Lý do từ chối (nếu bác sĩ từ chối)", example = "Lịch làm việc đã đầy")
    private String lyDoTuChoi;
    
    // ===== CHECK-IN & KHÁM =====
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian check-in", example = "2025-12-15 08:15:00")
    private LocalDateTime ngayCheckIn;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian bắt đầu khám", example = "2025-12-15 08:30:00")
    private LocalDateTime ngayKhamThucTe;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Thời gian hoàn thành", example = "2025-12-15 09:00:00")
    private LocalDateTime ngayHoanThanh;
    
    // ===== KẾT QUẢ KHÁM =====
    
    @Schema(description = "Chẩn đoán", example = "Viêm amidan cấp độ 2")
    private String chanDoan;
    
    @Schema(description = "Kết quả khám", example = "Amidan sưng đỏ...")
    private String ketQuaKham;
    
    @Schema(description = "Đơn thuốc", example = "Amoxicillin 500mg...")
    private String donThuoc;
    
    @Schema(description = "Lời dặn bác sĩ", example = "Uống đủ nước...")
    private String loiDanBacSi;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Ngày tái khám", example = "2025-12-22")
    private LocalDate ngayTaiKham;
    
    // ===== HỦY LỊCH =====
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày hủy", example = "2025-12-10 10:00:00")
    private LocalDateTime ngayHuy;
    
    @Schema(description = "Lý do hủy", example = "Có việc đột xuất")
    private String lyDoHuy;
    
    @Schema(description = "Người hủy", example = "Nguyễn Thị B")
    private String tenNguoiHuy;
    
    // ===== HOÀN TIỀN =====
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày hoàn tiền", example = "2025-12-10 10:30:00")
    private LocalDateTime ngayHoanTien;
    
    @Schema(description = "Số tiền hoàn", example = "300000")
    private BigDecimal soTienHoan;
    
    @Schema(description = "Lý do hoàn tiền", example = "Bác sĩ từ chối lịch khám")
    private String lyDoHoanTien;
    
    // ===== REMINDER =====
    
    @Schema(description = "Đã gửi email nhắc nhở chưa", example = "true")
    private Boolean daNhacNho;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày gửi nhắc nhở", example = "2025-12-14 08:00:00")
    private LocalDateTime ngayNhacNho;
    
    // ===== ĐÁNH GIÁ =====
    
    @Schema(description = "Số sao đánh giá (1-5)", example = "5")
    private Integer soSao;
    
    @Schema(description = "Nhận xét", example = "Bác sĩ tận tình...")
    private String nhanXet;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Ngày đánh giá", example = "2025-12-15 10:00:00")
    private LocalDateTime ngayDanhGia;
    
    // ===== COMPUTED FIELDS =====
    
    @Schema(description = "Có thể hủy không", example = "true")
    private Boolean canCancel;
    
    @Schema(description = "Có thể đánh giá không", example = "false")
    private Boolean canRate;
    
    @Schema(description = "Đã đánh giá chưa", example = "false")
    private Boolean isRated;
    
    @Schema(description = "Có hẹn tái khám không", example = "true")
    private Boolean hasFollowUp;
    
    // ===== FACTORY METHOD =====
    
    /**
     * Alias for from() method (for convenience)
     */
    public static BookingResponse of(DatLichKham entity) {
        return from(entity);
    }
    
    /**
     * Convert từ entity sang DTO
     */
    public static BookingResponse from(DatLichKham entity) {
        if (entity == null) return null;
        
        BookingResponse response = BookingResponse.builder()
                .datLichID(entity.getDatLichID())
                .maXacNhan(entity.getMaXacNhan())
                .ngayDat(entity.getNgayDat())
                
                // Thông tin bác sĩ
                .bacSiID(entity.getBacSi().getBacSiID())
                .tenBacSi(entity.getBacSi().getNguoiDung().getHoTen())
                .tenChuyenKhoa(entity.getBacSi().getChuyenKhoa().getTenChuyenKhoa())
                .tenTrinhDo(entity.getBacSi().getTrinhDo().getTenTrinhDo())
                .avatarBacSi(entity.getBacSi().getNguoiDung().getAvatarUrl())
                .sdtBacSi(entity.getBacSi().getNguoiDung().getSoDienThoai())
                
                // Thông tin bệnh nhân
                .benhNhanID(entity.getBenhNhan().getNguoiDungID())
                .tenBenhNhan(entity.getBenhNhan().getHoTen())
                .sdtBenhNhan(entity.getBenhNhan().getSoDienThoai())
                .emailBenhNhan(entity.getBenhNhan().getEmail())
                
                // Thời gian
                .ngayKham(entity.getNgayKham())
                .ca(entity.getCa())
                .gioKham(entity.getGioKham())
                
                // Lý do khám
                .lyDoKham(entity.getLyDoKham())
                .ghiChu(entity.getGhiChu())
                
                // Trạng thái
                .trangThai(entity.getTrangThai())
                
                // Thanh toán
                .giaKham(entity.getGiaKham())
                .phuongThucThanhToan(entity.getPhuongThucThanhToan())
                .trangThaiThanhToan(entity.getTrangThaiThanhToan())
                .ngayThanhToan(entity.getNgayThanhToan())
                .maGiaoDich(entity.getMaGiaoDich())
                
                // Xác nhận bác sĩ
                .ngayBacSiXacNhan(entity.getNgayBacSiXacNhan())
                .lyDoTuChoi(entity.getLyDoTuChoi())
                
                // Check-in & khám
                .ngayCheckIn(entity.getNgayCheckIn())
                .ngayKhamThucTe(entity.getNgayKhamThucTe())
                .ngayHoanThanh(entity.getNgayHoanThanh())
                
                // Kết quả khám
                .chanDoan(entity.getChanDoan())
                .ketQuaKham(entity.getKetQuaKham())
                .donThuoc(entity.getDonThuoc())
                .loiDanBacSi(entity.getLoiDanBacSi())
                .ngayTaiKham(entity.getNgayTaiKham())
                
                // Hủy lịch
                .ngayHuy(entity.getNgayHuy())
                .lyDoHuy(entity.getLyDoHuy())
                .tenNguoiHuy(entity.getNguoiHuy() != null ? entity.getNguoiHuy().getHoTen() : null)
                
                // Hoàn tiền
                .ngayHoanTien(entity.getNgayHoanTien())
                .soTienHoan(entity.getSoTienHoan())
                .lyDoHoanTien(entity.getLyDoHoanTien())
                
                // Reminder
                .daNhacNho(entity.getDaNhacNho())
                .ngayNhacNho(entity.getNgayNhacNho())
                
                // Đánh giá
                .soSao(entity.getSoSao())
                .nhanXet(entity.getNhanXet())
                .ngayDanhGia(entity.getNgayDanhGia())
                
                .build();
        
        // Calculate computed fields
        response.calculate();
        
        return response;
    }
    
    /**
     * Calculate computed fields & display values
     */
    private void calculate() {
        // Trạng thái
        if (trangThai != null) {
            this.tenTrangThai = trangThai.getMoTa();
            this.mauSacTrangThai = trangThai.getMauSac();
            this.canCancel = trangThai.isCanCancel();
        }
        
        // Ca làm việc
        if (ca != null) {
            this.tenCa = ca.getLabel();
        }
        
        // Thời gian khám display
        if (ngayKham != null && ca != null && gioKham != null) {
            this.thoiGianKhamDisplay = String.format("%s - %s - %s", 
                ngayKham.toString(), 
                ca.getTenCa(), 
                gioKham.toString());
        }
        
        // Giá khám display
        if (giaKham != null) {
            this.giaKhamDisplay = String.format("%,.0f đ", giaKham);
        }
        
        // Phương thức thanh toán
        if (phuongThucThanhToan != null) {
            this.tenPhuongThucThanhToan = phuongThucThanhToan.getMoTa();
        }
        
        // Trạng thái thanh toán
        if (trangThaiThanhToan != null) {
            this.tenTrangThaiThanhToan = trangThaiThanhToan.getMoTa();
        }
        
        // Đánh giá
        this.canRate = (trangThai == TrangThaiDatLich.HOAN_THANH && soSao == null);
        this.isRated = (soSao != null && ngayDanhGia != null);
        this.hasFollowUp = (ngayTaiKham != null && ngayTaiKham.isAfter(LocalDate.now()));
    }
}

