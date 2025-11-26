package org.example.demo.repository;

import org.example.demo.entity.BacSiNgayNghi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho BacSiNgayNghi (Doctor Day Off)
 * Xử lý: Quản lý ngày nghỉ đột xuất của bác sĩ
 */
@Repository
public interface BacSiNgayNghiRepository extends JpaRepository<BacSiNgayNghi, Integer> {
    
    /**
     * Lấy tất cả ngày nghỉ của bác sĩ
     */
    List<BacSiNgayNghi> findByBacSi_BacSiID(Integer bacSiId);
    
    /**
     * Lấy ngày nghỉ theo khoảng thời gian cụ thể
     * Kiểm tra xem khoảng thời gian [ngayBatDau, ngayKetThuc] có overlap với [fromDate, toDate] không
     */
    @Query("SELECT n FROM BacSiNgayNghi n WHERE n.bacSi.bacSiID = :bacSiId " +
           "AND n.ngayBatDau <= :toDate AND n.ngayKetThuc >= :fromDate")
    List<BacSiNgayNghi> findDayOffInRange(
            @Param("bacSiId") Integer bacSiId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
    
    /**
     * Kiểm tra bác sĩ có nghỉ vào ngày cụ thể không
     * Check xem ngayCheck có nằm trong [ngayBatDau, ngayKetThuc] không
     */
    @Query("SELECT n FROM BacSiNgayNghi n WHERE n.bacSi.bacSiID = :bacSiId " +
           "AND :ngayCheck BETWEEN n.ngayBatDau AND n.ngayKetThuc")
    Optional<BacSiNgayNghi> findDayOffByDate(
            @Param("bacSiId") Integer bacSiId,
            @Param("ngayCheck") LocalDate ngayCheck
    );
    
    /**
     * Kiểm tra bác sĩ có nghỉ trong khoảng thời gian không
     */
    @Query("SELECT COUNT(n) > 0 FROM BacSiNgayNghi n WHERE " +
           "n.bacSi.bacSiID = :bacSiId " +
           "AND n.ngayBatDau <= :toDate AND n.ngayKetThuc >= :fromDate")
    Boolean existsDayOffInRange(
            @Param("bacSiId") Integer bacSiId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
    
    /**
     * Xóa ngày nghỉ đã qua (cleanup)
     * Xóa những record có ngayKetThuc < date
     */
    void deleteByNgayKetThucBefore(LocalDate date);
}

