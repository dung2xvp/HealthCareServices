package org.example.demo.repository;

import org.example.demo.entity.BacSi;
import org.example.demo.entity.LichDatKham;
import org.example.demo.entity.NguoiDung;
import org.example.demo.enums.TrangThaiLichDat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho LichDatKham (Appointment)
 * Xử lý: Booking, kiểm tra slot trống, quản lý lịch khám
 */
@Repository
public interface LichDatKhamRepository extends JpaRepository<LichDatKham, Integer> {
    
    /**
     * Lấy tất cả lịch đặt của bệnh nhân
     * Dùng trong trang "Lịch khám của tôi"
     */
    List<LichDatKham> findByBenhNhan_NguoiDungID(Integer benhNhanId);
    
    /**
     * Lấy lịch đặt của bệnh nhân theo trạng thái
     * Ví dụ: Chỉ lấy lịch đã xác nhận, hoặc chờ xác nhận
     */
    List<LichDatKham> findByBenhNhan_NguoiDungIDAndTrangThai(
            Integer benhNhanId, 
            TrangThaiLichDat trangThai
    );
    
    /**
     * Lấy tất cả lịch đặt của bác sĩ
     * Dùng trong trang quản lý lịch của bác sĩ
     */
    List<LichDatKham> findByBacSi_BacSiID(Integer bacSiId);
    
    /**
     * Lấy lịch đặt của bác sĩ trong khoảng thời gian
     * Ví dụ: Xem lịch tuần này, tháng này
     */
    List<LichDatKham> findByBacSi_BacSiIDAndNgayKhamBetween(
            Integer bacSiId,
            LocalDate fromDate,
            LocalDate toDate
    );
    
    /**
     * KIỂM TRA SLOT ĐÃ ĐƯỢC ĐẶT CHƯA (Ngăn double booking)
     * Tìm lịch của bác sĩ vào ngày và giờ cụ thể, trạng thái != HUY
     * Nếu có kết quả = slot đã đặt, không cho đặt nữa
     */
    @Query("SELECT l FROM LichDatKham l WHERE l.bacSi.bacSiID = :bacSiId " +
           "AND l.ngayKham = :ngayKham " +
           "AND l.gioKham = :gioKham " +
           "AND l.trangThai != 'HUY'")
    Optional<LichDatKham> findExistingAppointment(
            @Param("bacSiId") Integer bacSiId,
            @Param("ngayKham") LocalDate ngayKham,
            @Param("gioKham") LocalTime gioKham
    );
    
    /**
     * Đếm số lịch đặt của bác sĩ trong ngày (để check giới hạn)
     * Ví dụ: Bác sĩ chỉ nhận tối đa 20 bệnh nhân/ngày
     */
    @Query("SELECT COUNT(l) FROM LichDatKham l WHERE l.bacSi.bacSiID = :bacSiId " +
           "AND l.ngayKham = :ngayKham " +
           "AND l.trangThai NOT IN ('HUY')")
    Long countAppointmentsByDoctorAndDate(
            @Param("bacSiId") Integer bacSiId,
            @Param("ngayKham") LocalDate ngayKham
    );
    
    /**
     * Lấy tất cả lịch đặt của bác sĩ trong ngày (để show slots đã đặt)
     */
    List<LichDatKham> findByBacSi_BacSiIDAndNgayKhamAndTrangThaiNot(
            Integer bacSiId,
            LocalDate ngayKham,
            TrangThaiLichDat trangThai
    );
    
    /**
     * Lấy lịch đặt cần gửi REMINDER (trước 2 giờ)
     * Chạy bằng scheduled task
     */
    @Query("SELECT l FROM LichDatKham l WHERE " +
           "l.trangThai IN ('ChoXacNhan', 'DaXacNhan') " +
           "AND l.ngayKham = :ngay " +
           "AND l.gioKham BETWEEN :gioFrom AND :gioTo")
    List<LichDatKham> findAppointmentsNeedingReminder(
            @Param("ngay") LocalDate ngay,
            @Param("gioFrom") LocalTime gioFrom,
            @Param("gioTo") LocalTime gioTo
    );
    
    /**
     * Lấy lịch đặt theo mã đơn hàng (dùng cho payment callback)
     */
    Optional<LichDatKham> findByMaDonHang(String maDonHang);
    
    /**
     * Lấy lịch khám đã hoàn thành của bệnh nhân (để xem lịch sử)
     */
    List<LichDatKham> findByBenhNhan_NguoiDungIDAndTrangThaiOrderByNgayKhamDesc(
            Integer benhNhanId,
            TrangThaiLichDat trangThai
    );
    
    /**
     * Kiểm tra bệnh nhân có lịch trùng không (cùng ngày giờ)
     */
    @Query("SELECT COUNT(l) FROM LichDatKham l WHERE " +
           "l.benhNhan.nguoiDungID = :benhNhanId " +
           "AND l.ngayKham = :ngayKham " +
           "AND l.gioKham = :gioKham " +
           "AND l.trangThai NOT IN ('HUY', 'HoanThanh')")
    Long countPatientConflictingAppointments(
            @Param("benhNhanId") Integer benhNhanId,
            @Param("ngayKham") LocalDate ngayKham,
            @Param("gioKham") LocalTime gioKham
    );
    
    /**
     * Thống kê: Tổng số lịch đặt theo trạng thái trong khoảng thời gian
     */
    @Query("SELECT COUNT(l) FROM LichDatKham l WHERE " +
           "l.trangThai = :trangThai " +
           "AND l.createdAt BETWEEN :fromDate AND :toDate")
    Long countByStatusAndDateRange(
            @Param("trangThai") TrangThaiLichDat trangThai,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );
}

