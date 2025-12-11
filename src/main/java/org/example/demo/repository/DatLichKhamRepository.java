package org.example.demo.repository;

import org.example.demo.entity.DatLichKham;
import org.example.demo.enums.CaLamViec;
import org.example.demo.enums.TrangThaiDatLich;
import org.example.demo.enums.PhuongThucThanhToan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
 * Repository cho DatLichKham (Booking/Appointment) - Phase 2
 * 
 * Features:
 * - Available slots detection
 * - Conflict checking (prevent double booking)
 * - Search with multiple filters
 * - Statistics queries
 * - Reminder system queries
 * - Doctor pending approvals
 * 
 * @author Healthcare System Team
 * @version 1.0 - Phase 2
 */
@Repository
public interface DatLichKhamRepository extends JpaRepository<DatLichKham, Integer> {
    
    // ==========================================
    // BASIC QUERIES
    // ==========================================
    
    /**
     * Tìm lịch khám theo mã xác nhận (8 ký tự)
     * Use case: Check-in, lookup booking
     */
    Optional<DatLichKham> findByMaXacNhan(String maXacNhan);

    /**
     * Tìm lịch khám theo mã giao dịch thanh toán
     */
    Optional<DatLichKham> findByMaGiaoDich(String maGiaoDich);
    
    /**
     * Tìm tất cả lịch của 1 bệnh nhân
     */
    List<DatLichKham> findByBenhNhan_NguoiDungIDOrderByNgayKhamDesc(Integer benhNhanID);
    
    /**
     * Tìm lịch của bệnh nhân với phân trang (sorted by creation date)
     */
    Page<DatLichKham> findByBenhNhan_NguoiDungIDOrderByNgayDatDesc(Integer benhNhanID, Pageable pageable);
    
    /**
     * Tìm lịch của bác sĩ theo ngày khám
     */
    List<DatLichKham> findByBacSi_BacSiIDAndNgayKhamOrderByGioKhamAsc(Integer bacSiID, LocalDate ngayKham);
    
    /**
     * Check slot đã bị đặt chưa (simplified - không check status)
     */
    boolean existsByBacSi_BacSiIDAndNgayKhamAndCaAndGioKham(
        Integer bacSiID, 
        LocalDate ngayKham, 
        CaLamViec ca, 
        LocalTime gioKham
    );
    
    /**
     * Check bệnh nhân có conflict không
     */
    boolean existsByBenhNhan_NguoiDungIDAndNgayKhamAndCaAndGioKham(
        Integer benhNhanID,
        LocalDate ngayKham,
        CaLamViec ca,
        LocalTime gioKham
    );

    /**
     * Check bệnh nhân có conflict không (chỉ tính booking active, chưa xóa mềm)
     */
    @Query("""
        SELECT COUNT(d) > 0 
        FROM DatLichKham d
        WHERE d.benhNhan.nguoiDungID = :benhNhanID
            AND d.ngayKham = :ngayKham
            AND d.ca = :ca
            AND d.gioKham = :gioKham
            AND d.trangThai IN :activeStatuses
            AND d.isDeleted = false
        """)
    boolean existsActivePatientConflict(
        @Param("benhNhanID") Integer benhNhanID,
        @Param("ngayKham") LocalDate ngayKham,
        @Param("ca") CaLamViec ca,
        @Param("gioKham") LocalTime gioKham,
        @Param("activeStatuses") List<TrangThaiDatLich> activeStatuses
    );
    
    /**
     * Tìm tất cả lịch của 1 bác sĩ
     */
    List<DatLichKham> findByBacSi_BacSiIDOrderByNgayKhamDesc(Integer bacSiID);

    // ==========================================
    // HISTORY QUERIES - Lịch sử khám
    // ==========================================

    @Query("""
        SELECT d FROM DatLichKham d
        WHERE d.benhNhan.nguoiDungID = :benhNhanID
            AND d.isDeleted = false
            AND (:fromDate IS NULL OR d.ngayKham >= :fromDate)
            AND (:toDate IS NULL OR d.ngayKham <= :toDate)
            AND (:status IS NULL OR d.trangThai = :status)
            AND (:paymentMethod IS NULL OR d.phuongThucThanhToan = :paymentMethod)
            AND (:hasRating IS NULL OR (:hasRating = true AND d.soSao IS NOT NULL) OR (:hasRating = false AND d.soSao IS NULL))
            AND (:doctorId IS NULL OR d.bacSi.bacSiID = :doctorId)
            AND (:facilityId IS NULL OR d.coSoYTe.coSoID = :facilityId)
        ORDER BY d.ngayKham DESC, d.gioKham DESC
        """)
    Page<DatLichKham> searchPatientHistory(
        @Param("benhNhanID") Integer benhNhanID,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("status") TrangThaiDatLich status,
        @Param("paymentMethod") PhuongThucThanhToan paymentMethod,
        @Param("hasRating") Boolean hasRating,
        @Param("doctorId") Integer doctorId,
        @Param("facilityId") Integer facilityId,
        Pageable pageable
    );

    @Query("""
        SELECT d FROM DatLichKham d
        WHERE d.bacSi.bacSiID = :bacSiID
            AND d.isDeleted = false
            AND (:fromDate IS NULL OR d.ngayKham >= :fromDate)
            AND (:toDate IS NULL OR d.ngayKham <= :toDate)
            AND (:status IS NULL OR d.trangThai = :status)
            AND (:paymentMethod IS NULL OR d.phuongThucThanhToan = :paymentMethod)
            AND (:hasRating IS NULL OR (:hasRating = true AND d.soSao IS NOT NULL) OR (:hasRating = false AND d.soSao IS NULL))
            AND (:patientId IS NULL OR d.benhNhan.nguoiDungID = :patientId)
            AND (:facilityId IS NULL OR d.coSoYTe.coSoID = :facilityId)
        ORDER BY d.ngayKham DESC, d.gioKham DESC
        """)
    Page<DatLichKham> searchDoctorHistory(
        @Param("bacSiID") Integer bacSiID,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("status") TrangThaiDatLich status,
        @Param("paymentMethod") PhuongThucThanhToan paymentMethod,
        @Param("hasRating") Boolean hasRating,
        @Param("patientId") Integer patientId,
        @Param("facilityId") Integer facilityId,
        Pageable pageable
    );

    @Query("""
        SELECT d FROM DatLichKham d
        WHERE d.isDeleted = false
            AND (:fromDate IS NULL OR d.ngayKham >= :fromDate)
            AND (:toDate IS NULL OR d.ngayKham <= :toDate)
            AND (:status IS NULL OR d.trangThai = :status)
            AND (:paymentMethod IS NULL OR d.phuongThucThanhToan = :paymentMethod)
            AND (:hasRating IS NULL OR (:hasRating = true AND d.soSao IS NOT NULL) OR (:hasRating = false AND d.soSao IS NULL))
            AND (:doctorId IS NULL OR d.bacSi.bacSiID = :doctorId)
            AND (:patientId IS NULL OR d.benhNhan.nguoiDungID = :patientId)
            AND (:facilityId IS NULL OR d.coSoYTe.coSoID = :facilityId)
        ORDER BY d.ngayKham DESC, d.gioKham DESC
        """)
    Page<DatLichKham> searchAdminHistory(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("status") TrangThaiDatLich status,
        @Param("paymentMethod") PhuongThucThanhToan paymentMethod,
        @Param("hasRating") Boolean hasRating,
        @Param("doctorId") Integer doctorId,
        @Param("patientId") Integer patientId,
        @Param("facilityId") Integer facilityId,
        Pageable pageable
    );

    // ==========================================
    // THỐNG KÊ DOANH THU / SỐ CA
    // ==========================================

    @Query("""
        SELECT new org.example.demo.dto.response.SpecialtyRevenueResponse(
            d.bacSi.chuyenKhoa.chuyenKhoaID,
            d.bacSi.chuyenKhoa.tenChuyenKhoa,
            COALESCE(SUM(d.giaKham), 0),
            COUNT(d)
        )
        FROM DatLichKham d
        WHERE d.isDeleted = false
            AND d.trangThai = org.example.demo.enums.TrangThaiDatLich.HOAN_THANH
            AND d.trangThaiThanhToan = org.example.demo.enums.TrangThaiThanhToan.THANH_CONG
            AND (:fromDate IS NULL OR d.ngayKham >= :fromDate)
            AND (:toDate IS NULL OR d.ngayKham <= :toDate)
        GROUP BY d.bacSi.chuyenKhoa.chuyenKhoaID, d.bacSi.chuyenKhoa.tenChuyenKhoa
        ORDER BY SUM(d.giaKham) DESC
        """)
    List<org.example.demo.dto.response.SpecialtyRevenueResponse> revenueBySpecialty(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );

    @Query("""
        SELECT new org.example.demo.dto.response.DoctorRevenueResponse(
            d.bacSi.bacSiID,
            d.bacSi.nguoiDung.hoTen,
            d.bacSi.chuyenKhoa.chuyenKhoaID,
            d.bacSi.chuyenKhoa.tenChuyenKhoa,
            COALESCE(SUM(d.giaKham), 0),
            COUNT(d)
        )
        FROM DatLichKham d
        WHERE d.isDeleted = false
            AND d.trangThai = org.example.demo.enums.TrangThaiDatLich.HOAN_THANH
            AND d.trangThaiThanhToan = org.example.demo.enums.TrangThaiThanhToan.THANH_CONG
            AND (:fromDate IS NULL OR d.ngayKham >= :fromDate)
            AND (:toDate IS NULL OR d.ngayKham <= :toDate)
        GROUP BY d.bacSi.bacSiID, d.bacSi.nguoiDung.hoTen, d.bacSi.chuyenKhoa.chuyenKhoaID, d.bacSi.chuyenKhoa.tenChuyenKhoa
        ORDER BY SUM(d.giaKham) DESC
        """)
    Page<org.example.demo.dto.response.DoctorRevenueResponse> revenueByDoctor(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        Pageable pageable
    );

    @Query("""
        SELECT new org.example.demo.dto.response.DoctorRevenueResponse(
            d.bacSi.bacSiID,
            d.bacSi.nguoiDung.hoTen,
            d.bacSi.chuyenKhoa.chuyenKhoaID,
            d.bacSi.chuyenKhoa.tenChuyenKhoa,
            COALESCE(SUM(d.giaKham), 0),
            COUNT(d)
        )
        FROM DatLichKham d
        WHERE d.isDeleted = false
            AND d.trangThai = org.example.demo.enums.TrangThaiDatLich.HOAN_THANH
            AND d.trangThaiThanhToan = org.example.demo.enums.TrangThaiThanhToan.THANH_CONG
            AND (:fromDate IS NULL OR d.ngayKham >= :fromDate)
            AND (:toDate IS NULL OR d.ngayKham <= :toDate)
        GROUP BY d.bacSi.bacSiID, d.bacSi.nguoiDung.hoTen, d.bacSi.chuyenKhoa.chuyenKhoaID, d.bacSi.chuyenKhoa.tenChuyenKhoa
        ORDER BY SUM(d.giaKham) DESC
        """)
    Page<org.example.demo.dto.response.DoctorRevenueResponse> topDoctorRevenue(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        Pageable pageable
    );

    @Query("""
        SELECT new org.example.demo.dto.response.DoctorRevenueResponse(
            d.bacSi.bacSiID,
            d.bacSi.nguoiDung.hoTen,
            d.bacSi.chuyenKhoa.chuyenKhoaID,
            d.bacSi.chuyenKhoa.tenChuyenKhoa,
            COALESCE(SUM(d.giaKham), 0),
            COUNT(d)
        )
        FROM DatLichKham d
        WHERE d.isDeleted = false
            AND d.trangThai = org.example.demo.enums.TrangThaiDatLich.HOAN_THANH
            AND d.trangThaiThanhToan = org.example.demo.enums.TrangThaiThanhToan.THANH_CONG
            AND (:fromDate IS NULL OR d.ngayKham >= :fromDate)
            AND (:toDate IS NULL OR d.ngayKham <= :toDate)
        GROUP BY d.bacSi.bacSiID, d.bacSi.nguoiDung.hoTen, d.bacSi.chuyenKhoa.chuyenKhoaID, d.bacSi.chuyenKhoa.tenChuyenKhoa
        ORDER BY COUNT(d) DESC, SUM(d.giaKham) DESC
        """)
    Page<org.example.demo.dto.response.DoctorRevenueResponse> topDoctorCompleted(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        Pageable pageable
    );
    
    // ==========================================
    // AVAILABLE SLOTS - Tìm slot trống
    // ==========================================
    
    /**
     * Tìm tất cả booking đã được đặt (active) cho 1 bác sĩ/ngày/ca
     * Active = CHO_XAC_NHAN_BAC_SI, DA_XAC_NHAN, DANG_KHAM
     * 
     * Use case: Calculate available slots
     * 
     * @return List giờ khám đã bị đặt (active bookings only)
     */
    @Query("""
        SELECT d.gioKham 
        FROM DatLichKham d 
        WHERE d.bacSi.bacSiID = :bacSiID 
            AND d.ngayKham = :ngayKham 
            AND d.ca = :ca 
            AND d.trangThai IN :activeStatuses
            AND d.isDeleted = false
        ORDER BY d.gioKham
        """)
    List<LocalTime> findBookedTimeSlots(
        @Param("bacSiID") Integer bacSiID,
        @Param("ngayKham") LocalDate ngayKham,
        @Param("ca") CaLamViec ca,
        @Param("activeStatuses") List<TrangThaiDatLich> activeStatuses
    );
    
    /**
     * Check slot có bị đặt (conflict) không
     * 
     * Use case: Validate before creating booking
     * 
     * @return true nếu slot đã bị đặt (conflict)
     */
    @Query("""
        SELECT COUNT(d) > 0 
        FROM DatLichKham d 
        WHERE d.bacSi.bacSiID = :bacSiID 
            AND d.ngayKham = :ngayKham 
            AND d.ca = :ca 
            AND d.gioKham = :gioKham 
            AND d.trangThai IN :activeStatuses
            AND d.isDeleted = false
        """)
    boolean existsActiveBookingForSlot(
        @Param("bacSiID") Integer bacSiID,
        @Param("ngayKham") LocalDate ngayKham,
        @Param("ca") CaLamViec ca,
        @Param("gioKham") LocalTime gioKham,
        @Param("activeStatuses") List<TrangThaiDatLich> activeStatuses
    );
    
    // ==========================================
    // SEARCH WITH FILTERS - Tìm kiếm nâng cao
    // ==========================================
    
    /**
     * Tìm lịch của bệnh nhân theo trạng thái (paginated)
     */
    Page<DatLichKham> findByBenhNhan_NguoiDungIDAndTrangThai(
        Integer benhNhanID,
        TrangThaiDatLich trangThai,
        Pageable pageable
    );
    
    /**
     * Tìm lịch của bệnh nhân trong khoảng thời gian
     */
    @Query("""
        SELECT d FROM DatLichKham d 
        WHERE d.benhNhan.nguoiDungID = :benhNhanID 
            AND d.ngayKham BETWEEN :fromDate AND :toDate
            AND (:trangThai IS NULL OR d.trangThai = :trangThai)
            AND d.isDeleted = false
        ORDER BY d.ngayKham DESC, d.gioKham DESC
        """)
    Page<DatLichKham> searchPatientBookings(
        @Param("benhNhanID") Integer benhNhanID,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("trangThai") TrangThaiDatLich trangThai,
        Pageable pageable
    );
    
    /**
     * Tìm lịch của bác sĩ theo trạng thái (paginated)
     */
    Page<DatLichKham> findByBacSi_BacSiIDAndTrangThai(
        Integer bacSiID,
        TrangThaiDatLich trangThai,
        Pageable pageable
    );
    
    /**
     * Tìm lịch của bác sĩ trong khoảng thời gian
     */
    @Query("""
        SELECT d FROM DatLichKham d 
        WHERE d.bacSi.bacSiID = :bacSiID 
            AND d.ngayKham BETWEEN :fromDate AND :toDate
            AND (:trangThai IS NULL OR d.trangThai = :trangThai)
            AND d.isDeleted = false
        ORDER BY d.ngayKham DESC, d.gioKham DESC
        """)
    Page<DatLichKham> searchDoctorBookings(
        @Param("bacSiID") Integer bacSiID,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("trangThai") TrangThaiDatLich trangThai,
        Pageable pageable
    );
    
    /**
     * Tìm lịch sắp tới của bệnh nhân (upcoming only)
     */
    @Query("""
        SELECT d FROM DatLichKham d 
        WHERE d.benhNhan.nguoiDungID = :benhNhanID 
            AND d.ngayKham >= :today
            AND d.trangThai IN :activeStatuses
            AND d.isDeleted = false
        ORDER BY d.ngayKham ASC, d.gioKham ASC
        """)
    List<DatLichKham> findUpcomingBookings(
        @Param("benhNhanID") Integer benhNhanID,
        @Param("today") LocalDate today,
        @Param("activeStatuses") List<TrangThaiDatLich> activeStatuses
    );
    
    // ==========================================
    // DOCTOR APPROVAL - Bác sĩ xác nhận
    // ==========================================
    
    /**
     * Tìm lịch chờ bác sĩ xác nhận (pending approval)
     */
    @Query("""
        SELECT d FROM DatLichKham d 
        WHERE d.bacSi.bacSiID = :bacSiID 
            AND d.trangThai = 'CHO_XAC_NHAN_BAC_SI'
            AND d.isDeleted = false
        ORDER BY d.createdAt ASC
        """)
    List<DatLichKham> findPendingApprovalForDoctor(@Param("bacSiID") Integer bacSiID);
    
    /**
     * Đếm số lịch chờ bác sĩ xác nhận
     */
    @Query("""
        SELECT COUNT(d) 
        FROM DatLichKham d 
        WHERE d.bacSi.bacSiID = :bacSiID 
            AND d.trangThai = 'CHO_XAC_NHAN_BAC_SI'
            AND d.isDeleted = false
        """)
    Long countPendingApprovalForDoctor(@Param("bacSiID") Integer bacSiID);
    
    // ==========================================
    // REMINDER SYSTEM - Nhắc nhở
    // ==========================================
    
    /**
     * Tìm lịch cần gửi reminder (24h trước giờ khám)
     * 
     * Criteria:
     * - TrangThai = DA_XAC_NHAN
     * - NgayKham = tomorrow (24h trước)
     * - DaNhacNho = false
     * 
     * Use case: Cron job gửi email nhắc nhở
     */
    @Query("""
        SELECT d FROM DatLichKham d 
        WHERE d.trangThai = 'DA_XAC_NHAN'
            AND d.ngayKham = :tomorrow
            AND d.daNhacNho = false
            AND d.isDeleted = false
        ORDER BY d.gioKham ASC
        """)
    List<DatLichKham> findBookingsNeedingReminder(@Param("tomorrow") LocalDate tomorrow);
    
    /**
     * Tìm lịch quá hạn cần auto-update status
     * 
     * Criteria:
     * - NgayKham < today
     * - TrangThai IN (CHO_XAC_NHAN_BAC_SI, DA_XAC_NHAN)
     * 
     * Use case: Cron job tự động chuyển sang QUA_HAN
     */
    @Query("""
        SELECT d FROM DatLichKham d 
        WHERE d.ngayKham < :today
            AND d.trangThai IN ('CHO_XAC_NHAN_BAC_SI', 'DA_XAC_NHAN')
            AND d.isDeleted = false
        """)
    List<DatLichKham> findExpiredBookings(@Param("today") LocalDate today);
    
    // ==========================================
    // STATISTICS - Thống kê
    // ==========================================
    
    /**
     * Đếm số lịch theo trạng thái
     */
    @Query("""
        SELECT COUNT(d) 
        FROM DatLichKham d 
        WHERE d.trangThai = :trangThai
            AND d.isDeleted = false
        """)
    Long countByTrangThai(@Param("trangThai") TrangThaiDatLich trangThai);
    
    /**
     * Đếm số lịch của bác sĩ theo trạng thái
     */
    @Query("""
        SELECT COUNT(d) 
        FROM DatLichKham d 
        WHERE d.bacSi.bacSiID = :bacSiID
            AND d.trangThai = :trangThai
            AND d.isDeleted = false
        """)
    Long countByDoctorAndStatus(
        @Param("bacSiID") Integer bacSiID,
        @Param("trangThai") TrangThaiDatLich trangThai
    );
    
    /**
     * Đếm số lịch hôm nay
     */
    @Query("""
        SELECT COUNT(d) 
        FROM DatLichKham d 
        WHERE d.ngayKham = :today
            AND d.isDeleted = false
        """)
    Long countTodayBookings(@Param("today") LocalDate today);
    
    /**
     * Đếm số lịch của bác sĩ hôm nay
     */
    @Query("""
        SELECT COUNT(d) 
        FROM DatLichKham d 
        WHERE d.bacSi.bacSiID = :bacSiID
            AND d.ngayKham = :today
            AND d.isDeleted = false
        """)
    Long countDoctorTodayBookings(
        @Param("bacSiID") Integer bacSiID,
        @Param("today") LocalDate today
    );
    
    /**
     * Tính tổng doanh thu đã thanh toán
     */
    @Query("""
        SELECT COALESCE(SUM(d.giaKham), 0) 
        FROM DatLichKham d 
        WHERE d.trangThaiThanhToan = 'THANH_CONG'
            AND d.isDeleted = false
        """)
    Double calculateTotalRevenue();
    
    /**
     * Tính doanh thu theo khoảng thời gian
     */
    @Query("""
        SELECT COALESCE(SUM(d.giaKham), 0) 
        FROM DatLichKham d 
        WHERE d.trangThaiThanhToan = 'THANH_CONG'
            AND d.ngayThanhToan BETWEEN :fromDate AND :toDate
            AND d.isDeleted = false
        """)
    Double calculateRevenueByDateRange(
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate
    );
    
    /**
     * Tính rating trung bình của bác sĩ
     */
    @Query("""
        SELECT AVG(d.soSao) 
        FROM DatLichKham d 
        WHERE d.bacSi.bacSiID = :bacSiID
            AND d.soSao IS NOT NULL
            AND d.isDeleted = false
        """)
    Double calculateDoctorAverageRating(@Param("bacSiID") Integer bacSiID);
    
    /**
     * Đếm số đánh giá của bác sĩ
     */
    @Query("""
        SELECT COUNT(d) 
        FROM DatLichKham d 
        WHERE d.bacSi.bacSiID = :bacSiID
            AND d.soSao IS NOT NULL
            AND d.isDeleted = false
        """)
    Long countDoctorRatings(@Param("bacSiID") Integer bacSiID);
    
    /**
     * Đếm số đánh giá theo số sao
     */
    @Query("""
        SELECT COUNT(d) 
        FROM DatLichKham d 
        WHERE d.bacSi.bacSiID = :bacSiID
            AND d.soSao = :stars
            AND d.isDeleted = false
        """)
    Long countRatingsByStars(
        @Param("bacSiID") Integer bacSiID,
        @Param("stars") Integer stars
    );
    
    // ==========================================
    // SPECIAL QUERIES
    // ==========================================
    
    /**
     * Tìm lịch khám đang active (can cancel/can reschedule)
     */
    @Query("""
        SELECT d FROM DatLichKham d 
        WHERE d.datLichID = :datLichID
            AND d.trangThai IN :cancellableStatuses
            AND d.isDeleted = false
        """)
    Optional<DatLichKham> findCancellableBooking(
        @Param("datLichID") Integer datLichID,
        @Param("cancellableStatuses") List<TrangThaiDatLich> cancellableStatuses
    );
    
    /**
     * Tìm lịch khám có thể đánh giá (completed & not rated)
     */
    @Query("""
        SELECT d FROM DatLichKham d 
        WHERE d.datLichID = :datLichID
            AND d.trangThai = 'HOAN_THANH'
            AND d.soSao IS NULL
            AND d.isDeleted = false
        """)
    Optional<DatLichKham> findRateableBooking(@Param("datLichID") Integer datLichID);
    
    /**
     * Tìm lịch khám của bệnh nhân trong ngày (để check no-show)
     */
    @Query("""
        SELECT d FROM DatLichKham d 
        WHERE d.benhNhan.nguoiDungID = :benhNhanID
            AND d.ngayKham = :date
            AND d.trangThai = 'DA_XAC_NHAN'
            AND d.isDeleted = false
        """)
    List<DatLichKham> findPatientBookingsOnDate(
        @Param("benhNhanID") Integer benhNhanID,
        @Param("date") LocalDate date
    );
    
    /**
     * Tìm lịch khám có hẹn tái khám
     */
    @Query("""
        SELECT d FROM DatLichKham d 
        WHERE d.benhNhan.nguoiDungID = :benhNhanID
            AND d.ngayTaiKham IS NOT NULL
            AND d.ngayTaiKham >= :today
            AND d.trangThai = 'HOAN_THANH'
            AND d.isDeleted = false
        ORDER BY d.ngayTaiKham ASC
        """)
    List<DatLichKham> findActiveFollowUps(
        @Param("benhNhanID") Integer benhNhanID,
        @Param("today") LocalDate today
    );
    
    /**
     * Tổng số tiền hoàn
     */
    @Query("""
        SELECT COALESCE(SUM(d.soTienHoan), 0) 
        FROM DatLichKham d 
        WHERE d.soTienHoan IS NOT NULL
            AND d.isDeleted = false
        """)
    Double calculateTotalRefund();
    
    /**
     * Đếm số lần hoàn tiền
     */
    @Query("""
        SELECT COUNT(d) 
        FROM DatLichKham d 
        WHERE d.soTienHoan IS NOT NULL
            AND d.isDeleted = false
        """)
    Long countRefunds();

    // ==========================================
    // GLOBAL STATISTICS (Admin dashboard)
    // ==========================================

    @Query("""
        SELECT COUNT(d)
        FROM DatLichKham d
        WHERE d.isDeleted = false
        """)
    Long countAllActive();

    @Query("""
        SELECT COUNT(d)
        FROM DatLichKham d
        WHERE d.ngayKham BETWEEN :from AND :to
            AND d.isDeleted = false
        """)
    Long countByDateRange(
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );

    @Query("""
        SELECT COUNT(d)
        FROM DatLichKham d
        WHERE d.trangThaiThanhToan = :status
            AND d.isDeleted = false
        """)
    Long countByPaymentStatus(@Param("status") org.example.demo.enums.TrangThaiThanhToan status);

    @Query("""
        SELECT COUNT(d)
        FROM DatLichKham d
        WHERE d.soSao IS NOT NULL
            AND d.isDeleted = false
        """)
    Long countRatingsAll();

    @Query("""
        SELECT COUNT(d)
        FROM DatLichKham d
        WHERE d.soSao IS NOT NULL
            AND d.soSao = :stars
            AND d.isDeleted = false
        """)
    Long countRatingsByStarsAll(@Param("stars") Integer stars);

    @Query("""
        SELECT AVG(d.soSao)
        FROM DatLichKham d
        WHERE d.soSao IS NOT NULL
            AND d.isDeleted = false
        """)
    Double calculateAverageRatingAll();
}

