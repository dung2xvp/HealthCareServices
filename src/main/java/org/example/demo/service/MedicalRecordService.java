package org.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.example.demo.dto.request.CreateBookingRequest;
import org.example.demo.dto.request.UpdateMedicalRecordRequest;
import org.example.demo.dto.response.MedicalRecordResponse;
import org.example.demo.entity.HoSoBenhAn;
import org.example.demo.entity.NguoiDung;
import org.example.demo.enums.VaiTro;
import org.example.demo.exception.ResourceNotFoundException;
import org.example.demo.exception.UnauthorizedException;
import org.example.demo.repository.HoSoBenhAnRepository;
import org.example.demo.repository.NguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@Service
public class MedicalRecordService {

    @Autowired
    private HoSoBenhAnRepository hoSoBenhAnRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Transactional(readOnly = true)
    public MedicalRecordResponse getOwnRecord(Integer userId) {
        HoSoBenhAn record = hoSoBenhAnRepository.findByBenhNhan_NguoiDungID(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Chưa có hồ sơ bệnh án"));
        return MedicalRecordResponse.fromEntity(record);
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse getRecordByPatient(Integer patientId) {
        HoSoBenhAn record = hoSoBenhAnRepository.findByBenhNhan_NguoiDungID(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Chưa có hồ sơ bệnh án"));
        return MedicalRecordResponse.fromEntity(record);
    }

    @Transactional
    public MedicalRecordResponse updateOwnRecord(Integer userId, UpdateMedicalRecordRequest request) {
        NguoiDung patient = getActivePatient(userId);
        HoSoBenhAn record = hoSoBenhAnRepository.findByBenhNhan_NguoiDungID(userId)
                .orElseGet(() -> newRecord(patient));

        applyRequest(request, record);
        record.setBenhNhan(patient);
        HoSoBenhAn saved = hoSoBenhAnRepository.save(record);
        log.info("🩺 Updated medical record by patient {}", userId);
        return MedicalRecordResponse.fromEntity(saved);
    }

    @Transactional
    public MedicalRecordResponse updateRecordByDoctor(Integer patientId, UpdateMedicalRecordRequest request) {
        NguoiDung patient = getActivePatient(patientId);
        HoSoBenhAn record = hoSoBenhAnRepository.findByBenhNhan_NguoiDungID(patientId)
                .orElseGet(() -> newRecord(patient));

        applyRequest(request, record);
        record.setBenhNhan(patient);
        HoSoBenhAn saved = hoSoBenhAnRepository.save(record);
        log.info("🩺 Updated medical record by doctor for patient {}", patientId);
        return MedicalRecordResponse.fromEntity(saved);
    }

    /**
     * Gắn thông tin y tế từ request đặt lịch vào hồ sơ (nếu có)
     */
    @Transactional
    public void upsertFromBooking(NguoiDung patient, CreateBookingRequest request) {
        if (request == null || !request.hasMedicalInfo()) {
            return;
        }
        Optional<HoSoBenhAn> optional = hoSoBenhAnRepository.findByBenhNhan_NguoiDungID(patient.getNguoiDungID());
        HoSoBenhAn record = optional.orElseGet(() -> newRecord(patient));

        if (request.getDiUng() != null && !request.getDiUng().isBlank()) {
            record.setDiUng(request.getDiUng().trim());
        }
        if (request.getTienSuBenh() != null && !request.getTienSuBenh().isBlank()) {
            record.setBenhManTinh(request.getTienSuBenh().trim());
        }
        if (request.getThuocDangDung() != null && !request.getThuocDangDung().isBlank()) {
            record.setThuocDangDung(request.getThuocDangDung().trim());
        }

        record.setBenhNhan(patient);
        hoSoBenhAnRepository.save(record);
        log.info("🩺 Upsert medical record from booking for patient {}", patient.getNguoiDungID());
    }

    // ============================================================
    // Helpers
    // ============================================================
    private NguoiDung getActivePatient(Integer userId) {
        NguoiDung patient = nguoiDungRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bệnh nhân"));
        if (patient.getVaiTro() != VaiTro.BenhNhan) {
            throw new UnauthorizedException("Chỉ hỗ trợ bệnh nhân");
        }
        if (Boolean.FALSE.equals(patient.getTrangThai())) {
            throw new UnauthorizedException("Tài khoản chưa được kích hoạt");
        }
        return patient;
    }

    private void applyRequest(UpdateMedicalRecordRequest request, HoSoBenhAn record) {
        applyString(request.getNhomMau(), record::setNhomMau);
        applyBigDecimal(request.getChieuCao(), record::setChieuCao);
        applyBigDecimal(request.getCanNang(), record::setCanNang);
        applyString(request.getDiUng(), record::setDiUng);
        applyString(request.getBenhManTinh(), record::setBenhManTinh);
        applyString(request.getThuocDangDung(), record::setThuocDangDung);
        applyString(request.getPhauThuatDaQua(), record::setPhauThuatDaQua);
        applyString(request.getTienSuGiaDinh(), record::setTienSuGiaDinh);

        if (request.getHutThuoc() != null) {
            record.setHutThuoc(request.getHutThuoc());
        }
        if (request.getUongRuou() != null) {
            record.setUongRuou(request.getUongRuou());
        }
    }

    private void applyString(String value, Consumer<String> setter) {
        if (value != null) {
            String v = value.isBlank() ? null : value.trim();
            setter.accept(v);
        }
    }

    private void applyBigDecimal(BigDecimal value, Consumer<BigDecimal> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private HoSoBenhAn newRecord(NguoiDung patient) {
        return new HoSoBenhAn(null, patient, null, null, null, null, null, null, null, null, false, false, null, null);
    }
}

