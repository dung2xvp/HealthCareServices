package org.example.demo.repository;

import org.example.demo.entity.CoSoYTe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho CoSoYTe (Medical Facility)
 * Xử lý: Quản lý cơ sở y tế, bệnh viện, phòng khám
 */
@Repository
public interface CoSoYTeRepository extends JpaRepository<CoSoYTe, Integer> {
    
    /**
     * Tìm cơ sở y tế theo tên
     */
    Optional<CoSoYTe> findByTenCoSo(String tenCoSo);
    
    /**
     * Tìm kiếm cơ sở y tế theo tên (LIKE search)
     */
    List<CoSoYTe> findByTenCoSoContainingIgnoreCase(String keyword);
    
    /**
     * Tìm cơ sở y tế theo địa chỉ
     */
    List<CoSoYTe> findByDiaChiContainingIgnoreCase(String diaChi);
}

