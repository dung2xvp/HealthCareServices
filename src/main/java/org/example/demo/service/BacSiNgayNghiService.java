package org.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.example.demo.dto.request.ApproveNgayNghiRequest;
import org.example.demo.dto.request.CreateNgayNghiRequest;
import org.example.demo.dto.request.SearchNgayNghiRequest;
import org.example.demo.dto.request.UpdateNgayNghiRequest;
import org.example.demo.dto.response.NgayNghiMyRequestResponse;
import org.example.demo.dto.response.NgayNghiPendingResponse;
import org.example.demo.dto.response.NgayNghiResponse;
import org.example.demo.dto.response.NgayNghiStatisticsResponse;
import org.example.demo.entity.BacSi;
import org.example.demo.entity.BacSiNgayNghi;
import org.example.demo.entity.NguoiDung;
import org.example.demo.enums.LoaiNghiPhep;
import org.example.demo.enums.TrangThaiNghi;
import org.example.demo.exception.BadRequestException;
import org.example.demo.exception.ConflictException;
import org.example.demo.exception.ResourceNotFoundException;
import org.example.demo.exception.UnauthorizedException;
import org.example.demo.repository.BacSiNgayNghiRepository;
import org.example.demo.repository.BacSiRepository;
import org.example.demo.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý logic nghiệp vụ cho Yêu Cầu Nghỉ của Bác Sĩ
 * 
 * Features:
 * - CRUD yêu cầu nghỉ
 * - Approval workflow (Admin duyệt/từ chối)
 * - Search/Filter với nhiều criteria
 * - Validate business rules (conflict, ngày phép, ...)
 * - Statistics & reporting
 * 
 * @author Healthcare System Team
 * @version 1.0
 */
@Service
@Transactional
@RequiredArgsConstructor
public class BacSiNgayNghiService {
    
    private final BacSiNgayNghiRepository ngayNghiRepository;
    private final BacSiRepository bacSiRepository;
    
    /**
     * Bác sĩ tạo yêu cầu nghỉ mới
     * 
     * Business Rules:
     * 1. Validate request data (conditional fields)
     * 2. Check conflict với yêu cầu đã duyệt
     * 3. Check số ngày phép còn lại (nếu loaiNghiPhep = PHEP_NAM)
     * 4. Ngày nghỉ phải trong tương lai
     * 
     * @param bacSiID ID bác sĩ
     * @param request CreateNgayNghiRequest
     * @return NgayNghiResponse
     */
    public NgayNghiResponse create(Integer bacSiID, CreateNgayNghiRequest request) {
        // 1. Validate business rules
        request.validateBusinessRules();
        
        // 2. Tìm bác sĩ
        BacSi bacSi = bacSiRepository.findById(bacSiID)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Không tìm thấy bác sĩ với ID: " + bacSiID
                ));
        
        // 3. Check conflict với yêu cầu đã duyệt
        checkConflict(bacSi, request);
        
        // 4. Check số ngày phép (nếu PHEP_NAM)
        if (request.getLoaiNghiPhep() == LoaiNghiPhep.PHEP_NAM) {
            checkSoNgayPhep(bacSi);
        }
        
        // 5. Tạo entity
        BacSiNgayNghi entity = new BacSiNgayNghi();
        entity.setBacSi(bacSi);
        entity.setLoaiNghi(request.getLoaiNghi());
        entity.setNgayNghiCuThe(request.getNgayNghiCuThe());
        entity.setThuTrongTuan(request.getThuTrongTuan());
        entity.setCa(request.getCa());
        entity.setLyDo(request.getLyDo());
        entity.setLoaiNghiPhep(request.getLoaiNghiPhep());
        entity.setFileDinhKem(request.getFileDinhKem());
        entity.setTrangThai(TrangThaiNghi.CHO_DUYET);
        
        // 6. Lưu vào database
        BacSiNgayNghi saved = ngayNghiRepository.save(entity);
        
        return NgayNghiResponse.fromEntity(saved);
    }
    
    /**
     * Lấy tất cả yêu cầu nghỉ
     * 
     * @return List<NgayNghiResponse>
     */
    public List<NgayNghiResponse> getAll() {
        return ngayNghiRepository.findAll()
                .stream()
                .map(NgayNghiResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy yêu cầu nghỉ theo ID
     * 
     * @param id NghiID
     * @return NgayNghiResponse
     */
    public NgayNghiResponse getById(Integer id) {
        BacSiNgayNghi entity = ngayNghiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Không tìm thấy yêu cầu nghỉ với ID: " + id
                ));
        
        return NgayNghiResponse.fromEntity(entity);
    }
    
    /**
     * Lấy yêu cầu nghỉ CHỜ DUYỆT (cho Admin)
     * Sắp xếp theo thời gian tạo ASC (yêu cầu cũ nhất trước)
     * 
     * @return List<NgayNghiPendingResponse>
     */
    public List<NgayNghiPendingResponse> getPendingRequests() {
        return ngayNghiRepository.findAllPendingRequests()
                .stream()
                .map(NgayNghiPendingResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy yêu cầu nghỉ của 1 bác sĩ (Doctor xem yêu cầu của mình)
     * 
     * @param bacSiID ID bác sĩ
     * @return List<NgayNghiMyRequestResponse>
     */
    public List<NgayNghiMyRequestResponse> getMyRequests(Integer bacSiID) {
        return ngayNghiRepository.findByBacSi(bacSiID)
                .stream()
                .map(NgayNghiMyRequestResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Search/Filter yêu cầu nghỉ với nhiều criteria
     * 
     * @param searchRequest SearchNgayNghiRequest
     * @return List<NgayNghiResponse> - TODO: Add pagination support
     */
    public List<NgayNghiResponse> search(SearchNgayNghiRequest searchRequest) {
        // Validate business rules
        searchRequest.validateBusinessRules();
        
        // TODO: Implement full search with Specification pattern
        // For now, return basic results
        List<BacSiNgayNghi> entities;
        
        if (searchRequest.getBacSiID() != null) {
            entities = ngayNghiRepository.findByBacSi(searchRequest.getBacSiID());
        } else if (searchRequest.getLoaiNghi() != null) {
            entities = ngayNghiRepository.findByLoaiNghi(searchRequest.getLoaiNghi());
        } else {
            entities = ngayNghiRepository.findAll();
        }
        
        // Filter by trangThai if provided
        if (searchRequest.getTrangThai() != null) {
            entities = entities.stream()
                    .filter(e -> e.getTrangThai() == searchRequest.getTrangThai())
                    .collect(Collectors.toList());
        }
        
        // Convert to Response DTO
        return entities.stream()
                .map(NgayNghiResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy thống kê yêu cầu nghỉ
     * 
     * @param bacSiID Optional - nếu null thì lấy tất cả, nếu có thì chỉ lấy của bác sĩ này
     * @return NgayNghiStatisticsResponse
     */
    public NgayNghiStatisticsResponse getStatistics(Integer bacSiID) {
        List<BacSiNgayNghi> entities;
        
        if (bacSiID != null) {
            // Thống kê của 1 bác sĩ
            entities = ngayNghiRepository.findByBacSi(bacSiID);
        } else {
            // Thống kê tất cả
            entities = ngayNghiRepository.findAll();
        }
        
        return NgayNghiStatisticsResponse.fromEntities(entities);
    }
    
    /**
     * Bác sĩ cập nhật yêu cầu nghỉ (CHỈ khi status = CHO_DUYET)
     * 
     * @param id NghiID
     * @param request UpdateNgayNghiRequest
     * @return NgayNghiResponse
     */
    public NgayNghiResponse update(Integer id, UpdateNgayNghiRequest request) {
        // Validate business rules
        request.validateBusinessRules();
        
        // Tìm entity
        BacSiNgayNghi entity = ngayNghiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Không tìm thấy yêu cầu nghỉ với ID: " + id
                ));
        
        // Check status: CHỈ cho phép update khi CHO_DUYET
        if (entity.getTrangThai() != TrangThaiNghi.CHO_DUYET) {
            throw new BadRequestException(
                "Chỉ có thể chỉnh sửa yêu cầu đang chờ duyệt"
            );
        }
        
        // Update fields
        if (request.hasLyDoUpdate()) {
            entity.setLyDo(request.getLyDo());
        }
        
        if (request.hasFileDinhKemUpdate()) {
            entity.setFileDinhKem(request.getFileDinhKem());
        }
        
        // Lưu thay đổi
        BacSiNgayNghi updated = ngayNghiRepository.save(entity);
        
        return NgayNghiResponse.fromEntity(updated);
    }
    
    /**
     * Admin duyệt/từ chối yêu cầu nghỉ (Batch operation)
     * 
     * Business Rules:
     * - CHỈ duyệt được yêu cầu có status = CHO_DUYET
     * - Nếu APPROVE: Update status = DA_DUYET, trừ ngày phép (nếu PHEP_NAM)
     * - Nếu REJECT: Update status = TU_CHOI, ghi lý do từ chối
     * 
     * @param request ApproveNgayNghiRequest
     * @return List<NgayNghiResponse>
     */
    public List<NgayNghiResponse> approve(ApproveNgayNghiRequest request) {
        // Validate business rules
        request.validateBusinessRules();
        
        // Get current user (Admin)
        NguoiDung currentUser = getCurrentUser();
        
        List<NgayNghiResponse> results = new ArrayList<>();
        
        for (Integer nghiID : request.getNghiIDs()) {
            // Tìm yêu cầu
            BacSiNgayNghi entity = ngayNghiRepository.findById(nghiID)
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy yêu cầu nghỉ với ID: " + nghiID
                    ));
            
            // Check status
            if (entity.getTrangThai() != TrangThaiNghi.CHO_DUYET) {
                throw new BadRequestException(
                    String.format("Yêu cầu #%d không ở trạng thái chờ duyệt", nghiID)
                );
            }
            
            // Process approval
            if (request.isApprove()) {
                // APPROVE
                entity.setTrangThai(TrangThaiNghi.DA_DUYET);
                entity.setNguoiDuyet(currentUser);
                entity.setNgayDuyet(LocalDateTime.now());
                
                // Trừ ngày phép nếu là PHEP_NAM
                if (entity.getLoaiNghiPhep() == LoaiNghiPhep.PHEP_NAM) {
                    deductAnnualLeave(entity.getBacSi(), entity);
                }
                
            } else {
                // REJECT
                entity.setTrangThai(TrangThaiNghi.TU_CHOI);
                entity.setNguoiDuyet(currentUser);
                entity.setNgayDuyet(LocalDateTime.now());
                entity.setLyDoTuChoi(request.getLyDoTuChoi());
            }
            
            // Lưu
            BacSiNgayNghi updated = ngayNghiRepository.save(entity);
            results.add(NgayNghiResponse.fromEntity(updated));
        }
        
        return results;
    }
    
    /**
     * Bác sĩ/Admin hủy yêu cầu nghỉ
     * 
     * @param id NghiID
     * @return NgayNghiResponse
     */
    public NgayNghiResponse cancel(Integer id) {
        BacSiNgayNghi entity = ngayNghiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Không tìm thấy yêu cầu nghỉ với ID: " + id
                ));
        
        // Check status: CHỈ cho phép hủy khi CHO_DUYET hoặc DA_DUYET
        if (entity.getTrangThai() != TrangThaiNghi.CHO_DUYET &&
            entity.getTrangThai() != TrangThaiNghi.DA_DUYET) {
            throw new BadRequestException(
                "Chỉ có thể hủy yêu cầu đang chờ duyệt hoặc đã duyệt"
            );
        }
        
        // Nếu đã duyệt và là PHEP_NAM thì hoàn lại ngày phép
        if (entity.getTrangThai() == TrangThaiNghi.DA_DUYET &&
            entity.getLoaiNghiPhep() == LoaiNghiPhep.PHEP_NAM) {
            refundAnnualLeave(entity.getBacSi(), entity);
        }
        
        // Update status
        entity.setTrangThai(TrangThaiNghi.HUY);
        
        // Lưu
        BacSiNgayNghi updated = ngayNghiRepository.save(entity);
        
        return NgayNghiResponse.fromEntity(updated);
    }
    
    /**
     * Xóa yêu cầu nghỉ (Hard delete)
     * CHỈ cho phép xóa khi status = CHO_DUYET hoặc TU_CHOI hoặc HUY
     * 
     * @param id NghiID
     */
    public void delete(Integer id) {
        BacSiNgayNghi entity = ngayNghiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Không tìm thấy yêu cầu nghỉ với ID: " + id
                ));
        
        // Check status: Không cho phép xóa yêu cầu đã duyệt
        if (entity.getTrangThai() == TrangThaiNghi.DA_DUYET) {
            throw new BadRequestException(
                "Không thể xóa yêu cầu đã được duyệt. Vui lòng hủy yêu cầu trước."
            );
        }
        
        // Hard delete
        ngayNghiRepository.delete(entity);
    }
    
    // ===== PRIVATE HELPER METHODS =====
    
    /**
     * Get current user from Security Context
     */
    private NguoiDung getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getNguoiDung();
        }
        throw new UnauthorizedException("Bạn chưa đăng nhập");
    }
    
    /**
     * Check conflict với yêu cầu đã được duyệt
     */
    private void checkConflict(BacSi bacSi, CreateNgayNghiRequest request) {
        // Get approved leaves
        // Use a large date range to include all leaves
        LocalDateTime startDate = LocalDateTime.now().minusYears(1);
        LocalDateTime endDate = LocalDateTime.now().plusYears(5);
        
        List<BacSiNgayNghi> approvedLeaves = ngayNghiRepository.findApprovedLeavesInRange(
            bacSi.getBacSiID(),
            startDate.toLocalDate(),
            endDate.toLocalDate()
        );
        
        for (BacSiNgayNghi approved : approvedLeaves) {
            // Logic check conflict tùy vào loaiNghi
            boolean conflict = false;
            
            switch (request.getLoaiNghi()) {
                case NGAY_CU_THE:
                    // Conflict nếu cùng ngày
                    if (approved.getNgayNghiCuThe() != null &&
                        approved.getNgayNghiCuThe().equals(request.getNgayNghiCuThe())) {
                        conflict = true;
                    }
                    break;
                    
                case CA_CU_THE:
                    // Conflict nếu cùng ngày + cùng ca
                    if (approved.getNgayNghiCuThe() != null &&
                        approved.getNgayNghiCuThe().equals(request.getNgayNghiCuThe()) &&
                        (approved.getCa() == null || approved.getCa() == request.getCa())) {
                        conflict = true;
                    }
                    break;
                    
                case CA_HANG_TUAN:
                    // Conflict nếu cùng thứ + cùng ca
                    if (approved.getThuTrongTuan() != null &&
                        approved.getThuTrongTuan().equals(request.getThuTrongTuan()) &&
                        (approved.getCa() == null || approved.getCa() == request.getCa())) {
                        conflict = true;
                    }
                    break;
            }
            
            if (conflict) {
                throw new ConflictException(
                    "Yêu cầu nghỉ bị trùng với lịch nghỉ đã được duyệt: " + request.getMoTaThoiGianNghi()
                );
            }
        }
    }
    
    /**
     * Check số ngày phép còn lại
     */
    private void checkSoNgayPhep(BacSi bacSi) {
        Integer conLai = bacSi.getSoNgayPhepNam() - bacSi.getSoNgayPhepDaSuDung();
        
        if (conLai <= 0) {
            throw new BadRequestException(
                String.format("Không đủ số ngày phép. Còn lại: %d ngày", conLai)
            );
        }
    }
    
    /**
     * Trừ ngày phép khi approve PHEP_NAM
     */
    private void deductAnnualLeave(BacSi bacSi, BacSiNgayNghi ngayNghi) {
        // Tính số ngày nghỉ
        int soNgayNghi = calculateLeaveDays(ngayNghi);
        
        // Trừ
        Integer daSuDung = bacSi.getSoNgayPhepDaSuDung() + soNgayNghi;
        bacSi.setSoNgayPhepDaSuDung(daSuDung);
        
        // Lưu
        bacSiRepository.save(bacSi);
    }
    
    /**
     * Hoàn lại ngày phép khi cancel PHEP_NAM đã duyệt
     */
    private void refundAnnualLeave(BacSi bacSi, BacSiNgayNghi ngayNghi) {
        // Tính số ngày nghỉ
        int soNgayNghi = calculateLeaveDays(ngayNghi);
        
        // Hoàn lại
        Integer daSuDung = bacSi.getSoNgayPhepDaSuDung() - soNgayNghi;
        if (daSuDung < 0) daSuDung = 0;
        bacSi.setSoNgayPhepDaSuDung(daSuDung);
        
        // Lưu
        bacSiRepository.save(bacSi);
    }
    
    /**
     * Tính số ngày nghỉ (để trừ/hoàn phép)
     * - CA_CU_THE hoặc CA_HANG_TUAN: 0.5 ngày
     * - NGAY_CU_THE: 1 ngày
     */
    private int calculateLeaveDays(BacSiNgayNghi ngayNghi) {
        switch (ngayNghi.getLoaiNghi()) {
            case NGAY_CU_THE:
                return 1;
            case CA_CU_THE:
            case CA_HANG_TUAN:
                return ngayNghi.getCa() == null ? 1 : 0; // Nếu ca = null thì 1 ngày, có ca thì 0.5 → tạm để 0
            default:
                return 0;
        }
    }
    
}

