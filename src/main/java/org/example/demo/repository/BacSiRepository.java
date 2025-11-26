package org.example.demo.repository;

import org.example.demo.entity.BacSi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho BacSi (Doctor)
 * Xử lý: Tìm kiếm bác sĩ, lọc theo chuyên khoa, trình độ...
 */
@Repository
public interface BacSiRepository extends JpaRepository<BacSi, Integer> {
    
    /**
     * Lấy tất cả bác sĩ CHƯA bị xóa (isDeleted = false)
     */
    Page<BacSi> findAllByIsDeleted(Boolean isDeleted, Pageable pageable);
    
    /**
     * Tìm bác sĩ theo email của user
     * Dùng khi bác sĩ login và cần lấy thông tin profile
     */
    Optional<BacSi> findByNguoiDung_Email(String email);
    
    /**
     * Lấy danh sách bác sĩ theo chuyên khoa
     * Ví dụ: Lấy tất cả bác sĩ khoa Nhi
     */
    List<BacSi> findByChuyenKhoa_ChuyenKhoaID(Integer chuyenKhoaId);
    
    /**
     * Lấy bác sĩ theo chuyên khoa và đang làm việc (CHƯA bị xóa)
     * Dùng khi user tìm bác sĩ để đặt lịch
     */
    List<BacSi> findByChuyenKhoa_ChuyenKhoaIDAndTrangThaiCongViecAndIsDeleted(
            Integer chuyenKhoaId, 
            Boolean trangThaiCongViec,
            Boolean isDeleted
    );
    
    /**
     * Lấy bác sĩ theo trình độ
     * Ví dụ: Lọc chỉ PGS.TS, Thạc sĩ...
     */
    List<BacSi> findByTrinhDo_TrinhDoID(Integer trinhDoId);
    
    /**
     * Tìm bác sĩ có giá khám trong khoảng
     * Ví dụ: Tìm bác sĩ giá từ 200k - 500k
     */
    List<BacSi> findByGiaKhamBetween(BigDecimal minPrice, BigDecimal maxPrice);
    
    /**
     * Tìm bác sĩ theo tên (search) - CHỈ lấy bác sĩ chưa bị xóa
     * Ví dụ: Search "Nguyễn" hoặc "Tim mạch"
     */
    @Query("SELECT b FROM BacSi b WHERE b.isDeleted = false AND (" +
           "LOWER(b.nguoiDung.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.chuyenKhoa.tenChuyenKhoa) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<BacSi> searchDoctors(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Lấy top bác sĩ có kinh nghiệm nhất (CHƯA bị xóa)
     */
    List<BacSi> findTop10ByTrangThaiCongViecAndIsDeletedOrderBySoNamKinhNghiemDesc(
            Boolean trangThaiCongViec, 
            Boolean isDeleted
    );
}

