package org.example.demo.repository;

import org.example.demo.entity.ThongBao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho ThongBao (Notification)
 * Xử lý: Quản lý thông báo cho user
 */
@Repository
public interface ThongBaoRepository extends JpaRepository<ThongBao, Integer> {
    
    /**
     * Lấy tất cả thông báo của user
     * Sắp xếp theo mới nhất, hỗ trợ phân trang
     */
    Page<ThongBao> findByNguoiDung_NguoiDungIDOrderByCreatedAtDesc(
            Integer nguoiDungId,
            Pageable pageable
    );
    
    /**
     * Lấy thông báo chưa đọc của user
     */
    List<ThongBao> findByNguoiDung_NguoiDungIDAndDaDocOrderByCreatedAtDesc(
            Integer nguoiDungId,
            Boolean daDoc
    );
    
    /**
     * Đếm số thông báo chưa đọc (hiển thị badge)
     */
    Long countByNguoiDung_NguoiDungIDAndDaDoc(Integer nguoiDungId, Boolean daDoc);
    
    /**
     * Đánh dấu tất cả thông báo của user là đã đọc
     */
    @Modifying
    @Query("UPDATE ThongBao t SET t.daDoc = true WHERE t.nguoiDung.nguoiDungID = :nguoiDungId")
    void markAllAsRead(@Param("nguoiDungId") Integer nguoiDungId);
    
    /**
     * Xóa thông báo cũ (cleanup - chạy scheduled task)
     * Ví dụ: Xóa thông báo > 3 tháng
     */
    void deleteByCreatedAtBefore(java.time.LocalDateTime date);
}

