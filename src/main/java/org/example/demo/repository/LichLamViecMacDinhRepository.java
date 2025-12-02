package org.example.demo.repository;

import org.example.demo.entity.LichLamViecMacDinh;
import org.example.demo.enums.CaLamViec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository: Lịch làm việc mặc định (áp dụng cho toàn bệnh viện)
 * Admin setup 1 lần, áp dụng cho TẤT CẢ bác sĩ
 */
@Repository
public interface LichLamViecMacDinhRepository extends JpaRepository<LichLamViecMacDinh, Integer> {

    /**
     * Lấy tất cả lịch đang active (chưa bị xóa mềm)
     */
    @Query("SELECT l FROM LichLamViecMacDinh l WHERE l.isDeleted = false ORDER BY l.thuTrongTuan, l.ca")
    List<LichLamViecMacDinh> findAllActive();

    /**
     * Lấy lịch theo thứ trong tuần
     */
    @Query("SELECT l FROM LichLamViecMacDinh l WHERE l.thuTrongTuan = :thu AND l.isDeleted = false AND l.isActive = true ORDER BY l.ca")
    List<LichLamViecMacDinh> findByThuTrongTuan(@Param("thu") Integer thu);

    /**
     * Lấy lịch theo thứ và ca cụ thể
     */
    @Query("SELECT l FROM LichLamViecMacDinh l WHERE l.thuTrongTuan = :thu AND l.ca = :ca AND l.isDeleted = false AND l.isActive = true")
    Optional<LichLamViecMacDinh> findByThuAndCa(@Param("thu") Integer thu, @Param("ca") CaLamViec ca);

    /**
     * Kiểm tra có lịch nào cho thứ + ca cụ thể không (dùng để validate)
     */
    @Query("SELECT COUNT(l) > 0 FROM LichLamViecMacDinh l WHERE l.coSoYTe.coSoID = :coSoID AND l.thuTrongTuan = :thu AND l.ca = :ca AND l.isDeleted = false")
    Boolean existsByCoSoAndThuAndCa(@Param("coSoID") Integer coSoID, @Param("thu") Integer thu, @Param("ca") CaLamViec ca);

    /**
     * Lấy tất cả lịch theo cơ sở y tế (đang active)
     */
    @Query("SELECT l FROM LichLamViecMacDinh l WHERE l.coSoYTe.coSoID = :coSoID AND l.isDeleted = false AND l.isActive = true ORDER BY l.thuTrongTuan, l.ca")
    List<LichLamViecMacDinh> findByCoSoYTe(@Param("coSoID") Integer coSoID);

    /**
     * Đếm số ca đang active cho một cơ sở
     */
    @Query("SELECT COUNT(l) FROM LichLamViecMacDinh l WHERE l.coSoYTe.coSoID = :coSoID AND l.isDeleted = false AND l.isActive = true")
    Long countActiveByCoSo(@Param("coSoID") Integer coSoID);

    /**
     * Lấy các ca làm việc trong khoảng thứ (VD: Thứ 2-6)
     */
    @Query("SELECT l FROM LichLamViecMacDinh l WHERE l.thuTrongTuan BETWEEN :thuFrom AND :thuTo AND l.isDeleted = false AND l.isActive = true ORDER BY l.thuTrongTuan, l.ca")
    List<LichLamViecMacDinh> findByThuRange(@Param("thuFrom") Integer thuFrom, @Param("thuTo") Integer thuTo);

    /**
     * Lấy tất cả lịch của một ca cụ thể (VD: tất cả ca SANG)
     */
    @Query("SELECT l FROM LichLamViecMacDinh l WHERE l.ca = :ca AND l.isDeleted = false AND l.isActive = true ORDER BY l.thuTrongTuan")
    List<LichLamViecMacDinh> findByCa(@Param("ca") CaLamViec ca);

    /**
     * Tìm lịch có xung đột thời gian (overlap)
     * Dùng khi thêm/sửa lịch để validate không trùng giờ
     */
    @Query("SELECT l FROM LichLamViecMacDinh l WHERE l.coSoYTe.coSoID = :coSoID " +
            "AND l.thuTrongTuan = :thu " +
            "AND l.isDeleted = false " +
            "AND l.configID != :excludeId " +
            "AND ((l.thoiGianBatDau < :thoiGianKetThuc AND l.thoiGianKetThuc > :thoiGianBatDau))")
    List<LichLamViecMacDinh> findOverlappingSchedules(
            @Param("coSoID") Integer coSoID,
            @Param("thu") Integer thu,
            @Param("thoiGianBatDau") java.time.LocalTime thoiGianBatDau,
            @Param("thoiGianKetThuc") java.time.LocalTime thoiGianKetThuc,
            @Param("excludeId") Integer excludeId
    );
}

