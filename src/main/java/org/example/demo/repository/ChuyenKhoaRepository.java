package org.example.demo.repository;

import org.example.demo.entity.ChuyenKhoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho ChuyenKhoa (Specialty)
 * Xử lý: Quản lý chuyên khoa, tìm kiếm chuyên khoa
 */
@Repository
public interface ChuyenKhoaRepository extends JpaRepository<ChuyenKhoa, Integer> {
    
    /**
     * Lấy danh sách chuyên khoa theo cơ sở y tế
     */
    List<ChuyenKhoa> findByCoSoYTe_CoSoID(Integer coSoId);
    
    /**
     * Tìm chuyên khoa theo tên (exact match)
     */
    Optional<ChuyenKhoa> findByTenChuyenKhoa(String tenChuyenKhoa);
    
    /**
     * Tìm chuyên khoa theo tên (LIKE search)
     */
    List<ChuyenKhoa> findByTenChuyenKhoaContainingIgnoreCase(String keyword);
    
    /**
     * Lấy chuyên khoa sắp xếp theo thứ tự hiển thị
     */
    List<ChuyenKhoa> findAllByOrderByThuTuHienThiAsc();
}

