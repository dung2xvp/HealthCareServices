package org.example.demo.repository;

import org.example.demo.entity.HoSoBenhAn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho HoSoBenhAn (Medical Record - Hồ sơ y tế tổng quan)
 * Xử lý: Quản lý hồ sơ y tế cơ bản của bệnh nhân
 * LƯU Ý: HoSoBenhAn là hồ sơ y tế TỔNG QUAN (nhóm máu, dị ứng, tiền sử...)
 *        KHÔNG phải lịch sử khám bệnh (lịch sử khám nằm trong LichDatKham)
 */
@Repository
public interface HoSoBenhAnRepository extends JpaRepository<HoSoBenhAn, Integer> {
    
    /**
     * Lấy hồ sơ bệnh án của bệnh nhân
     * Mỗi bệnh nhân chỉ có 1 hồ sơ duy nhất (UNIQUE constraint)
     */
    Optional<HoSoBenhAn> findByBenhNhan_NguoiDungID(Integer benhNhanId);
    
    /**
     * Kiểm tra bệnh nhân đã có hồ sơ chưa
     */
    Boolean existsByBenhNhan_NguoiDungID(Integer benhNhanId);
}

