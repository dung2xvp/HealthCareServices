package org.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.example.demo.dto.request.*;
import org.example.demo.dto.response.BookingResponse;
import org.example.demo.entity.*;
import org.example.demo.enums.*;
import org.example.demo.exception.*;
import org.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * BookingService - Simplified Version
 * 
 * Core Features:
 * 1. Create booking
 * 2. Doctor confirm/reject
 * 3. Cancel booking
 * 4. Check-in patient
 * 5. Complete appointment
 * 6. Rate booking
 * 7. Query bookings
 * 
 * Note: Advanced features (slot search, reschedule) will be in Phase 3
 */
@Slf4j
@Service
public class BookingService {

    @Autowired
    private DatLichKhamRepository datLichKhamRepository;

    @Autowired
    private BacSiRepository bacSiRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private CoSoYTeRepository coSoYTeRepository;

    @Autowired
    private NotificationService notificationService;

    private static final int MAX_BOOKING_DAYS_AHEAD = 30;
    private static final int CANCELLATION_HOURS_BEFORE = 24;

    // ========================================
    // BOOKING CREATION
    // ========================================

    /**
     * Tạo booking mới
     */
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, Integer currentUserId) {
        log.info("📝 Creating new booking for user: {}", currentUserId);

        // 1. Validate
        request.validate();
        validateBookingDate(request.getNgayKham());

        // 2. Load entities
        NguoiDung patient = nguoiDungRepository.findById(currentUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        BacSi doctor = bacSiRepository.findById(request.getBacSiID())
            .orElseThrow(() -> new ResourceNotFoundException("Bác sĩ không tồn tại"));

        if (!Boolean.TRUE.equals(doctor.getTrangThaiCongViec())) {
            throw new BadRequestException("Bác sĩ hiện không nhận khám");
        }

        // 3. Check duplicate slot (simplified check)
        boolean slotOccupied = datLichKhamRepository.existsByBacSi_BacSiIDAndNgayKhamAndCaAndGioKham(
            request.getBacSiID(),
            request.getNgayKham(),
            request.getCa(),
            request.getGioKham()
        );

        if (slotOccupied) {
            throw new ConflictException("Khung giờ này đã có người đặt");
        }

        // 4. Check patient conflict
        boolean patientConflict = datLichKhamRepository.existsByBenhNhan_NguoiDungIDAndNgayKhamAndCaAndGioKham(
            currentUserId,
            request.getNgayKham(),
            request.getCa(),
            request.getGioKham()
        );

        if (patientConflict) {
            throw new ConflictException("Bạn đã có lịch hẹn khác vào cùng thời gian này");
        }

        // 5. Tính giá
        BigDecimal giaKham = calculateBookingPrice(doctor);

        // 6. Get clinic (simplified: use first available clinic)
        CoSoYTe clinic = coSoYTeRepository.findAll().stream()
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cơ sở y tế"));

        // 7. Tạo booking
        DatLichKham booking = new DatLichKham();
        booking.setBenhNhan(patient);
        booking.setBacSi(doctor);
        booking.setCoSoYTe(clinic);
        booking.setNgayKham(request.getNgayKham());
        booking.setCa(request.getCa());
        booking.setGioKham(request.getGioKham());
        booking.setLyDoKham(request.getLyDoKham());
        booking.setGhiChu(request.getGhiChu());
        booking.setGiaKham(giaKham);
        booking.setMaXacNhan(generateConfirmationCode());
        booking.setPhuongThucThanhToan(request.getPhuongThucThanhToan());
        booking.setNgayDat(LocalDateTime.now());

        // Set status
        if (request.getPhuongThucThanhToan() == PhuongThucThanhToan.TIEN_MAT) {
            booking.setTrangThai(TrangThaiDatLich.CHO_XAC_NHAN_BAC_SI);
            booking.setTrangThaiThanhToan(TrangThaiThanhToan.CHUA_THANH_TOAN);
        } else {
            booking.setTrangThai(TrangThaiDatLich.CHO_THANH_TOAN);
            booking.setTrangThaiThanhToan(TrangThaiThanhToan.CHUA_THANH_TOAN);
        }

        booking = datLichKhamRepository.save(booking);
        log.info("✅ Created booking #{}", booking.getDatLichID());

        // Send notification (only for cash payment)
        if (request.getPhuongThucThanhToan() == PhuongThucThanhToan.TIEN_MAT) {
            try {
                notificationService.sendBookingConfirmation(booking.getDatLichID());
            } catch (Exception e) {
                log.error("❌ Failed to send notification: {}", e.getMessage());
            }
        }

        return BookingResponse.of(booking);
    }

    /**
     * Validate ngày khám
     */
    private void validateBookingDate(LocalDate ngayKham) {
        LocalDate today = LocalDate.now();

        if (ngayKham.isBefore(today)) {
            throw new BadRequestException("Không thể đặt lịch cho ngày trong quá khứ");
        }

        if (ngayKham.isAfter(today.plusDays(MAX_BOOKING_DAYS_AHEAD))) {
            throw new BadRequestException(
                String.format("Chỉ có thể đặt lịch trước tối đa %d ngày", MAX_BOOKING_DAYS_AHEAD)
            );
        }
    }

    /**
     * Tính giá khám
     */
    private BigDecimal calculateBookingPrice(BacSi doctor) {
        BigDecimal basePrice = doctor.getGiaKham();
        String trinhDo = doctor.getTrinhDo().getTenTrinhDo();

        BigDecimal multiplier = BigDecimal.ONE;
        
        if (trinhDo.contains("Tiến sĩ") || trinhDo.contains("TS")) {
            multiplier = new BigDecimal("1.5");
        } else if (trinhDo.contains("Thạc sĩ") || trinhDo.contains("ThS")) {
            multiplier = new BigDecimal("1.3");
        } else if (trinhDo.contains("Chuyên khoa 2") || trinhDo.contains("CK2")) {
            multiplier = new BigDecimal("1.4");
        } else if (trinhDo.contains("Chuyên khoa 1") || trinhDo.contains("CK1")) {
            multiplier = new BigDecimal("1.2");
        }

        return basePrice.multiply(multiplier).setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Tạo mã xác nhận
     */
    private String generateConfirmationCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "BK" + timestamp + random;
    }

    // ========================================
    // STATUS TRANSITIONS
    // ========================================

    /**
     * Bác sĩ xác nhận/từ chối
     */
    @Transactional
    public BookingResponse doctorConfirmBooking(DoctorConfirmBookingRequest request, Integer currentDoctorId) {
        log.info("✅ Doctor {} processing booking #{}", currentDoctorId, request.getDatLichID());

        request.validate();
        DatLichKham booking = getBookingById(request.getDatLichID());

        // Verify ownership
        if (!booking.getBacSi().getBacSiID().equals(currentDoctorId)) {
            throw new UnauthorizedException("Bạn không có quyền xử lý lịch hẹn này");
        }

        // Validate status
        if (booking.getTrangThai() != TrangThaiDatLich.CHO_XAC_NHAN_BAC_SI) {
            throw new BadRequestException("Chỉ có thể xử lý lịch hẹn ở trạng thái CHỜ XÁC NHẬN");
        }

        if (request.isApprove()) {
            booking.setTrangThai(TrangThaiDatLich.DA_XAC_NHAN);
            booking.setNgayBacSiXacNhan(LocalDateTime.now());
            booking = datLichKhamRepository.save(booking);

            try {
                notificationService.sendDoctorConfirmation(booking.getDatLichID());
            } catch (Exception e) {
                log.error("❌ Failed to send notification: {}", e.getMessage());
            }

            log.info("✅ Booking #{} confirmed", booking.getDatLichID());
        } else {
            booking.setTrangThai(TrangThaiDatLich.TU_CHOI);
            booking.setLyDoTuChoi(request.getLyDoTuChoi());
            datLichKhamRepository.save(booking);

            try {
                notificationService.sendDoctorRejection(
                    booking.getDatLichID(),
                    request.getLyDoTuChoi()
                );
            } catch (Exception e) {
                log.error("❌ Failed to send notification: {}", e.getMessage());
            }

            log.info("✅ Booking #{} rejected", booking.getDatLichID());
        }

        return BookingResponse.of(booking);
    }

    /**
     * Hủy booking
     */
    @Transactional
    public void cancelBooking(CancelBookingRequest request, Integer currentUserId) {
        log.info("🚫 User {} cancelling booking #{}", currentUserId, request.getDatLichID());

        DatLichKham booking = getBookingById(request.getDatLichID());

        // Verify permission
        boolean isPatient = booking.getBenhNhan().getNguoiDungID().equals(currentUserId);
        boolean isDoctor = booking.getBacSi().getBacSiID().equals(currentUserId);

        if (!isPatient && !isDoctor) {
            throw new UnauthorizedException("Bạn không có quyền hủy lịch hẹn này");
        }

        // Validate status
        if (!booking.canCancel()) {
            throw new BadRequestException("Không thể hủy lịch hẹn ở trạng thái này");
        }

        // Check time constraint for patient
        if (isPatient) {
            LocalDateTime appointmentTime = LocalDateTime.of(booking.getNgayKham(), booking.getGioKham());
            long hoursUntilAppointment = java.time.Duration.between(LocalDateTime.now(), appointmentTime).toHours();

            if (hoursUntilAppointment < CANCELLATION_HOURS_BEFORE) {
                throw new BadRequestException(
                    String.format("Bạn phải hủy lịch trước ít nhất %d giờ", CANCELLATION_HOURS_BEFORE)
                );
            }
        }

        // Update booking
        NguoiDung cancelledBy = nguoiDungRepository.findById(currentUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        
        booking.setTrangThai(isPatient ? TrangThaiDatLich.HUY_BOI_BENH_NHAN : TrangThaiDatLich.HUY_BOI_BAC_SI);
        booking.setNguoiHuy(cancelledBy);
        booking.setLyDoHuy(request.getLyDoHuy());
        booking.setNgayHuy(LocalDateTime.now());

        datLichKhamRepository.save(booking);

        // Send notification
        try {
            notificationService.sendCancellationNotification(
                booking.getDatLichID(),
                currentUserId,
                request.getLyDoHuy()
            );
        } catch (Exception e) {
            log.error("❌ Failed to send notification: {}", e.getMessage());
        }

        log.info("✅ Booking #{} cancelled", booking.getDatLichID());
    }

    /**
     * Check-in bệnh nhân
     */
    @Transactional
    public BookingResponse checkInPatient(CheckInBookingRequest request) {
        log.info("👤 Checking in patient for booking #{}", request.getDatLichID());

        DatLichKham booking = getBookingById(request.getDatLichID());

        if (booking.getTrangThai() != TrangThaiDatLich.DA_XAC_NHAN) {
            throw new BadRequestException("Chỉ có thể check-in lịch hẹn đã được xác nhận");
        }

        if (!booking.getNgayKham().equals(LocalDate.now())) {
            throw new BadRequestException("Chỉ có thể check-in vào đúng ngày hẹn");
        }

        booking.setTrangThai(TrangThaiDatLich.DANG_KHAM);
        booking.setNgayCheckIn(LocalDateTime.now());

        booking = datLichKhamRepository.save(booking);

        log.info("✅ Patient checked in for booking #{}", booking.getDatLichID());
        return BookingResponse.of(booking);
    }

    /**
     * Hoàn thành khám bệnh
     */
    @Transactional
    public BookingResponse completeAppointment(CompleteAppointmentRequest request, Integer currentDoctorId) {
        log.info("✅ Completing appointment #{}", request.getDatLichID());

        DatLichKham booking = getBookingById(request.getDatLichID());

        // Verify ownership
        if (!booking.getBacSi().getBacSiID().equals(currentDoctorId)) {
            throw new UnauthorizedException("Bạn không có quyền hoàn thành lịch hẹn này");
        }

        if (booking.getTrangThai() != TrangThaiDatLich.DANG_KHAM) {
            throw new BadRequestException("Chỉ có thể hoàn thành lịch hẹn đang khám");
        }

        // Update booking
        booking.setTrangThai(TrangThaiDatLich.HOAN_THANH);
        booking.setKetQuaKham(request.getKetQuaKham());
        booking.setDonThuoc(request.getDonThuoc());
        booking.setChanDoan(request.getChanDoan());
        booking.setLoiDanBacSi(request.getLoiDanBacSi());
        booking.setNgayTaiKham(request.getNgayTaiKham());
        booking.setNgayHoanThanh(LocalDateTime.now());

        // Handle cash payment
        if (booking.getPhuongThucThanhToan() == PhuongThucThanhToan.TIEN_MAT &&
            booking.getTrangThaiThanhToan() == TrangThaiThanhToan.CHUA_THANH_TOAN) {
            booking.setTrangThaiThanhToan(TrangThaiThanhToan.THANH_CONG);
            booking.setNgayThanhToan(LocalDateTime.now());
            booking.setMaGiaoDich("CASH_" + System.currentTimeMillis());
        }

        booking = datLichKhamRepository.save(booking);

        // Send notification
        try {
            notificationService.sendCompletionNotification(booking.getDatLichID());
        } catch (Exception e) {
            log.error("❌ Failed to send notification: {}", e.getMessage());
        }

        log.info("✅ Appointment #{} completed", booking.getDatLichID());
        return BookingResponse.of(booking);
    }

    // ========================================
    // QUERY METHODS
    // ========================================

    /**
     * Lấy chi tiết booking
     */
    @Transactional(readOnly = true)
    public BookingResponse getBookingDetails(Integer datLichID, Integer currentUserId) {
        DatLichKham booking = getBookingById(datLichID);

        // Verify permission
        boolean isPatient = booking.getBenhNhan().getNguoiDungID().equals(currentUserId);
        boolean isDoctor = booking.getBacSi().getBacSiID().equals(currentUserId);

        if (!isPatient && !isDoctor) {
            throw new UnauthorizedException("Bạn không có quyền xem lịch hẹn này");
        }

        return BookingResponse.of(booking);
    }

    /**
     * Lấy bookings của bệnh nhân
     */
    @Transactional(readOnly = true)
    public Page<BookingResponse> getMyBookings(Integer benhNhanID, Pageable pageable) {
        Page<DatLichKham> bookings = datLichKhamRepository
            .findByBenhNhan_NguoiDungIDOrderByNgayDatDesc(benhNhanID, pageable);

        return bookings.map(BookingResponse::of);
    }

    /**
     * Lấy lịch hẹn của bác sĩ
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getDoctorAppointments(Integer bacSiID, LocalDate ngayKham) {
        List<DatLichKham> bookings = datLichKhamRepository
            .findByBacSi_BacSiIDAndNgayKhamOrderByGioKhamAsc(bacSiID, ngayKham);

        return bookings.stream()
            .map(BookingResponse::of)
            .toList();
    }

    // ========================================
    // ADDITIONAL FEATURES
    // ========================================

    /**
     * Đánh giá sau khám
     */
    @Transactional
    public BookingResponse rateBooking(RateBookingRequest request, Integer currentUserId) {
        log.info("⭐ Rating booking #{}", request.getDatLichID());

        DatLichKham booking = getBookingById(request.getDatLichID());

        if (!booking.getBenhNhan().getNguoiDungID().equals(currentUserId)) {
            throw new UnauthorizedException("Chỉ bệnh nhân mới có thể đánh giá");
        }

        if (booking.getTrangThai() != TrangThaiDatLich.HOAN_THANH) {
            throw new BadRequestException("Chỉ có thể đánh giá sau khi hoàn thành khám");
        }

        if (booking.getSoSao() != null) {
            throw new BadRequestException("Bạn đã đánh giá lịch hẹn này rồi");
        }

        booking.setSoSao(request.getSoSao());
        booking.setNhanXet(request.getNhanXet());
        booking.setNgayDanhGia(LocalDateTime.now());

        booking = datLichKhamRepository.save(booking);

        log.info("✅ Booking #{} rated with {} stars", booking.getDatLichID(), request.getSoSao());
        return BookingResponse.of(booking);
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    private DatLichKham getBookingById(Integer datLichID) {
        return datLichKhamRepository.findById(datLichID)
            .orElseThrow(() -> new ResourceNotFoundException("Lịch hẹn không tồn tại"));
    }
}
