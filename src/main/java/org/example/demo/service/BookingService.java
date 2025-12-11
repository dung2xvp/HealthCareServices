package org.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.example.demo.dto.request.*;
import org.example.demo.dto.response.BookingResponse;
import org.example.demo.dto.response.DoctorScheduleItemResponse;
import org.example.demo.dto.response.AvailableSlotsResponse;
import org.example.demo.dto.response.TimeSlotResponse;
import org.example.demo.dto.response.BookingStatisticsResponse;
import org.example.demo.dto.response.SpecialtyRevenueResponse;
import org.example.demo.dto.response.DoctorRevenueResponse;
import org.example.demo.service.MedicalRecordService;
import org.example.demo.entity.*;
import org.example.demo.enums.*;
import org.example.demo.exception.*;
import org.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

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
    private MedicalRecordService medicalRecordService;

    @Autowired
    private CoSoYTeRepository coSoYTeRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private BacSiNgayNghiRepository bacSiNgayNghiRepository;

    @Autowired
    private LichLamViecMacDinhRepository lichLamViecMacDinhRepository;

    private static final int MAX_BOOKING_DAYS_AHEAD = 30;
    private static final int CANCELLATION_HOURS_BEFORE = 24;
    private static final int MAX_SCHEDULE_RANGE_DAYS = 31;
    // Bước slot mặc định (phút). Có thể đổi thành cấu hình sau.
    private static final int DEFAULT_SLOT_MINUTES = 30;
    private static final String FRIENDLY_ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int CONFIRM_CODE_RANDOM_LEN = 6; // Tổng độ dài mã = 2(prefix) + 6 = 8
    private static final SecureRandom RANDOM = new SecureRandom();

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

        List<TrangThaiDatLich> activeStatuses = Arrays.asList(
            TrangThaiDatLich.CHO_XAC_NHAN_BAC_SI,
            TrangThaiDatLich.CHO_THANH_TOAN,
            TrangThaiDatLich.DA_XAC_NHAN,
            TrangThaiDatLich.DANG_KHAM
        );

        // 3. Check duplicate slot (only active & not deleted)
        boolean slotOccupied = datLichKhamRepository.existsActiveBookingForSlot(
            request.getBacSiID(),
            request.getNgayKham(),
            request.getCa(),
            request.getGioKham(),
            activeStatuses
        );

        if (slotOccupied) {
            throw new ConflictException("Khung giờ này đã có người đặt");
        }

        // 4. Check patient conflict (only active & not deleted)
        boolean patientConflict = datLichKhamRepository.existsActivePatientConflict(
            currentUserId,
            request.getNgayKham(),
            request.getCa(),
            request.getGioKham(),
            activeStatuses
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

        try {
            booking = datLichKhamRepository.save(booking);
        } catch (DataIntegrityViolationException e) {
            // Nếu DB còn unique index cũ gây trùng dù booking đã hủy/hoàn thành
            if (e.getMessage() != null && e.getMessage().contains("unique_booking_slot_active")) {
                throw new ConflictException("Khung giờ này đã có người đặt (unique index cũ). Vui lòng drop index unique_booking_slot_active.");
            }
            // Bắt lỗi unique khác
            throw new ConflictException("Khung giờ này đã có người đặt (trùng slot)");
        }
        log.info("✅ Created booking #{}", booking.getDatLichID());

        if (request.hasMedicalInfo()) {
            medicalRecordService.upsertFromBooking(patient, request);
        }

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

    @Transactional(readOnly = true)
    public Page<BookingResponse> getDoctorHistoryByPhone(
            Integer doctorId,
            String phone,
            LocalDate fromDate,
            LocalDate toDate,
            TrangThaiDatLich status,
            PhuongThucThanhToan paymentMethod,
            Boolean hasRating,
            Integer doctorFilterId,
            Pageable pageable
    ) {
        if (phone == null || phone.isBlank()) {
            throw new BadRequestException("Số điện thoại không được trống");
        }
        NguoiDung patient = nguoiDungRepository.findBySoDienThoai(phone.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bệnh nhân với số điện thoại này"));
        if (patient.getVaiTro() != VaiTro.BenhNhan) {
            throw new BadRequestException("Người dùng không phải bệnh nhân");
        }
        if (Boolean.TRUE.equals(patient.getIsDeleted())) {
            throw new BadRequestException("Tài khoản bệnh nhân đã bị khóa/xóa");
        }
        Page<DatLichKham> bookings = datLichKhamRepository.searchAdminHistory(
                fromDate,
                toDate,
                status,
                paymentMethod,
                hasRating,
                doctorFilterId,
                patient.getNguoiDungID(),
                null,
                pageable
        );
        return bookings.map(BookingResponse::of);
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
        // Sinh mã thân thiện cho người nhập: BK + 6 ký tự (2-9,A-H,J-N,P-Z), tổng 8 ký tự
        for (int attempt = 0; attempt < 5; attempt++) {
            StringBuilder sb = new StringBuilder("BK");
            for (int i = 0; i < CONFIRM_CODE_RANDOM_LEN; i++) {
                int idx = RANDOM.nextInt(FRIENDLY_ALPHABET.length());
                sb.append(FRIENDLY_ALPHABET.charAt(idx));
            }
            String code = sb.toString();
            boolean exists = datLichKhamRepository.findByMaXacNhan(code).isPresent();
            if (!exists) {
                return code;
            }
        }
        throw new IllegalStateException("Không thể sinh mã xác nhận (trùng quá nhiều lần)");
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

    /**
     * Xác nhận đã thanh toán tiền mặt (LeTan/BacSi/Admin)
     */
    @Transactional
    public BookingResponse confirmCashPayment(Integer datLichID) {
        DatLichKham booking = getBookingById(datLichID);

        if (booking.getPhuongThucThanhToan() != PhuongThucThanhToan.TIEN_MAT) {
            throw new BadRequestException("Chỉ áp dụng xác nhận tiền mặt cho phương thức TIEN_MAT");
        }
        if (booking.getTrangThaiThanhToan() == TrangThaiThanhToan.THANH_CONG) {
            return BookingResponse.of(booking); // đã thanh toán rồi
        }

        booking.setTrangThaiThanhToan(TrangThaiThanhToan.THANH_CONG);
        booking.setNgayThanhToan(LocalDateTime.now());
        booking.setMaGiaoDich("CASH_CONFIRM_" + System.currentTimeMillis());

        booking = datLichKhamRepository.save(booking);
        return BookingResponse.of(booking);
    }

    /**
     * Xác nhận thanh toán tiền mặt bằng mã xác nhận
     */
    @Transactional
    public BookingResponse confirmCashPaymentByCode(String confirmationCode) {
        DatLichKham booking = datLichKhamRepository.findByMaXacNhan(confirmationCode)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking với mã này"));

        if (Boolean.TRUE.equals(booking.getIsDeleted())) {
            throw new BadRequestException("Booking đã bị xóa");
        }

        return confirmCashPayment(booking.getDatLichID());
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
     * Lịch sử khám của bệnh nhân (có lọc)
     */
    @Transactional(readOnly = true)
    public Page<BookingResponse> getPatientHistory(
        Integer benhNhanID,
        LocalDate fromDate,
        LocalDate toDate,
        TrangThaiDatLich status,
        PhuongThucThanhToan paymentMethod,
        Boolean hasRating,
        Integer doctorId,
        Integer facilityId,
        Pageable pageable
    ) {
        Page<DatLichKham> bookings = datLichKhamRepository.searchPatientHistory(
            benhNhanID,
            fromDate,
            toDate,
            status,
            paymentMethod,
            hasRating,
            doctorId,
            facilityId,
            pageable
        );
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

    /**
     * Lịch sử khám của bác sĩ (có lọc)
     */
    @Transactional(readOnly = true)
    public Page<BookingResponse> getDoctorHistory(
        Integer bacSiID,
        LocalDate fromDate,
        LocalDate toDate,
        TrangThaiDatLich status,
        PhuongThucThanhToan paymentMethod,
        Boolean hasRating,
        Integer patientId,
        Integer facilityId,
        Pageable pageable
    ) {
        Page<DatLichKham> bookings = datLichKhamRepository.searchDoctorHistory(
            bacSiID,
            fromDate,
            toDate,
            status,
            paymentMethod,
            hasRating,
            patientId,
            facilityId,
            pageable
        );
        return bookings.map(BookingResponse::of);
    }

    /**
     * Lấy lịch làm việc thực tế của 1 bác sĩ trong khoảng ngày (cho bệnh nhân/ bác sĩ xem)
     */
    @Transactional(readOnly = true)
    public List<DoctorScheduleItemResponse> getDoctorSchedule(
        Integer bacSiId,
        LocalDate fromDate,
        LocalDate toDate
    ) {
        LocalDate from = fromDate != null ? fromDate : LocalDate.now();
        LocalDate to = toDate != null ? toDate : from.plusDays(6);

        if (to.isBefore(from)) {
            throw new BadRequestException("Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu");
        }
        if (ChronoUnit.DAYS.between(from, to) > MAX_SCHEDULE_RANGE_DAYS) {
            throw new BadRequestException("Chỉ cho phép xem tối đa 31 ngày một lần");
        }

        BacSi doctor = bacSiRepository.findById(bacSiId)
            .orElseThrow(() -> new ResourceNotFoundException("Bác sĩ không tồn tại"));
        if (Boolean.FALSE.equals(doctor.getTrangThaiCongViec())) {
            throw new BadRequestException("Bác sĩ hiện không nhận khám");
        }

        List<LichLamViecMacDinh> defaultSchedules = lichLamViecMacDinhRepository.findAllActive()
            .stream()
            .filter(LichLamViecMacDinh::getIsActive)
            .toList();
        Map<Integer, List<LichLamViecMacDinh>> scheduleByThu = defaultSchedules.stream()
            .collect(Collectors.groupingBy(LichLamViecMacDinh::getThuTrongTuan));

        List<BacSiNgayNghi> leaves = bacSiNgayNghiRepository.findApprovedLeavesInRange(bacSiId, from, to);

        List<TrangThaiDatLich> activeStatuses = Arrays.asList(
            TrangThaiDatLich.CHO_XAC_NHAN_BAC_SI,
            TrangThaiDatLich.DA_XAC_NHAN,
            TrangThaiDatLich.DANG_KHAM
        );

        List<DoctorScheduleItemResponse> result = new ArrayList<>();

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            LocalDate currentDate = date;
            int thu = convertToThuTrongTuan(currentDate);
            List<LichLamViecMacDinh> schedulesForDay = scheduleByThu.getOrDefault(thu, Collections.emptyList());

            boolean leaveFullDay = leaves.stream().anyMatch(n ->
                (n.getLoaiNghi() == LoaiNghi.NGAY_CU_THE &&
                    currentDate.equals(n.getNgayNghiCuThe())) ||
                // Nghỉ ca cụ thể nhưng ca = null => nghỉ cả ngày đó
                (n.getLoaiNghi() == LoaiNghi.CA_CU_THE &&
                    n.getCa() == null &&
                    currentDate.equals(n.getNgayNghiCuThe())) ||
                // Nghỉ hàng tuần với ca = null => nghỉ cả ngày của thứ đó
                (n.getLoaiNghi() == LoaiNghi.CA_HANG_TUAN &&
                    n.getCa() == null &&
                    Objects.equals(n.getThuTrongTuan(), thu))
            );

            for (LichLamViecMacDinh schedule : schedulesForDay) {
                boolean onLeave = leaveFullDay || isLeaveForShift(leaves, currentDate, thu, schedule.getCa());

                List<LocalTime> bookedTimes = datLichKhamRepository.findBookedTimeSlots(
                    bacSiId,
                    currentDate,
                    schedule.getCa(),
                    activeStatuses
                );

                int totalSlots = calculateTotalSlots(schedule.getThoiGianBatDau(), schedule.getThoiGianKetThuc());
                int remainingSlots = Math.max(totalSlots - bookedTimes.size(), 0);

                DoctorScheduleItemResponse item = DoctorScheduleItemResponse.builder()
                    .ngay(currentDate)
                    .thu(thu)
                    .tenThu(getTenThu(thu))
                    .ca(schedule.getCa())
                    .tenCa(schedule.getCa().getTenCa())
                    .gioBatDau(DoctorScheduleItemResponse.formatTime(schedule.getThoiGianBatDau()))
                    .gioKetThuc(DoctorScheduleItemResponse.formatTime(schedule.getThoiGianKetThuc()))
                    .isOnLeave(onLeave)
                    .loaiNghi(resolveLoaiNghi(leaves, currentDate, thu, schedule.getCa(), leaveFullDay))
                    .ghiChuNghi(resolveLeaveNote(leaves, currentDate, thu, schedule.getCa(), leaveFullDay))
                    .soSlotDaDat(bookedTimes.size())
                    .tongSlot(totalSlots)
                    .slotConLai(remainingSlots)
                    .gioDaDat(bookedTimes.stream().map(DoctorScheduleItemResponse::formatTime).toList())
                    // Còn nhận lịch nếu không nghỉ và còn slot trống
                    .available(!onLeave && remainingSlots > 0)
                    .build();

                result.add(item);
            }
        }

        return result;
    }

    /**
     * Lịch sử khám cho admin (có lọc)
     */
    @Transactional(readOnly = true)
    public Page<BookingResponse> getAdminHistory(
        LocalDate fromDate,
        LocalDate toDate,
        TrangThaiDatLich status,
        PhuongThucThanhToan paymentMethod,
        Boolean hasRating,
        Integer doctorId,
        Integer patientId,
        Integer facilityId,
        Pageable pageable
    ) {
        Page<DatLichKham> bookings = datLichKhamRepository.searchAdminHistory(
            fromDate,
            toDate,
            status,
            paymentMethod,
            hasRating,
            doctorId,
            patientId,
            facilityId,
            pageable
        );
        return bookings.map(BookingResponse::of);
    }

    /**
     * Tìm danh sách slot trống cho 1 bác sĩ/ngày/ca
     * Lưu ý: dựa trên lịch mặc định toàn viện + nghỉ đã duyệt; chưa có lịch riêng từng bác sĩ
     */
    @Transactional(readOnly = true)
    public AvailableSlotsResponse searchAvailableSlots(SearchAvailableSlotsRequest request) {
        request.validate();
        validateBookingDate(request.getNgayKham());

        BacSi doctor = bacSiRepository.findById(request.getBacSiID())
            .orElseThrow(() -> new ResourceNotFoundException("Bác sĩ không tồn tại"));
        if (!Boolean.TRUE.equals(doctor.getTrangThaiCongViec())) {
            throw new BadRequestException("Bác sĩ hiện không nhận khám");
        }

        int thu = convertToThuTrongTuan(request.getNgayKham());
        // Lấy lịch mặc định theo thứ + ca
        LichLamViecMacDinh schedule = lichLamViecMacDinhRepository.findByThuAndCa(thu, request.getCa())
            .orElseThrow(() -> new BadRequestException("Bác sĩ không làm việc ca này theo lịch mặc định"));

        // Lấy nghỉ trong ngày này (đủ cho 1 ngày)
        List<BacSiNgayNghi> leaves = bacSiNgayNghiRepository.findApprovedLeavesOnDate(
            request.getBacSiID(),
            request.getNgayKham(),
            thu
        );

        List<TrangThaiDatLich> activeStatuses = Arrays.asList(
            TrangThaiDatLich.CHO_THANH_TOAN,
            TrangThaiDatLich.CHO_XAC_NHAN_BAC_SI,
            TrangThaiDatLich.DA_XAC_NHAN,
            TrangThaiDatLich.DANG_KHAM
        );

        return buildAvailableSlotsResponse(doctor, request.getNgayKham(), schedule, leaves, activeStatuses);
    }

    /**
     * Lấy slot trống cho 1 bác sĩ trong 1 ngày (tối đa trong 7 ngày kể từ hôm nay)
     */
    @Transactional(readOnly = true)
    public List<AvailableSlotsResponse> getAvailableSlotsForDay(Integer bacSiId, LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();
        validateBookingDate(target);
        // giới hạn trong 7 ngày từ hôm nay
        if (ChronoUnit.DAYS.between(LocalDate.now(), target) > 6) {
            throw new BadRequestException("Chỉ cho phép xem trong phạm vi 7 ngày kể từ hôm nay");
        }

        BacSi doctor = bacSiRepository.findById(bacSiId)
            .orElseThrow(() -> new ResourceNotFoundException("Bác sĩ không tồn tại"));
        if (!Boolean.TRUE.equals(doctor.getTrangThaiCongViec())) {
            throw new BadRequestException("Bác sĩ hiện không nhận khám");
        }

        int thu = convertToThuTrongTuan(target);
        List<LichLamViecMacDinh> schedulesForDay = lichLamViecMacDinhRepository.findByThuTrongTuan(thu);

        List<BacSiNgayNghi> leaves = bacSiNgayNghiRepository.findApprovedLeavesOnDate(
            bacSiId,
            target,
            thu
        );

        List<TrangThaiDatLich> activeStatuses = Arrays.asList(
            TrangThaiDatLich.CHO_THANH_TOAN,
            TrangThaiDatLich.CHO_XAC_NHAN_BAC_SI,
            TrangThaiDatLich.DA_XAC_NHAN,
            TrangThaiDatLich.DANG_KHAM
        );

        List<AvailableSlotsResponse> result = new ArrayList<>();
        for (LichLamViecMacDinh schedule : schedulesForDay) {
            AvailableSlotsResponse resp = buildAvailableSlotsResponse(doctor, target, schedule, leaves, activeStatuses);
            result.add(resp);
        }
        return result;
    }

    private boolean isLeaveForShift(List<BacSiNgayNghi> leaves, LocalDate date, int thu, CaLamViec ca) {
        return leaves.stream().anyMatch(n -> {
            if (n.getLoaiNghi() == LoaiNghi.CA_CU_THE) {
                // ca = null => nghỉ cả ngày, coi là nghỉ mọi ca trong ngày
                boolean fullDay = date.equals(n.getNgayNghiCuThe()) && n.getCa() == null;
                boolean exactShift = date.equals(n.getNgayNghiCuThe()) && ca == n.getCa();
                return fullDay || exactShift;
            }
            if (n.getLoaiNghi() == LoaiNghi.CA_HANG_TUAN) {
                boolean sameDay = Objects.equals(n.getThuTrongTuan(), thu);
                // ca = null => nghỉ cả ngày trong thứ đó
                boolean caMatch = n.getCa() == null || n.getCa() == ca;
                return sameDay && caMatch;
            }
            return false;
        });
    }

    private LoaiNghi resolveLoaiNghi(
        List<BacSiNgayNghi> leaves,
        LocalDate date,
        int thu,
        CaLamViec ca,
        boolean leaveFullDay
    ) {
        if (leaveFullDay) {
            return LoaiNghi.NGAY_CU_THE;
        }
        return leaves.stream()
            .filter(n -> {
                if (n.getLoaiNghi() == LoaiNghi.CA_CU_THE) {
                    boolean fullDay = date.equals(n.getNgayNghiCuThe()) && n.getCa() == null;
                    boolean exactShift = date.equals(n.getNgayNghiCuThe()) && ca == n.getCa();
                    return fullDay || exactShift;
                }
                if (n.getLoaiNghi() == LoaiNghi.CA_HANG_TUAN) {
                    boolean sameDay = Objects.equals(n.getThuTrongTuan(), thu);
                    boolean caMatch = n.getCa() == null || n.getCa() == ca;
                    return sameDay && caMatch;
                }
                return false;
            })
            .map(BacSiNgayNghi::getLoaiNghi)
            .findFirst()
            .orElse(null);
    }

    private String resolveLeaveNote(
        List<BacSiNgayNghi> leaves,
        LocalDate date,
        int thu,
        CaLamViec ca,
        boolean leaveFullDay
    ) {
        return leaves.stream()
            .filter(n -> {
                if (leaveFullDay &&
                    ((n.getLoaiNghi() == LoaiNghi.NGAY_CU_THE && date.equals(n.getNgayNghiCuThe())) ||
                     (n.getLoaiNghi() == LoaiNghi.CA_CU_THE && date.equals(n.getNgayNghiCuThe()) && n.getCa() == null) ||
                     (n.getLoaiNghi() == LoaiNghi.CA_HANG_TUAN && Objects.equals(n.getThuTrongTuan(), thu) && n.getCa() == null))
                ) {
                    return true;
                }
                if (n.getLoaiNghi() == LoaiNghi.CA_CU_THE) {
                    boolean fullDay = date.equals(n.getNgayNghiCuThe()) && n.getCa() == null;
                    boolean exactShift = date.equals(n.getNgayNghiCuThe()) && ca == n.getCa();
                    return fullDay || exactShift;
                }
                if (n.getLoaiNghi() == LoaiNghi.CA_HANG_TUAN) {
                    boolean sameDay = Objects.equals(n.getThuTrongTuan(), thu);
                    boolean caMatch = n.getCa() == null || n.getCa() == ca;
                    return sameDay && caMatch;
                }
                return false;
            })
            .map(BacSiNgayNghi::getLyDo)
            .findFirst()
            .orElse(null);
    }

    /**
     * Tính số slot trong một ca dựa trên bước slot mặc định
     */
    private int calculateTotalSlots(LocalTime start, LocalTime end) {
        if (start == null || end == null) return 0;
        long minutes = ChronoUnit.MINUTES.between(start, end);
        if (minutes <= 0) return 0;
        return (int) Math.ceil(minutes / (double) DEFAULT_SLOT_MINUTES);
    }

    /**
     * Sinh danh sách giờ bắt đầu slot trong khoảng [start, end)
     */
    private List<LocalTime> generateSlots(LocalTime start, LocalTime end) {
        List<LocalTime> result = new ArrayList<>();
        if (start == null || end == null) return result;
        LocalTime cursor = start;
        while (cursor.isBefore(end)) {
            result.add(cursor);
            cursor = cursor.plusMinutes(DEFAULT_SLOT_MINUTES);
        }
        return result;
    }

    private long safeCount(Long value) {
        return value != null ? value : 0L;
    }

    private BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    /**
     * Helper: build response slot trống cho 1 ngày/ca
     */
    private AvailableSlotsResponse buildAvailableSlotsResponse(
        BacSi doctor,
        LocalDate ngayKham,
        LichLamViecMacDinh schedule,
        List<BacSiNgayNghi> leaves,
        List<TrangThaiDatLich> activeStatuses
    ) {
        int thu = convertToThuTrongTuan(ngayKham);

        boolean leaveFullDay = leaves.stream().anyMatch(n ->
            (n.getLoaiNghi() == LoaiNghi.NGAY_CU_THE && ngayKham.equals(n.getNgayNghiCuThe())) ||
                (n.getLoaiNghi() == LoaiNghi.CA_CU_THE && n.getCa() == null && ngayKham.equals(n.getNgayNghiCuThe())) ||
                (n.getLoaiNghi() == LoaiNghi.CA_HANG_TUAN && n.getCa() == null && Objects.equals(n.getThuTrongTuan(), thu))
        );
        boolean leaveThisShift = leaveFullDay || isLeaveForShift(leaves, ngayKham, thu, schedule.getCa());

        if (leaveThisShift) {
            AvailableSlotsResponse resp = AvailableSlotsResponse.builder()
                .bacSiID(doctor.getBacSiID())
                .tenBacSi(doctor.getNguoiDung().getHoTen())
                .tenChuyenKhoa(doctor.getChuyenKhoa() != null ? doctor.getChuyenKhoa().getTenChuyenKhoa() : null)
                .tenTrinhDo(doctor.getTrinhDo() != null ? doctor.getTrinhDo().getTenTrinhDo() : null)
                .avatarUrl(doctor.getNguoiDung().getAvatarUrl())
                .ngayKham(ngayKham)
                .ca(schedule.getCa())
                .tenCa(schedule.getCa().getTenCa())
                .giaKham(doctor.getGiaKham())
                .slots(Collections.emptyList())
                .totalSlots(0)
                .availableSlots(0)
                .bookedSlots(0)
                .hasAvailableSlots(false)
                .build();
            resp.calculate();
            return resp;
        }

        List<LocalTime> bookedTimes = datLichKhamRepository.findBookedTimeSlots(
            doctor.getBacSiID(),
            ngayKham,
            schedule.getCa(),
            activeStatuses
        );

        List<LocalTime> generatedSlots = generateSlots(schedule.getThoiGianBatDau(), schedule.getThoiGianKetThuc());
        Set<LocalTime> bookedSet = new HashSet<>(bookedTimes);

        List<TimeSlotResponse> slotResponses = generatedSlots.stream()
            .map(time -> TimeSlotResponse.builder()
                .gioKham(time)
                .gioBatDau(time)
                .gioKetThuc(time.plusMinutes(DEFAULT_SLOT_MINUTES))
                .available(!bookedSet.contains(time))
                .label(String.format("%s - %s",
                    DoctorScheduleItemResponse.formatTime(time),
                    DoctorScheduleItemResponse.formatTime(time.plusMinutes(DEFAULT_SLOT_MINUTES))
                ))
                .build()
            )
            .toList();

        AvailableSlotsResponse response = AvailableSlotsResponse.builder()
            .bacSiID(doctor.getBacSiID())
            .tenBacSi(doctor.getNguoiDung().getHoTen())
            .tenChuyenKhoa(doctor.getChuyenKhoa() != null ? doctor.getChuyenKhoa().getTenChuyenKhoa() : null)
            .tenTrinhDo(doctor.getTrinhDo() != null ? doctor.getTrinhDo().getTenTrinhDo() : null)
            .avatarUrl(doctor.getNguoiDung().getAvatarUrl())
            .ngayKham(ngayKham)
            .ca(schedule.getCa())
            .tenCa(schedule.getCa().getTenCa())
            .giaKham(doctor.getGiaKham())
            .slots(slotResponses)
            .build();
        response.calculate();
        return response;
    }

    private int convertToThuTrongTuan(LocalDate date) {
        // Java DayOfWeek: MONDAY=1 ... SUNDAY=7. Hệ thống: 2=Mon ... 8=Sun
        return date.getDayOfWeek().getValue() + 1;
    }

    private String getTenThu(int thu) {
        return switch (thu) {
            case 2 -> "Thứ 2";
            case 3 -> "Thứ 3";
            case 4 -> "Thứ 4";
            case 5 -> "Thứ 5";
            case 6 -> "Thứ 6";
            case 7 -> "Thứ 7";
            case 8 -> "Chủ nhật";
            default -> "Không xác định";
        };
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

    /**
     * Thống kê booking cho admin dashboard
     */
    @Transactional(readOnly = true)
    public BookingStatisticsResponse getBookingStatistics() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1); // Monday
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate monthEnd = today.with(TemporalAdjusters.lastDayOfMonth());

        long total = safeCount(datLichKhamRepository.countAllActive());

        long pendingApproval = safeCount(datLichKhamRepository.countByTrangThai(TrangThaiDatLich.CHO_XAC_NHAN_BAC_SI));
        long pendingPayment = safeCount(datLichKhamRepository.countByTrangThai(TrangThaiDatLich.CHO_THANH_TOAN));
        long confirmed = safeCount(datLichKhamRepository.countByTrangThai(TrangThaiDatLich.DA_XAC_NHAN));
        long inProgress = safeCount(datLichKhamRepository.countByTrangThai(TrangThaiDatLich.DANG_KHAM));
        long completed = safeCount(datLichKhamRepository.countByTrangThai(TrangThaiDatLich.HOAN_THANH));
        long rejected = safeCount(datLichKhamRepository.countByTrangThai(TrangThaiDatLich.TU_CHOI));
        long cancelled = safeCount(datLichKhamRepository.countByTrangThai(TrangThaiDatLich.HUY_BOI_BENH_NHAN))
                + safeCount(datLichKhamRepository.countByTrangThai(TrangThaiDatLich.HUY_BOI_BAC_SI))
                + safeCount(datLichKhamRepository.countByTrangThai(TrangThaiDatLich.HUY_BOI_ADMIN));
        long noShow = safeCount(datLichKhamRepository.countByTrangThai(TrangThaiDatLich.KHONG_DEN));

        long todayBookings = safeCount(datLichKhamRepository.countByDateRange(today, today));
        long weekBookings = safeCount(datLichKhamRepository.countByDateRange(weekStart, weekEnd));
        long monthBookings = safeCount(datLichKhamRepository.countByDateRange(monthStart, monthEnd));

        BigDecimal totalRevenue = toBigDecimal(datLichKhamRepository.calculateTotalRevenue());
        BigDecimal todayRevenue = toBigDecimal(datLichKhamRepository.calculateRevenueByDateRange(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay().minusNanos(1)));
        BigDecimal weekRevenue = toBigDecimal(datLichKhamRepository.calculateRevenueByDateRange(
                weekStart.atStartOfDay(), weekEnd.plusDays(1).atStartOfDay().minusNanos(1)));
        BigDecimal monthRevenue = toBigDecimal(datLichKhamRepository.calculateRevenueByDateRange(
                monthStart.atStartOfDay(), monthEnd.plusDays(1).atStartOfDay().minusNanos(1)));

        long paidBookings = safeCount(datLichKhamRepository.countByPaymentStatus(TrangThaiThanhToan.THANH_CONG));
        long unpaidBookings = safeCount(datLichKhamRepository.countByPaymentStatus(TrangThaiThanhToan.CHUA_THANH_TOAN))
                + safeCount(datLichKhamRepository.countByPaymentStatus(TrangThaiThanhToan.DANG_XU_LY))
                + safeCount(datLichKhamRepository.countByPaymentStatus(TrangThaiThanhToan.THAT_BAI));
        long refundCount = safeCount(datLichKhamRepository.countRefunds());
        BigDecimal totalRefund = toBigDecimal(datLichKhamRepository.calculateTotalRefund());

        Double averageRating = datLichKhamRepository.calculateAverageRatingAll();
        long totalRatings = safeCount(datLichKhamRepository.countRatingsAll());
        long fiveStars = safeCount(datLichKhamRepository.countRatingsByStarsAll(5));
        long fourStars = safeCount(datLichKhamRepository.countRatingsByStarsAll(4));
        long threeStars = safeCount(datLichKhamRepository.countRatingsByStarsAll(3));
        long twoStars = safeCount(datLichKhamRepository.countRatingsByStarsAll(2));
        long oneStar = safeCount(datLichKhamRepository.countRatingsByStarsAll(1));

        BookingStatisticsResponse response = BookingStatisticsResponse.builder()
                .totalBookings(total)
                .pendingApproval(pendingApproval)
                .pendingPayment(pendingPayment)
                .confirmed(confirmed)
                .inProgress(inProgress)
                .completed(completed)
                .cancelled(cancelled)
                .noShow(noShow)
                .rejected(rejected)
                .todayBookings(todayBookings)
                .thisWeekBookings(weekBookings)
                .thisMonthBookings(monthBookings)
                .totalRevenue(totalRevenue)
                .todayRevenue(todayRevenue)
                .thisWeekRevenue(weekRevenue)
                .thisMonthRevenue(monthRevenue)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalRatings(totalRatings)
                .fiveStars(fiveStars)
                .fourStars(fourStars)
                .threeStars(threeStars)
                .twoStars(twoStars)
                .oneStar(oneStar)
                .paidBookings(paidBookings)
                .unpaidBookings(unpaidBookings)
                .refundCount(refundCount)
                .totalRefund(totalRefund)
                .build();
        response.calculate();
        return response;
    }

    // ========================================
    // THỐNG KÊ DOANH THU / TOP
    // ========================================

    @Transactional(readOnly = true)
    public List<SpecialtyRevenueResponse> getRevenueBySpecialty(LocalDate fromDate, LocalDate toDate) {
        return datLichKhamRepository.revenueBySpecialty(fromDate, toDate);
    }

    @Transactional(readOnly = true)
    public List<DoctorRevenueResponse> getRevenueByDoctor(LocalDate fromDate, LocalDate toDate) {
        // trả tối đa 200 bản ghi để tránh trả quá lớn
        Page<DoctorRevenueResponse> page = datLichKhamRepository.revenueByDoctor(fromDate, toDate, PageRequest.of(0, 200));
        return page.getContent();
    }

    @Transactional(readOnly = true)
    public List<DoctorRevenueResponse> getTopDoctorRevenue(LocalDate fromDate, LocalDate toDate, int size) {
        int limit = Math.max(1, Math.min(size, 50)); // giới hạn tối đa 50
        return datLichKhamRepository.topDoctorRevenue(fromDate, toDate, PageRequest.of(0, limit)).getContent();
    }

    @Transactional(readOnly = true)
    public List<DoctorRevenueResponse> getTopDoctorCompleted(LocalDate fromDate, LocalDate toDate, int size) {
        int limit = Math.max(1, Math.min(size, 50)); // giới hạn tối đa 50
        return datLichKhamRepository.topDoctorCompleted(fromDate, toDate, PageRequest.of(0, limit)).getContent();
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    private DatLichKham getBookingById(Integer datLichID) {
        return datLichKhamRepository.findById(datLichID)
            .orElseThrow(() -> new ResourceNotFoundException("Lịch hẹn không tồn tại"));
    }
}
