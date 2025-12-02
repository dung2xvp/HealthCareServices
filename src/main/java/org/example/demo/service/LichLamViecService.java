package org.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.example.demo.dto.request.LichLamViecBulkCreateRequest;
import org.example.demo.dto.request.LichLamViecRequest;
import org.example.demo.dto.request.ToggleScheduleActiveRequest;
import org.example.demo.dto.response.LichLamViecResponse;
import org.example.demo.dto.response.LichLamViecSummaryResponse;
import org.example.demo.dto.response.LichLamViecWeeklyResponse;
import org.example.demo.entity.CoSoYTe;
import org.example.demo.entity.LichLamViecMacDinh;
import org.example.demo.exception.BadRequestException;
import org.example.demo.exception.ConflictException;
import org.example.demo.exception.ResourceNotFoundException;
import org.example.demo.repository.CoSoYTeRepository;
import org.example.demo.repository.LichLamViecMacDinhRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý logic nghiệp vụ cho Lịch Làm Việc Mặc Định
 * 
 * Features:
 * - CRUD lịch làm việc mặc định
 * - Bulk create (tạo nhiều ca cùng lúc)
 * - Toggle active/inactive
 * - Get weekly view (calendar)
 * - Get summary (statistics)
 * - Validation business rules
 * 
 * @author Healthcare System Team
 * @version 1.0
 */
@Service
@Transactional
@RequiredArgsConstructor
public class LichLamViecService {
    
    private final LichLamViecMacDinhRepository lichLamViecRepository;
    private final CoSoYTeRepository coSoYTeRepository;
    
    /**
     * Tạo 1 ca làm việc mặc định
     * 
     * Business Rules:
     * - Không được trùng (cùng thuTrongTuan + ca)
     * - thuTrongTuan: 2-8 (Thứ 2 - Chủ nhật)
     * - thoiGianKetThuc > thoiGianBatDau
     * 
     * @param request LichLamViecRequest
     * @return LichLamViecResponse
     */
    public LichLamViecResponse create(LichLamViecRequest request) {
        // Validate business rules
        request.validateBusinessRules();
        
        // Lấy cơ sở y tế đầu tiên (vì chỉ quản lý 1 cơ sở)
        CoSoYTe coSoYTe = coSoYTeRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Không tìm thấy cơ sở y tế trong hệ thống"
                ));
        
        // Check conflict: Không được trùng (cùng thuTrongTuan + ca)
        boolean exists = lichLamViecRepository.existsByCoSoAndThuAndCa(
            coSoYTe.getCoSoID(),
            request.getThuTrongTuan(),
            request.getCa()
        );
        
        if (exists) {
            throw new ConflictException(
                String.format("Đã tồn tại lịch làm việc cho %s - Ca %s",
                    getTenThu(request.getThuTrongTuan()),
                    request.getCa().getTenCa()
                )
            );
        }
        
        // Tạo entity
        LichLamViecMacDinh entity = new LichLamViecMacDinh();
        entity.setCoSoYTe(coSoYTe);
        entity.setThuTrongTuan(request.getThuTrongTuan());
        entity.setCa(request.getCa());
        entity.setThoiGianBatDau(request.getThoiGianBatDau());
        entity.setThoiGianKetThuc(request.getThoiGianKetThuc());
        entity.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        entity.setGhiChu(request.getGhiChu());
        
        // Lưu vào database
        LichLamViecMacDinh saved = lichLamViecRepository.save(entity);
        
        return LichLamViecResponse.fromEntity(saved);
    }
    
    /**
     * Tạo nhiều ca làm việc cùng lúc (Bulk Create)
     * 
     * Use Cases:
     * - Setup lịch ban đầu (14 ca/tuần)
     * - Import lịch từ file
     * 
     * @param request LichLamViecBulkCreateRequest
     * @return List<LichLamViecResponse>
     */
    public List<LichLamViecResponse> bulkCreate(LichLamViecBulkCreateRequest request) {
        // Validate business rules
        request.validateBusinessRules();
        
        // Lấy cơ sở y tế
        CoSoYTe coSoYTe = coSoYTeRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Không tìm thấy cơ sở y tế trong hệ thống"
                ));
        
        List<LichLamViecResponse> results = new ArrayList<>();
        
        // Tạo từng ca
        for (LichLamViecRequest scheduleRequest : request.getSchedules()) {
            // Check conflict với DB
            boolean exists = lichLamViecRepository.existsByCoSoAndThuAndCa(
                coSoYTe.getCoSoID(),
                scheduleRequest.getThuTrongTuan(),
                scheduleRequest.getCa()
            );
            
            if (exists) {
                // Skip nếu đã tồn tại (hoặc throw exception tùy yêu cầu)
                continue;
            }
            
            // Tạo entity
            LichLamViecMacDinh entity = new LichLamViecMacDinh();
            entity.setCoSoYTe(coSoYTe);
            entity.setThuTrongTuan(scheduleRequest.getThuTrongTuan());
            entity.setCa(scheduleRequest.getCa());
            entity.setThoiGianBatDau(scheduleRequest.getThoiGianBatDau());
            entity.setThoiGianKetThuc(scheduleRequest.getThoiGianKetThuc());
            entity.setIsActive(scheduleRequest.getIsActive() != null ? scheduleRequest.getIsActive() : true);
            entity.setGhiChu(scheduleRequest.getGhiChu());
            
            // Lưu
            LichLamViecMacDinh saved = lichLamViecRepository.save(entity);
            results.add(LichLamViecResponse.fromEntity(saved));
        }
        
        return results;
    }
    
    /**
     * Lấy tất cả lịch làm việc mặc định
     * 
     * @return List<LichLamViecResponse>
     */
    public List<LichLamViecResponse> getAll() {
        return lichLamViecRepository.findAll()
                .stream()
                .map(LichLamViecResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy lịch làm việc theo ID
     * 
     * @param id ConfigID
     * @return LichLamViecResponse
     */
    public LichLamViecResponse getById(Integer id) {
        LichLamViecMacDinh entity = lichLamViecRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Không tìm thấy lịch làm việc với ID: " + id
                ));
        
        return LichLamViecResponse.fromEntity(entity);
    }
    
    /**
     * Lấy lịch làm việc theo ngày trong tuần
     * 
     * @param thuTrongTuan 2-8
     * @return List<LichLamViecResponse>
     */
    public List<LichLamViecResponse> getByDay(Integer thuTrongTuan) {
        if (thuTrongTuan < 2 || thuTrongTuan > 8) {
            throw new BadRequestException("Thứ trong tuần phải từ 2-8");
        }
        
        return lichLamViecRepository.findByThuTrongTuan(thuTrongTuan)
                .stream()
                .map(LichLamViecResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy lịch làm việc đang active
     * 
     * @return List<LichLamViecResponse>
     */
    public List<LichLamViecResponse> getActiveSchedules() {
        return lichLamViecRepository.findAllActive()
                .stream()
                .map(LichLamViecResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy lịch làm việc theo tuần (Calendar view)
     * 
     * @return List<LichLamViecWeeklyResponse> - 7 ngày group by day
     */
    public List<LichLamViecWeeklyResponse> getWeeklyView() {
        List<LichLamViecMacDinh> allSchedules = lichLamViecRepository.findAll();
        return LichLamViecWeeklyResponse.fromEntities(allSchedules);
    }
    
    /**
     * Lấy tổng quan lịch làm việc (Dashboard/Statistics)
     * 
     * @return LichLamViecSummaryResponse
     */
    public LichLamViecSummaryResponse getSummary() {
        List<LichLamViecMacDinh> allSchedules = lichLamViecRepository.findAll();
        return LichLamViecSummaryResponse.fromEntities(allSchedules);
    }
    
    /**
     * Cập nhật lịch làm việc
     * 
     * @param id ConfigID
     * @param request LichLamViecRequest
     * @return LichLamViecResponse
     */
    public LichLamViecResponse update(Integer id, LichLamViecRequest request) {
        // Validate business rules
        request.validateBusinessRules();
        
        // Tìm entity
        LichLamViecMacDinh entity = lichLamViecRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Không tìm thấy lịch làm việc với ID: " + id
                ));
        
        // Check conflict nếu thay đổi thuTrongTuan hoặc ca
        if (!entity.getThuTrongTuan().equals(request.getThuTrongTuan()) ||
            !entity.getCa().equals(request.getCa())) {
            
            boolean exists = lichLamViecRepository.existsByCoSoAndThuAndCa(
                entity.getCoSoYTe().getCoSoID(),
                request.getThuTrongTuan(),
                request.getCa()
            );
            
            if (exists) {
                throw new ConflictException(
                    String.format("Đã tồn tại lịch làm việc cho %s - Ca %s",
                        getTenThu(request.getThuTrongTuan()),
                        request.getCa().getTenCa()
                    )
                );
            }
        }
        
        // Update fields
        entity.setThuTrongTuan(request.getThuTrongTuan());
        entity.setCa(request.getCa());
        entity.setThoiGianBatDau(request.getThoiGianBatDau());
        entity.setThoiGianKetThuc(request.getThoiGianKetThuc());
        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }
        entity.setGhiChu(request.getGhiChu());
        
        // Lưu thay đổi
        LichLamViecMacDinh updated = lichLamViecRepository.save(entity);
        
        return LichLamViecResponse.fromEntity(updated);
    }
    
    /**
     * Bật/tắt nhiều ca làm việc cùng lúc
     * 
     * Use Cases:
     * - Tắt tất cả ca Chủ nhật
     * - Tắt ca TOI toàn bộ
     * - Bật/tắt khi nghỉ lễ
     * 
     * @param request ToggleScheduleActiveRequest
     * @return List<LichLamViecResponse>
     */
    public List<LichLamViecResponse> toggleActive(ToggleScheduleActiveRequest request) {
        // Validate business rules
        request.validateBusinessRules();
        
        List<LichLamViecResponse> results = new ArrayList<>();
        
        for (Integer configID : request.getConfigIDs()) {
            LichLamViecMacDinh entity = lichLamViecRepository.findById(configID)
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy lịch làm việc với ID: " + configID
                    ));
            
            // Toggle active
            entity.setIsActive(request.getIsActive());
            
            // Update ghi chú nếu có reason
            if (request.getReason() != null && !request.getReason().isBlank()) {
                String note = entity.getGhiChu() != null ? entity.getGhiChu() + " | " : "";
                note += request.getReason();
                entity.setGhiChu(note);
            }
            
            // Lưu
            LichLamViecMacDinh updated = lichLamViecRepository.save(entity);
            results.add(LichLamViecResponse.fromEntity(updated));
        }
        
        return results;
    }
    
    /**
     * Xóa lịch làm việc
     * 
     * Note: Xóa cứng (hard delete) vì không cần soft delete
     * 
     * @param id ConfigID
     */
    public void delete(Integer id) {
        LichLamViecMacDinh entity = lichLamViecRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Không tìm thấy lịch làm việc với ID: " + id
                ));
        
        // Hard delete
        lichLamViecRepository.delete(entity);
    }
    
    /**
     * Xóa tất cả lịch làm việc (Reset)
     * 
     * Use Case: Reset lịch để setup lại từ đầu
     * ⚠️ Cẩn thận khi dùng!
     */
    public void deleteAll() {
        lichLamViecRepository.deleteAll();
    }
    
    // ===== HELPER METHODS =====
    
    /**
     * Helper: Convert thuTrongTuan to tên thứ
     */
    private String getTenThu(Integer thu) {
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
}

