package org.example.demo.repository;

import org.example.demo.entity.BacSiLichLamViec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho BacSiLichLamViec (Doctor Work Schedule)
 * Xử lý: Quản lý lịch làm việc cố định theo thứ của bác sĩ
 */
@Repository
public interface BacSiLichLamViecRepository extends JpaRepository<BacSiLichLamViec, Integer> {
    
    /**
     * Lấy tất cả lịch làm việc của bác sĩ
     * Ví dụ: Bác sĩ làm thứ 2, 4, 6
     */
    List<BacSiLichLamViec> findByBacSi_BacSiID(Integer bacSiId);
    
    /**
     * Kiểm tra bác sĩ có làm việc vào thứ này không
     * @param bacSiId - ID bác sĩ
     * @param thuTrongTuan - Thứ trong tuần (2-8, với 8 = Chủ nhật)
     */
    Optional<BacSiLichLamViec> findByBacSi_BacSiIDAndThuTrongTuan(Integer bacSiId, Integer thuTrongTuan);
    
    /**
     * Xóa tất cả lịch của bác sĩ (dùng khi update lịch mới)
     */
    void deleteByBacSi_BacSiID(Integer bacSiId);
}

