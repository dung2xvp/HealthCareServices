package org.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.example.demo.entity.DatLichKham;
import org.example.demo.entity.NguoiDung;
import org.example.demo.entity.ThongBao;
import org.example.demo.enums.LoaiThongBao;
import org.example.demo.exception.ResourceNotFoundException;
import org.example.demo.repository.DatLichKhamRepository;
import org.example.demo.repository.NguoiDungRepository;
import org.example.demo.repository.ThongBaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * NotificationService - Quản lý thông báo và email
 * 
 * Chức năng:
 * 1. Tạo thông báo in-app
 * 2. Gửi email thông báo
 * 3. Quản lý trạng thái thông báo (đọc/chưa đọc)
 * 4. Gửi reminder trước giờ hẹn
 * 5. Cleanup thông báo cũ
 */
@Slf4j
@Service
public class NotificationService {

    @Autowired
    private ThongBaoRepository thongBaoRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private DatLichKhamRepository datLichKhamRepository;

    @Autowired
    private EmailService emailService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // ========================================
    // CORE NOTIFICATION METHODS
    // ========================================

    /**
     * Gửi thông báo khi bệnh nhân đặt lịch thành công
     */
    @Transactional
    public void sendBookingConfirmation(Integer datLichID) {
        DatLichKham booking = getBooking(datLichID);
        
        // 1. Thông báo cho BỆNH NHÂN
        ThongBao patientNotif = createNotification(
            booking.getBenhNhan(),
            LoaiThongBao.DAT_LICH_MOI,
            "Đặt lịch khám thành công",
            String.format(
                "Bạn đã đặt lịch khám với %s vào lúc %s ngày %s. " +
                "Vui lòng chờ bác sĩ xác nhận.",
                booking.getBacSi().getNguoiDung().getHoTen(),
                booking.getGioKham().format(TIME_FORMATTER),
                booking.getNgayKham().format(DATE_FORMATTER)
            ),
            booking,
            "/bookings/" + datLichID
        );
        thongBaoRepository.save(patientNotif);

        // 2. Thông báo cho BÁC SĨ
        ThongBao doctorNotif = createNotification(
            booking.getBacSi().getNguoiDung(),
            LoaiThongBao.DAT_LICH_MOI,
            "Yêu cầu xác nhận lịch khám mới",
            String.format(
                "Bệnh nhân %s đã đặt lịch khám vào lúc %s ngày %s. " +
                "Vui lòng xác nhận hoặc từ chối lịch hẹn này.",
                booking.getBenhNhan().getHoTen(),
                booking.getGioKham().format(TIME_FORMATTER),
                booking.getNgayKham().format(DATE_FORMATTER)
            ),
            booking,
            "/doctor/appointments/" + datLichID
        );
        thongBaoRepository.save(doctorNotif);

        // 3. Gửi email cho bệnh nhân
        sendEmailAsync(patientNotif);

        // 4. Gửi email cho bác sĩ
        sendEmailAsync(doctorNotif);

        log.info("✅ Sent booking confirmation notifications for booking #{}", datLichID);
    }

    /**
     * Gửi thông báo khi bác sĩ XÁC NHẬN lịch hẹn
     */
    @Transactional
    public void sendDoctorConfirmation(Integer datLichID) {
        DatLichKham booking = getBooking(datLichID);
        
        ThongBao notification = createNotification(
            booking.getBenhNhan(),
            LoaiThongBao.BAC_SI_XAC_NHAN,
            "Bác sĩ đã xác nhận lịch hẹn",
            String.format(
                "%s đã xác nhận lịch khám của bạn vào lúc %s ngày %s. " +
                "Vui lòng đến đúng giờ. Mã xác nhận: %s",
                booking.getBacSi().getNguoiDung().getHoTen(),
                booking.getGioKham().format(TIME_FORMATTER),
                booking.getNgayKham().format(DATE_FORMATTER),
                booking.getMaXacNhan()
            ),
            booking,
            "/bookings/" + datLichID
        );
        thongBaoRepository.save(notification);

        // Gửi email
        sendEmailAsync(notification);

        log.info("✅ Sent doctor confirmation notification for booking #{}", datLichID);
    }

    /**
     * Gửi thông báo khi bác sĩ TỪ CHỐI lịch hẹn
     */
    @Transactional
    public void sendDoctorRejection(Integer datLichID, String reason) {
        DatLichKham booking = getBooking(datLichID);
        
        String content = String.format(
            "%s đã từ chối lịch hẹn của bạn vào lúc %s ngày %s.",
            booking.getBacSi().getNguoiDung().getHoTen(),
            booking.getGioKham().format(TIME_FORMATTER),
            booking.getNgayKham().format(DATE_FORMATTER)
        );
        
        if (reason != null && !reason.isBlank()) {
            content += "\nLý do: " + reason;
        }

        ThongBao notification = createNotification(
            booking.getBenhNhan(),
            LoaiThongBao.BAC_SI_TU_CHOI,
            "Lịch hẹn bị từ chối",
            content,
            booking,
            "/bookings/" + datLichID
        );
        thongBaoRepository.save(notification);

        // Gửi email
        sendEmailAsync(notification);

        log.info("✅ Sent doctor rejection notification for booking #{}", datLichID);
    }

    /**
     * Gửi thông báo khi lịch hẹn bị HỦY
     */
    @Transactional
    public void sendCancellationNotification(Integer datLichID, Integer cancelledByUserId, String reason) {
        DatLichKham booking = getBooking(datLichID);
        NguoiDung cancelledBy = nguoiDungRepository.findById(cancelledByUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        // Xác định người nhận thông báo (người còn lại)
        NguoiDung recipient = booking.getBenhNhan().getNguoiDungID().equals(cancelledByUserId)
            ? booking.getBacSi().getNguoiDung()
            : booking.getBenhNhan();

        String title = booking.getBenhNhan().getNguoiDungID().equals(cancelledByUserId)
            ? "Bệnh nhân đã hủy lịch hẹn"
            : "Bác sĩ đã hủy lịch hẹn";

        String content = String.format(
            "%s đã hủy lịch khám vào lúc %s ngày %s.",
            cancelledBy.getHoTen(),
            booking.getGioKham().format(TIME_FORMATTER),
            booking.getNgayKham().format(DATE_FORMATTER)
        );

        if (reason != null && !reason.isBlank()) {
            content += "\nLý do: " + reason;
        }

        ThongBao notification = createNotification(
            recipient,
            LoaiThongBao.HUY_LICH,
            title,
            content,
            booking,
            "/bookings/" + datLichID
        );
        thongBaoRepository.save(notification);

        // Gửi email
        sendEmailAsync(notification);

        log.info("✅ Sent cancellation notification for booking #{}", datLichID);
    }

    /**
     * Gửi nhắc nhở trước giờ hẹn (24h hoặc 1h)
     */
    @Transactional
    public void sendReminder(Integer datLichID, int hoursBefore) {
        DatLichKham booking = getBooking(datLichID);
        
        String timeText = hoursBefore == 24 ? "ngày mai" : "1 giờ nữa";
        
        // 1. Nhắc nhở cho BỆNH NHÂN
        ThongBao patientNotif = createNotification(
            booking.getBenhNhan(),
            LoaiThongBao.NHAC_LICH_KHAM,
            "Nhắc nhở lịch khám",
            String.format(
                "Bạn có lịch khám với %s vào lúc %s %s. " +
                "Vui lòng đến đúng giờ. Mã xác nhận: %s",
                booking.getBacSi().getNguoiDung().getHoTen(),
                booking.getGioKham().format(TIME_FORMATTER),
                timeText,
                booking.getMaXacNhan()
            ),
            booking,
            "/bookings/" + datLichID
        );
        thongBaoRepository.save(patientNotif);

        // 2. Nhắc nhở cho BÁC SĨ
        ThongBao doctorNotif = createNotification(
            booking.getBacSi().getNguoiDung(),
            LoaiThongBao.NHAC_LICH_KHAM,
            "Nhắc nhở lịch khám",
            String.format(
                "Bạn có lịch khám bệnh nhân %s vào lúc %s %s. " +
                "Mã xác nhận: %s",
                booking.getBenhNhan().getHoTen(),
                booking.getGioKham().format(TIME_FORMATTER),
                timeText,
                booking.getMaXacNhan()
            ),
            booking,
            "/doctor/appointments/" + datLichID
        );
        thongBaoRepository.save(doctorNotif);

        // 3. Gửi email
        sendEmailAsync(patientNotif);
        sendEmailAsync(doctorNotif);

        log.info("✅ Sent {}h reminder for booking #{}", hoursBefore, datLichID);
    }

    /**
     * Gửi thông báo khi hoàn thành khám
     */
    @Transactional
    public void sendCompletionNotification(Integer datLichID) {
        DatLichKham booking = getBooking(datLichID);
        
        ThongBao notification = createNotification(
            booking.getBenhNhan(),
            LoaiThongBao.LICH_KHAM_HON_THANH,
            "Đã hoàn thành khám bệnh",
            String.format(
                "Bạn đã hoàn thành buổi khám với %s. " +
                "Vui lòng đánh giá trải nghiệm của bạn để cải thiện chất lượng dịch vụ.",
                booking.getBacSi().getNguoiDung().getHoTen()
            ),
            booking,
            "/bookings/" + datLichID
        );
        thongBaoRepository.save(notification);

        // Gửi email
        sendEmailAsync(notification);

        log.info("✅ Sent completion notification for booking #{}", datLichID);
    }

    /**
     * Gửi thông báo thanh toán thành công
     */
    @Transactional
    public void sendPaymentSuccessNotification(Integer datLichID) {
        DatLichKham booking = getBooking(datLichID);
        
        ThongBao notification = createNotification(
            booking.getBenhNhan(),
            LoaiThongBao.THANH_TOAN_THANH_CONG,
            "Thanh toán thành công",
            String.format(
                "Bạn đã thanh toán thành công cho lịch khám với %s. " +
                "Mã giao dịch: %s",
                booking.getBacSi().getNguoiDung().getHoTen(),
                booking.getMaGiaoDich()
            ),
            booking,
            "/bookings/" + datLichID
        );
        thongBaoRepository.save(notification);

        // Gửi email
        sendEmailAsync(notification);

        log.info("✅ Sent payment success notification for booking #{}", datLichID);
    }

    /**
     * Gửi thông báo thanh toán thất bại
     */
    @Transactional
    public void sendPaymentFailedNotification(Integer datLichID) {
        DatLichKham booking = getBooking(datLichID);
        
        ThongBao notification = createNotification(
            booking.getBenhNhan(),
            LoaiThongBao.THANH_TOAN_THAT_BAI,
            "Thanh toán thất bại",
            String.format(
                "Thanh toán cho lịch khám với %s không thành công. " +
                "Vui lòng thử lại hoặc liên hệ với chúng tôi để được hỗ trợ.",
                booking.getBacSi().getNguoiDung().getHoTen()
            ),
            booking,
            "/bookings/" + datLichID
        );
        thongBaoRepository.save(notification);

        // Gửi email
        sendEmailAsync(notification);

        log.info("✅ Sent payment failed notification for booking #{}", datLichID);
    }

    // ========================================
    // QUERY METHODS
    // ========================================

    /**
     * Lấy tất cả thông báo của user (có phân trang)
     */
    @Transactional(readOnly = true)
    public Page<ThongBao> getNotifications(Integer nguoiDungID, Pageable pageable) {
        return thongBaoRepository.findByNguoiNhan_NguoiDungIDOrderByThoiGianDesc(nguoiDungID, pageable);
    }

    /**
     * Lấy thông báo chưa đọc của user
     */
    @Transactional(readOnly = true)
    public List<ThongBao> getUnreadNotifications(Integer nguoiDungID) {
        return thongBaoRepository.findByNguoiNhan_NguoiDungIDAndDaDocOrderByThoiGianDesc(nguoiDungID, false);
    }

    /**
     * Đếm số thông báo chưa đọc
     */
    @Transactional(readOnly = true)
    public long countUnread(Integer nguoiDungID) {
        return thongBaoRepository.countByNguoiNhan_NguoiDungIDAndDaDoc(nguoiDungID, false);
    }

    /**
     * Đánh dấu 1 thông báo đã đọc
     */
    @Transactional
    public void markAsRead(Integer thongBaoID, Integer nguoiDungID) {
        ThongBao notification = thongBaoRepository.findById(thongBaoID)
            .orElseThrow(() -> new ResourceNotFoundException("Thông báo không tồn tại"));

        // Kiểm tra quyền sở hữu
        if (!notification.getNguoiNhan().getNguoiDungID().equals(nguoiDungID)) {
            throw new ResourceNotFoundException("Thông báo không tồn tại");
        }

        notification.markAsRead();
        thongBaoRepository.save(notification);

        log.info("✅ Marked notification #{} as read", thongBaoID);
    }

    /**
     * Đánh dấu tất cả thông báo đã đọc
     */
    @Transactional
    public void markAllAsRead(Integer nguoiDungID) {
        thongBaoRepository.markAllAsRead(nguoiDungID, LocalDateTime.now());
        log.info("✅ Marked all notifications as read for user #{}", nguoiDungID);
    }

    /**
     * Xóa thông báo
     */
    @Transactional
    public void deleteNotification(Integer thongBaoID, Integer nguoiDungID) {
        ThongBao notification = thongBaoRepository.findById(thongBaoID)
            .orElseThrow(() -> new ResourceNotFoundException("Thông báo không tồn tại"));

        // Kiểm tra quyền sở hữu
        if (!notification.getNguoiNhan().getNguoiDungID().equals(nguoiDungID)) {
            throw new ResourceNotFoundException("Thông báo không tồn tại");
        }

        notification.setIsDeleted(true);
        thongBaoRepository.save(notification);

        log.info("✅ Deleted notification #{}", thongBaoID);
    }

    /**
     * Lấy thông báo liên quan đến 1 booking
     */
    @Transactional(readOnly = true)
    public List<ThongBao> getNotificationsByBooking(Integer datLichID) {
        return thongBaoRepository.findByBooking(datLichID);
    }

    // ========================================
    // SCHEDULED TASKS
    // ========================================

    /**
     * Gửi reminder cho các booking sắp tới (24h trước)
     * Chạy mỗi giờ
     * 
     * TODO: Implement after adding reminder tracking fields to DatLichKham entity:
     * - daGuiNhacNho24h (Boolean)
     * - daGuiNhacNho1h (Boolean)
     */
    @Transactional
    public void send24HourReminders() {
        LocalDate tomorrowDate = LocalDate.now().plusDays(1);
        
        List<DatLichKham> bookings = datLichKhamRepository
            .findBookingsNeedingReminder(tomorrowDate);

        int count = 0;
        for (DatLichKham booking : bookings) {
            try {
                sendReminder(booking.getDatLichID(), 24);
                count++;
            } catch (Exception e) {
                log.error("Failed to send 24h reminder for booking #{}: {}", 
                    booking.getDatLichID(), e.getMessage());
            }
        }

        log.info("✅ Sent 24h reminders for {}/{} bookings", count, bookings.size());
    }

    /**
     * Gửi reminder cho các booking sắp tới (1h trước)
     * Chạy mỗi 15 phút
     * 
     * TODO: Implement after adding reminder tracking fields to DatLichKham entity
     */
    @Transactional
    public void send1HourReminders() {
        // For now, just log - implement when reminder tracking is added
        log.info("⏭️ 1h reminder task (pending reminder tracking implementation)");
        
        // Future implementation:
        // - Find bookings in next 1 hour
        // - Check daGuiNhacNho1h flag
        // - Send reminders
    }

    /**
     * Xóa thông báo cũ hơn 90 ngày
     * Chạy hàng ngày lúc 2:00 AM
     */
    @Transactional
    public void cleanupOldNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        thongBaoRepository.deleteOldReadNotifications(cutoffDate);
        
        log.info("✅ Cleaned up old notifications (> 90 days)");
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    /**
     * Tạo ThongBao entity
     */
    private ThongBao createNotification(
        NguoiDung recipient,
        LoaiThongBao type,
        String title,
        String content,
        DatLichKham booking,
        String link
    ) {
        ThongBao notification = new ThongBao();
        notification.setNguoiNhan(recipient);
        notification.setLoaiThongBao(type);
        notification.setTieuDe(title);
        notification.setNoiDung(content);
        notification.setThoiGian(LocalDateTime.now());
        notification.setDaDoc(false);
        notification.setDatLichKham(booking);
        notification.setLinkDinhKem(link);
        notification.setDaGuiEmail(false);
        
        return notification;
    }

    /**
     * Gửi email async (không block main thread)
     */
    private void sendEmailAsync(ThongBao notification) {
        try {
            // TODO: Implement proper async with @Async in future
            String toEmail = notification.getNguoiNhan().getEmail();
            String hoTen = notification.getNguoiNhan().getHoTen();
            String subject = notification.getTieuDe();
            String body = notification.getNoiDung();

            // Gửi email đơn giản
            emailService.sendNotificationEmail(toEmail, hoTen, subject, body);

            // Đánh dấu đã gửi email
            notification.markEmailSent();
            thongBaoRepository.save(notification);

        } catch (Exception e) {
            log.error("❌ Failed to send email for notification #{}: {}", 
                notification.getThongBaoID(), e.getMessage());
            // Không throw exception để không ảnh hưởng đến flow chính
        }
    }

    /**
     * Lấy booking entity
     */
    private DatLichKham getBooking(Integer datLichID) {
        return datLichKhamRepository.findById(datLichID)
            .orElseThrow(() -> new ResourceNotFoundException("Lịch khám không tồn tại"));
    }
}
