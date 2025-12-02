package org.example.demo.repository;

import org.example.demo.entity.ThongBao;
import org.example.demo.enums.LoaiThongBao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository cho ThongBao (Notification) - Phase 2
 * 
 * Features:
 * - Email tracking (DaGuiEmail)
 * - Link to booking (DatLichID)
 * - Filter by type (LoaiThongBao)
 * - Batch email processing
 * 
 * @author Healthcare System Team
 * @version 2.0 - Phase 2
 */
@Repository
public interface ThongBaoRepository extends JpaRepository<ThongBao, Integer> {
    
    // ==========================================
    // BASIC QUERIES
    // ==========================================
    
    /**
     * Lấy tất cả thông báo của user (paginated)
     * Sắp xếp theo mới nhất
     */
    Page<ThongBao> findByNguoiNhan_NguoiDungIDOrderByThoiGianDesc(
        Integer nguoiNhanID,
        Pageable pageable
    );
    
    /**
     * Lấy thông báo chưa đọc của user
     */
    List<ThongBao> findByNguoiNhan_NguoiDungIDAndDaDocOrderByThoiGianDesc(
        Integer nguoiNhanID,
        Boolean daDoc
    );
    
    /**
     * Đếm số thông báo chưa đọc (hiển thị badge)
     */
    Long countByNguoiNhan_NguoiDungIDAndDaDoc(Integer nguoiNhanID, Boolean daDoc);
    
    // ==========================================
    // FILTER BY TYPE & BOOKING
    // ==========================================
    
    /**
     * Lấy thông báo theo loại
     */
    @Query("""
        SELECT t FROM ThongBao t 
        WHERE t.nguoiNhan.nguoiDungID = :nguoiNhanID
            AND t.loaiThongBao = :loaiThongBao
            AND t.isDeleted = false
        ORDER BY t.thoiGian DESC
        """)
    List<ThongBao> findByUserAndType(
        @Param("nguoiNhanID") Integer nguoiNhanID,
        @Param("loaiThongBao") LoaiThongBao loaiThongBao
    );
    
    /**
     * Lấy tất cả thông báo liên quan đến 1 booking
     */
    @Query("""
        SELECT t FROM ThongBao t 
        WHERE t.datLichKham.datLichID = :datLichID
            AND t.isDeleted = false
        ORDER BY t.thoiGian DESC
        """)
    List<ThongBao> findByBooking(@Param("datLichID") Integer datLichID);
    
    /**
     * Lấy thông báo của user liên quan đến 1 booking
     */
    @Query("""
        SELECT t FROM ThongBao t 
        WHERE t.nguoiNhan.nguoiDungID = :nguoiNhanID
            AND t.datLichKham.datLichID = :datLichID
            AND t.isDeleted = false
        ORDER BY t.thoiGian DESC
        """)
    List<ThongBao> findByUserAndBooking(
        @Param("nguoiNhanID") Integer nguoiNhanID,
        @Param("datLichID") Integer datLichID
    );
    
    // ==========================================
    // EMAIL TRACKING - Phase 2
    // ==========================================
    
    /**
     * Tìm thông báo chưa gửi email (for batch processing)
     * 
     * Criteria:
     * - LoaiThongBao requires email (shouldSendEmail = true)
     * - DaGuiEmail = false
     * - Created > 5 minutes ago (buffer time)
     * 
     * Use case: Cron job gửi email hàng loạt
     */
    @Query("""
        SELECT t FROM ThongBao t 
        WHERE t.daGuiEmail = false
            AND t.createdAt <= :beforeTime
            AND t.isDeleted = false
        ORDER BY t.createdAt ASC
        """)
    List<ThongBao> findPendingEmails(@Param("beforeTime") LocalDateTime beforeTime);
    
    /**
     * Đánh dấu email đã gửi
     */
    @Modifying
    @Query("""
        UPDATE ThongBao t 
        SET t.daGuiEmail = true, t.ngayGuiEmail = :now 
        WHERE t.thongBaoID = :thongBaoID
        """)
    void markEmailAsSent(
        @Param("thongBaoID") Integer thongBaoID,
        @Param("now") LocalDateTime now
    );
    
    /**
     * Batch mark emails as sent
     */
    @Modifying
    @Query("""
        UPDATE ThongBao t 
        SET t.daGuiEmail = true, t.ngayGuiEmail = :now 
        WHERE t.thongBaoID IN :thongBaoIDs
        """)
    void markEmailsAsSent(
        @Param("thongBaoIDs") List<Integer> thongBaoIDs,
        @Param("now") LocalDateTime now
    );
    
    // ==========================================
    // READ/UNREAD MANAGEMENT
    // ==========================================
    
    /**
     * Đánh dấu 1 thông báo là đã đọc
     */
    @Modifying
    @Query("""
        UPDATE ThongBao t 
        SET t.daDoc = true, t.ngayDoc = :now 
        WHERE t.thongBaoID = :thongBaoID 
            AND t.nguoiNhan.nguoiDungID = :nguoiNhanID
        """)
    void markAsRead(
        @Param("thongBaoID") Integer thongBaoID,
        @Param("nguoiNhanID") Integer nguoiNhanID,
        @Param("now") LocalDateTime now
    );
    
    /**
     * Đánh dấu tất cả thông báo của user là đã đọc
     */
    @Modifying
    @Query("""
        UPDATE ThongBao t 
        SET t.daDoc = true, t.ngayDoc = :now 
        WHERE t.nguoiNhan.nguoiDungID = :nguoiNhanID 
            AND t.daDoc = false
        """)
    void markAllAsRead(
        @Param("nguoiNhanID") Integer nguoiNhanID,
        @Param("now") LocalDateTime now
    );
    
    // ==========================================
    // STATISTICS
    // ==========================================
    
    /**
     * Đếm tổng thông báo của user
     */
    @Query("""
        SELECT COUNT(t) 
        FROM ThongBao t 
        WHERE t.nguoiNhan.nguoiDungID = :nguoiNhanID
            AND t.isDeleted = false
        """)
    Long countByUser(@Param("nguoiNhanID") Integer nguoiNhanID);
    
    /**
     * Đếm số email đã gửi
     */
    @Query("""
        SELECT COUNT(t) 
        FROM ThongBao t 
        WHERE t.daGuiEmail = true
            AND t.isDeleted = false
        """)
    Long countSentEmails();
    
    /**
     * Đếm số email pending
     */
    @Query("""
        SELECT COUNT(t) 
        FROM ThongBao t 
        WHERE t.daGuiEmail = false
            AND t.isDeleted = false
        """)
    Long countPendingEmails();
    
    // ==========================================
    // CLEANUP
    // ==========================================
    
    /**
     * Xóa thông báo cũ (cleanup - scheduled task)
     * Xóa thông báo đã đọc > 3 tháng
     */
    @Modifying
    @Query("""
        DELETE FROM ThongBao t 
        WHERE t.daDoc = true 
            AND t.thoiGian < :beforeDate
        """)
    void deleteOldReadNotifications(@Param("beforeDate") LocalDateTime beforeDate);
    
    /**
     * Soft delete thông báo cũ
     */
    @Modifying
    @Query("""
        UPDATE ThongBao t 
        SET t.isDeleted = true, t.deletedAt = :now 
        WHERE t.daDoc = true 
            AND t.thoiGian < :beforeDate
            AND t.isDeleted = false
        """)
    void softDeleteOldNotifications(
        @Param("beforeDate") LocalDateTime beforeDate,
        @Param("now") LocalDateTime now
    );
}

