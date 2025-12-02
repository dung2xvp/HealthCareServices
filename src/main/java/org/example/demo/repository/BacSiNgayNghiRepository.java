package org.example.demo.repository;

import org.example.demo.entity.BacSiNgayNghi;
import org.example.demo.enums.CaLamViec;
import org.example.demo.enums.LoaiNghi;
import org.example.demo.enums.TrangThaiNghi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository: Yêu cầu nghỉ của bác sĩ (với approval workflow)
 */
@Repository
public interface BacSiNgayNghiRepository extends JpaRepository<BacSiNgayNghi, Integer> {

    // ==========================================
    // QUERIES CHO ADMIN - PHÂN LOẠI THEO TRẠNG THÁI
    // ==========================================

    /**
     * Lấy tất cả yêu cầu chờ duyệt (Admin dashboard)
     */
    @Query("SELECT n FROM BacSiNgayNghi n WHERE n.trangThai = 'CHO_DUYET' AND n.isDeleted = false ORDER BY n.createdAt ASC")
    List<BacSiNgayNghi> findAllPendingRequests();

    /**
     * Lấy yêu cầu chờ duyệt theo bác sĩ
     */
    @Query("SELECT n FROM BacSiNgayNghi n WHERE n.bacSi.bacSiID = :bacSiID AND n.trangThai = 'CHO_DUYET' AND n.isDeleted = false ORDER BY n.createdAt ASC")
    List<BacSiNgayNghi> findPendingByBacSi(@Param("bacSiID") Integer bacSiID);

    /**
     * Đếm số yêu cầu chờ duyệt (hiển thị badge)
     */
    @Query("SELECT COUNT(n) FROM BacSiNgayNghi n WHERE n.trangThai = 'CHO_DUYET' AND n.isDeleted = false")
    Long countPendingRequests();

    // ==========================================
    // QUERIES CHO BÁC SĨ - LỊCH SỬ YÊU CẦU
    // ==========================================

    /**
     * Lấy tất cả yêu cầu nghỉ của một bác sĩ
     */
    @Query("SELECT n FROM BacSiNgayNghi n WHERE n.bacSi.bacSiID = :bacSiID AND n.isDeleted = false ORDER BY n.createdAt DESC")
    List<BacSiNgayNghi> findByBacSi(@Param("bacSiID") Integer bacSiID);

    /**
     * Lấy yêu cầu nghỉ theo trạng thái
     */
    @Query("SELECT n FROM BacSiNgayNghi n WHERE n.bacSi.bacSiID = :bacSiID AND n.trangThai = :trangThai AND n.isDeleted = false ORDER BY n.createdAt DESC")
    List<BacSiNgayNghi> findByBacSiAndTrangThai(@Param("bacSiID") Integer bacSiID, @Param("trangThai") TrangThaiNghi trangThai);

    // ==========================================
    // QUERIES CHO VALIDATION - KIỂM TRA XUNG ĐỘT
    // ==========================================

    /**
     * Kiểm tra bác sĩ đã nghỉ ngày cụ thể chưa (đã duyệt)
     * Dùng để validate: không cho đăng ký nghỉ trùng
     */
    @Query("SELECT n FROM BacSiNgayNghi n WHERE n.bacSi.bacSiID = :bacSiID " +
            "AND n.loaiNghi = 'NGAY_CU_THE' " +
            "AND n.ngayNghiCuThe = :ngay " +
            "AND n.trangThai = 'DA_DUYET' " +
            "AND n.isDeleted = false")
    List<BacSiNgayNghi> findApprovedLeaveOnDate(@Param("bacSiID") Integer bacSiID, @Param("ngay") LocalDate ngay);

    /**
     * Kiểm tra bác sĩ đã nghỉ ca cụ thể chưa (đã duyệt)
     */
    @Query("SELECT n FROM BacSiNgayNghi n WHERE n.bacSi.bacSiID = :bacSiID " +
            "AND n.loaiNghi = 'CA_CU_THE' " +
            "AND n.ngayNghiCuThe = :ngay " +
            "AND n.ca = :ca " +
            "AND n.trangThai = 'DA_DUYET' " +
            "AND n.isDeleted = false")
    List<BacSiNgayNghi> findApprovedLeaveOnDateAndShift(@Param("bacSiID") Integer bacSiID, @Param("ngay") LocalDate ngay, @Param("ca") CaLamViec ca);

    /**
     * Kiểm tra bác sĩ có nghỉ hàng tuần vào thứ + ca này không (đã duyệt)
     */
    @Query("SELECT n FROM BacSiNgayNghi n WHERE n.bacSi.bacSiID = :bacSiID " +
            "AND n.loaiNghi = 'CA_HANG_TUAN' " +
            "AND n.thuTrongTuan = :thu " +
            "AND (n.ca = :ca OR n.ca IS NULL) " +
            "AND n.trangThai = 'DA_DUYET' " +
            "AND n.isDeleted = false")
    List<BacSiNgayNghi> findApprovedWeeklyLeave(@Param("bacSiID") Integer bacSiID, @Param("thu") Integer thu, @Param("ca") CaLamViec ca);

    // ==========================================
    // QUERIES CHO BUSINESS LOGIC - LẤY LỊCH NGHỈ THỰC TẾ
    // ==========================================

    /**
     * Lấy tất cả lịch nghỉ đã duyệt của bác sĩ trong khoảng thời gian
     * Dùng để tính available slots
     */
    @Query("SELECT n FROM BacSiNgayNghi n WHERE n.bacSi.bacSiID = :bacSiID " +
            "AND n.trangThai = 'DA_DUYET' " +
            "AND n.isDeleted = false " +
            "AND ((n.loaiNghi IN ('NGAY_CU_THE', 'CA_CU_THE') AND n.ngayNghiCuThe BETWEEN :startDate AND :endDate) " +
            "OR n.loaiNghi = 'CA_HANG_TUAN')")
    List<BacSiNgayNghi> findApprovedLeavesInRange(@Param("bacSiID") Integer bacSiID, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Lấy lịch nghỉ đã duyệt của bác sĩ tại một ngày cụ thể
     * Bao gồm: NGAY_CU_THE, CA_CU_THE (ngày đó) + CA_HANG_TUAN (thứ đó)
     */
    @Query("SELECT n FROM BacSiNgayNghi n WHERE n.bacSi.bacSiID = :bacSiID " +
            "AND n.trangThai = 'DA_DUYET' " +
            "AND n.isDeleted = false " +
            "AND ((n.loaiNghi IN ('NGAY_CU_THE', 'CA_CU_THE') AND n.ngayNghiCuThe = :ngay) " +
            "OR (n.loaiNghi = 'CA_HANG_TUAN' AND n.thuTrongTuan = :thu))")
    List<BacSiNgayNghi> findApprovedLeavesOnDate(@Param("bacSiID") Integer bacSiID, @Param("ngay") LocalDate ngay, @Param("thu") Integer thu);

    /**
     * Lấy lịch nghỉ hàng tuần đã duyệt của bác sĩ
     */
    @Query("SELECT n FROM BacSiNgayNghi n WHERE n.bacSi.bacSiID = :bacSiID " +
            "AND n.loaiNghi = 'CA_HANG_TUAN' " +
            "AND n.trangThai = 'DA_DUYET' " +
            "AND n.isDeleted = false")
    List<BacSiNgayNghi> findApprovedWeeklyLeaves(@Param("bacSiID") Integer bacSiID);

    // ==========================================
    // QUERIES CHO THỐNG KÊ
    // ==========================================

    /**
     * Đếm số ngày đã nghỉ theo loại nghỉ phép (dùng để trừ ngày phép)
     * Chỉ tính các yêu cầu đã duyệt + loại phép = PHEP_NAM
     */
    @Query("SELECT COUNT(n) FROM BacSiNgayNghi n WHERE n.bacSi.bacSiID = :bacSiID " +
            "AND n.trangThai = 'DA_DUYET' " +
            "AND n.loaiNghiPhep = 'PHEP_NAM' " +
            "AND n.loaiNghi = 'NGAY_CU_THE' " +
            "AND YEAR(n.ngayNghiCuThe) = :year " +
            "AND n.isDeleted = false")
    Long countUsedAnnualLeave(@Param("bacSiID") Integer bacSiID, @Param("year") Integer year);

    /**
     * Thống kê yêu cầu nghỉ theo trạng thái (Dashboard)
     */
    @Query("SELECT n.trangThai, COUNT(n) FROM BacSiNgayNghi n WHERE n.isDeleted = false GROUP BY n.trangThai")
    List<Object[]> countByTrangThai();

    /**
     * Thống kê yêu cầu nghỉ của một bác sĩ theo loại nghỉ phép
     */
    @Query("SELECT n.loaiNghiPhep, COUNT(n) FROM BacSiNgayNghi n WHERE n.bacSi.bacSiID = :bacSiID AND n.trangThai = 'DA_DUYET' AND n.isDeleted = false GROUP BY n.loaiNghiPhep")
    List<Object[]> countByLoaiNghiPhep(@Param("bacSiID") Integer bacSiID);

    // ==========================================
    // QUERIES CHO ADMIN - QUẢN LÝ YÊU CẦU
    // ==========================================

    /**
     * Lấy yêu cầu nghỉ trong khoảng thời gian (Admin report)
     */
    @Query("SELECT n FROM BacSiNgayNghi n WHERE n.createdAt BETWEEN :startDate AND :endDate AND n.isDeleted = false ORDER BY n.createdAt DESC")
    List<BacSiNgayNghi> findByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Tìm kiếm yêu cầu theo lý do (keyword search)
     */
    @Query("SELECT n FROM BacSiNgayNghi n WHERE LOWER(n.lyDo) LIKE LOWER(CONCAT('%', :keyword, '%')) AND n.isDeleted = false ORDER BY n.createdAt DESC")
    List<BacSiNgayNghi> searchByLyDo(@Param("keyword") String keyword);

    /**
     * Lấy yêu cầu theo loại nghỉ
     */
    @Query("SELECT n FROM BacSiNgayNghi n WHERE n.loaiNghi = :loaiNghi AND n.isDeleted = false ORDER BY n.createdAt DESC")
    List<BacSiNgayNghi> findByLoaiNghi(@Param("loaiNghi") LoaiNghi loaiNghi);
}

