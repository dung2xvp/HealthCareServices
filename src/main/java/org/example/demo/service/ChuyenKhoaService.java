package org.example.demo.service;

import org.example.demo.dto.request.ChuyenKhoaRequest;
import org.example.demo.dto.response.ChuyenKhoaResponse;
import org.example.demo.entity.ChuyenKhoa;
import org.example.demo.entity.CoSoYTe;
import org.example.demo.exception.ResourceNotFoundException;
import org.example.demo.repository.ChuyenKhoaRepository;
import org.example.demo.repository.CoSoYTeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChuyenKhoaService {

    @Autowired
    private ChuyenKhoaRepository chuyenKhoaRepository;
    
    @Autowired
    private CoSoYTeRepository coSoYTeRepository;

    /**
     * Tạo chuyên khoa mới
     * Tự động lấy cơ sở y tế đầu tiên trong hệ thống
     */
    public ChuyenKhoaResponse create(ChuyenKhoaRequest request) {
        // Tự động lấy cơ sở y tế đầu tiên (vì chỉ quản lý 1 cơ sở)
        CoSoYTe coSoYTe = coSoYTeRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cơ sở y tế trong hệ thống. Vui lòng tạo cơ sở y tế trước."));
        
        // Tạo entity mới
        ChuyenKhoa chuyenKhoa = new ChuyenKhoa();
        chuyenKhoa.setCoSoYTe(coSoYTe);
        chuyenKhoa.setTenChuyenKhoa(request.getTenChuyenKhoa());
        chuyenKhoa.setMoTa(request.getMoTa());
        chuyenKhoa.setAnhDaiDien(request.getAnhDaiDien());
        chuyenKhoa.setThuTuHienThi(request.getThuTuHienThi() != null ? request.getThuTuHienThi() : 0);
        chuyenKhoa.setIsDeleted(false);
        
        // Lưu vào database
        ChuyenKhoa saved = chuyenKhoaRepository.save(chuyenKhoa);
        
        return convertToResponse(saved);
    }

    /**
     * Lấy tất cả chuyên khoa (chưa bị xóa)
     */
    public List<ChuyenKhoaResponse> getAllChuyenKhoa() {
        return chuyenKhoaRepository.findAll()
                .stream()
                .filter(ck -> !ck.getIsDeleted())
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả chuyên khoa sắp xếp theo thứ tự hiển thị
     */
    public List<ChuyenKhoaResponse> getAllChuyenKhoaSorted() {
        return chuyenKhoaRepository.findAllByOrderByThuTuHienThiAsc()
                .stream()
                .filter(ck -> !ck.getIsDeleted())
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy chuyên khoa theo ID
     */
    public ChuyenKhoaResponse getById(Integer id) {
        ChuyenKhoa chuyenKhoa = chuyenKhoaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa với ID: " + id));
        
        if (chuyenKhoa.getIsDeleted()) {
            throw new ResourceNotFoundException("Chuyên khoa đã bị xóa");
        }
        
        return convertToResponse(chuyenKhoa);
    }

    /**
     * Lấy danh sách chuyên khoa theo cơ sở y tế
     */
    public List<ChuyenKhoaResponse> getByCoSoId(Integer coSoId) {
        return chuyenKhoaRepository.findByCoSoYTe_CoSoID(coSoId)
                .stream()
                .filter(ck -> !ck.getIsDeleted())
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm chuyên khoa theo tên
     */
    public List<ChuyenKhoaResponse> searchByName(String keyword) {
        return chuyenKhoaRepository.findByTenChuyenKhoaContainingIgnoreCase(keyword)
                .stream()
                .filter(ck -> !ck.getIsDeleted())
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật chuyên khoa
     */
    public ChuyenKhoaResponse update(Integer id, ChuyenKhoaRequest request) {
        // Tìm chuyên khoa cần update
        ChuyenKhoa chuyenKhoa = chuyenKhoaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa với ID: " + id));
        
        if (chuyenKhoa.getIsDeleted()) {
            throw new ResourceNotFoundException("Không thể cập nhật chuyên khoa đã bị xóa");
        }
        
        // Update các field (CoSoYTe giữ nguyên)
        chuyenKhoa.setTenChuyenKhoa(request.getTenChuyenKhoa());
        chuyenKhoa.setMoTa(request.getMoTa());
        chuyenKhoa.setAnhDaiDien(request.getAnhDaiDien());
        chuyenKhoa.setThuTuHienThi(request.getThuTuHienThi() != null ? request.getThuTuHienThi() : 0);
        
        // Lưu thay đổi
        ChuyenKhoa updated = chuyenKhoaRepository.save(chuyenKhoa);
        
        return convertToResponse(updated);
    }

    /**
     * Xóa mềm chuyên khoa
     */
    public void delete(Integer id) {
        ChuyenKhoa chuyenKhoa = chuyenKhoaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa với ID: " + id));
        
        if (chuyenKhoa.getIsDeleted()) {
            throw new ResourceNotFoundException("Chuyên khoa đã bị xóa trước đó");
        }
        
        // Soft delete
        chuyenKhoa.setIsDeleted(true);
        chuyenKhoa.setDeletedAt(LocalDateTime.now());
        
        chuyenKhoaRepository.save(chuyenKhoa);
    }

    /**
     * Helper method: Convert Entity -> Response DTO
     */
    private ChuyenKhoaResponse convertToResponse(ChuyenKhoa entity) {
        return new ChuyenKhoaResponse(
                entity.getChuyenKhoaID(),
                entity.getTenChuyenKhoa(),
                entity.getMoTa(),
                entity.getAnhDaiDien(),
                entity.getThuTuHienThi()
        );
    }
}
