package org.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.example.demo.dto.request.BacSiRequest;
import org.example.demo.dto.response.BacSiDetailResponse;
import org.example.demo.dto.response.BacSiResponse;
import org.example.demo.entity.BacSi;
import org.example.demo.entity.ChuyenKhoa;
import org.example.demo.entity.NguoiDung;
import org.example.demo.entity.TrinhDo;
import org.example.demo.enums.VaiTro;
import org.example.demo.exception.BadRequestException;
import org.example.demo.exception.ConflictException;
import org.example.demo.exception.ResourceNotFoundException;
import org.example.demo.repository.BacSiRepository;
import org.example.demo.repository.ChuyenKhoaRepository;
import org.example.demo.repository.NguoiDungRepository;
import org.example.demo.repository.TrinhDoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý logic nghiệp vụ cho Bác Sĩ
 */
@Service
@RequiredArgsConstructor
public class BacSiService {
    
    private final BacSiRepository bacSiRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final ChuyenKhoaRepository chuyenKhoaRepository;
    private final TrinhDoRepository trinhDoRepository;
    
    /**
     * Tạo mới bác sĩ (Admin only)
     * Logic:
     * 1. Kiểm tra NguoiDung có tồn tại và có VaiTro = BacSi
     * 2. Kiểm tra NguoiDung đã là bác sĩ chưa (tránh duplicate)
     * 3. Kiểm tra ChuyenKhoa, TrinhDo có tồn tại
     * 4. Nếu không có giaKham → lấy từ TrinhDo
     */
    @Transactional
    public BacSiResponse create(BacSiRequest request) {
        // 1. Kiểm tra NguoiDung
        NguoiDung nguoiDung = nguoiDungRepository.findById(request.getNguoiDungID())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        
        // 2. Kiểm tra VaiTro phải là BacSi
        if (!nguoiDung.getVaiTro().equals(VaiTro.BacSi)) {
            throw new BadRequestException("Người dùng này không có vai trò Bác Sĩ");
        }
        
        // 3. Kiểm tra user này đã là bác sĩ chưa
        if (bacSiRepository.existsById(nguoiDung.getNguoiDungID())) {
            throw new ConflictException("Người dùng này đã là bác sĩ");
        }
        
        // 4. Kiểm tra ChuyenKhoa
        ChuyenKhoa chuyenKhoa = chuyenKhoaRepository.findById(request.getChuyenKhoaID())
                .orElseThrow(() -> new ResourceNotFoundException("Chuyên khoa không tồn tại"));
        
        // 5. Kiểm tra TrinhDo
        TrinhDo trinhDo = trinhDoRepository.findById(request.getTrinhDoID())
                .orElseThrow(() -> new ResourceNotFoundException("Trình độ không tồn tại"));
        
        // 6. Tạo BacSi entity
        BacSi bacSi = new BacSi();
        bacSi.setNguoiDung(nguoiDung);
        bacSi.setChuyenKhoa(chuyenKhoa);
        bacSi.setTrinhDo(trinhDo);
        bacSi.setSoNamKinhNghiem(request.getSoNamKinhNghiem());
        bacSi.setGioiThieu(request.getGioiThieu());
        bacSi.setQuaTrinhDaoTao(request.getQuaTrinhDaoTao());
        bacSi.setKinhNghiemLamViec(request.getKinhNghiemLamViec());
        bacSi.setThanhTich(request.getThanhTich());
        bacSi.setChungChi(request.getChungChi());
        bacSi.setSoBenhNhanToiDaMotNgay(request.getSoBenhNhanToiDaMotNgay());
        bacSi.setThoiGianKhamMotCa(request.getThoiGianKhamMotCa());
        bacSi.setTrangThaiCongViec(request.getTrangThaiCongViec());
        
        // 7. Set giá khám: Nếu request có giá → dùng giá đó, không thì lấy từ TrinhDo
        if (request.getGiaKham() != null) {
            bacSi.setGiaKham(request.getGiaKham());
        } else {
            bacSi.setGiaKham(trinhDo.getGiaKham());
        }
        
        // 8. Lưu vào DB
        BacSi saved = bacSiRepository.save(bacSi);
        
        // 9. Convert sang Response
        return convertToResponse(saved);
    }
    
    /**
     * Lấy danh sách bác sĩ (có phân trang) - CHỈ lấy bác sĩ chưa bị xóa
     */
    @Transactional(readOnly = true)
    public Page<BacSiResponse> getAll(Pageable pageable) {
        return bacSiRepository.findAllByIsDeleted(false, pageable)
                .map(this::convertToResponse);
    }
    
    /**
     * Lấy chi tiết bác sĩ
     */
    @Transactional(readOnly = true)
    public BacSiDetailResponse getById(Integer id) {
        BacSi bacSi = bacSiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bác sĩ không tồn tại"));
        
        return convertToDetailResponse(bacSi);
    }
    
    /**
     * Lấy bác sĩ theo chuyên khoa
     */
    @Transactional(readOnly = true)
    public List<BacSiResponse> getByChuyenKhoa(Integer chuyenKhoaId) {
        // Kiểm tra chuyên khoa có tồn tại
        if (!chuyenKhoaRepository.existsById(chuyenKhoaId)) {
            throw new ResourceNotFoundException("Chuyên khoa không tồn tại");
        }
        
        // Chỉ lấy bác sĩ đang làm việc VÀ chưa bị xóa
        return bacSiRepository.findByChuyenKhoa_ChuyenKhoaIDAndTrangThaiCongViecAndIsDeleted(
                        chuyenKhoaId, true, false)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Tìm kiếm bác sĩ theo keyword (tên hoặc chuyên khoa)
     */
    @Transactional(readOnly = true)
    public Page<BacSiResponse> search(String keyword, Pageable pageable) {
        return bacSiRepository.searchDoctors(keyword, pageable)
                .map(this::convertToResponse);
    }
    
    /**
     * Cập nhật thông tin bác sĩ
     * Admin: Cập nhật được tất cả
     * BacSi: Chỉ cập nhật được thông tin của mình
     */
    @Transactional
    public BacSiResponse update(Integer id, BacSiRequest request) {
        // 1. Kiểm tra bác sĩ có tồn tại
        BacSi bacSi = bacSiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bác sĩ không tồn tại"));
        
        // 2. Cập nhật ChuyenKhoa nếu thay đổi
        if (!bacSi.getChuyenKhoa().getChuyenKhoaID().equals(request.getChuyenKhoaID())) {
            ChuyenKhoa chuyenKhoa = chuyenKhoaRepository.findById(request.getChuyenKhoaID())
                    .orElseThrow(() -> new ResourceNotFoundException("Chuyên khoa không tồn tại"));
            bacSi.setChuyenKhoa(chuyenKhoa);
        }
        
        // 3. Cập nhật TrinhDo nếu thay đổi
        if (!bacSi.getTrinhDo().getTrinhDoID().equals(request.getTrinhDoID())) {
            TrinhDo trinhDo = trinhDoRepository.findById(request.getTrinhDoID())
                    .orElseThrow(() -> new ResourceNotFoundException("Trình độ không tồn tại"));
            bacSi.setTrinhDo(trinhDo);
            
            // Nếu không có giá custom → update giá theo TrinhDo mới
            if (request.getGiaKham() == null) {
                bacSi.setGiaKham(trinhDo.getGiaKham());
            }
        }
        
        // 4. Cập nhật các field khác
        bacSi.setSoNamKinhNghiem(request.getSoNamKinhNghiem());
        bacSi.setGioiThieu(request.getGioiThieu());
        bacSi.setQuaTrinhDaoTao(request.getQuaTrinhDaoTao());
        bacSi.setKinhNghiemLamViec(request.getKinhNghiemLamViec());
        bacSi.setThanhTich(request.getThanhTich());
        bacSi.setChungChi(request.getChungChi());
        bacSi.setSoBenhNhanToiDaMotNgay(request.getSoBenhNhanToiDaMotNgay());
        bacSi.setThoiGianKhamMotCa(request.getThoiGianKhamMotCa());
        bacSi.setTrangThaiCongViec(request.getTrangThaiCongViec());
        
        // 5. Cập nhật giá nếu có
        if (request.getGiaKham() != null) {
            bacSi.setGiaKham(request.getGiaKham());
        }
        
        // 6. Lưu vào DB
        BacSi updated = bacSiRepository.save(bacSi);
        
        return convertToResponse(updated);
    }
    
    /**
     * Xóa bác sĩ (soft delete)
     */
    @Transactional
    public void delete(Integer id) {
        BacSi bacSi = bacSiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bác sĩ không tồn tại"));
        
        // Soft delete
        bacSi.setIsDeleted(true);
        bacSi.setTrangThaiCongViec(false); // Tắt trạng thái làm việc
        bacSiRepository.save(bacSi);
    }
    
    /**
     * Bật/Tắt trạng thái làm việc của bác sĩ
     */
    @Transactional
    public BacSiResponse toggleStatus(Integer id) {
        BacSi bacSi = bacSiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bác sĩ không tồn tại"));
        
        // Toggle trạng thái
        bacSi.setTrangThaiCongViec(!bacSi.getTrangThaiCongViec());
        BacSi updated = bacSiRepository.save(bacSi);
        
        return convertToResponse(updated);
    }
    
    /**
     * Lấy top bác sĩ có kinh nghiệm cao (CHỈ lấy bác sĩ chưa bị xóa)
     */
    @Transactional(readOnly = true)
    public List<BacSiResponse> getTopExperienced() {
        return bacSiRepository.findTop10ByTrangThaiCongViecAndIsDeletedOrderBySoNamKinhNghiemDesc(
                        true, false)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Convert BacSi entity sang BacSiResponse (thông tin cơ bản)
     */
    private BacSiResponse convertToResponse(BacSi bacSi) {
        BacSiResponse response = new BacSiResponse();
        
        // Thông tin cơ bản
        response.setBacSiID(bacSi.getBacSiID());
        
        // Thông tin từ NguoiDung
        NguoiDung nguoiDung = bacSi.getNguoiDung();
        response.setHoTen(nguoiDung.getHoTen());
        response.setEmail(nguoiDung.getEmail());
        response.setSoDienThoai(nguoiDung.getSoDienThoai());
        response.setAvatarUrl(nguoiDung.getAvatarUrl());
        response.setGioiTinh(nguoiDung.getGioiTinh());
        response.setNgaySinh(nguoiDung.getNgaySinh());
        
        // Thông tin từ ChuyenKhoa
        ChuyenKhoa chuyenKhoa = bacSi.getChuyenKhoa();
        response.setChuyenKhoaID(chuyenKhoa.getChuyenKhoaID());
        response.setTenChuyenKhoa(chuyenKhoa.getTenChuyenKhoa());
        
        // Thông tin từ TrinhDo
        TrinhDo trinhDo = bacSi.getTrinhDo();
        response.setTrinhDoID(trinhDo.getTrinhDoID());
        response.setTenTrinhDo(trinhDo.getTenTrinhDo());
        
        // Thông tin nghề nghiệp
        response.setSoNamKinhNghiem(bacSi.getSoNamKinhNghiem());
        response.setGioiThieu(bacSi.getGioiThieu());
        response.setGiaKham(bacSi.getGiaKham());
        response.setTrangThaiCongViec(bacSi.getTrangThaiCongViec());
        response.setSoBenhNhanToiDaMotNgay(bacSi.getSoBenhNhanToiDaMotNgay());
        response.setThoiGianKhamMotCa(bacSi.getThoiGianKhamMotCa());
        
        return response;
    }
    
    /**
     * Convert BacSi entity sang BacSiDetailResponse (thông tin đầy đủ)
     */
    private BacSiDetailResponse convertToDetailResponse(BacSi bacSi) {
        BacSiDetailResponse response = new BacSiDetailResponse();
        
        // Thông tin cơ bản (từ BacSiResponse)
        response.setBacSiID(bacSi.getBacSiID());
        
        // Thông tin từ NguoiDung
        NguoiDung nguoiDung = bacSi.getNguoiDung();
        response.setHoTen(nguoiDung.getHoTen());
        response.setEmail(nguoiDung.getEmail());
        response.setSoDienThoai(nguoiDung.getSoDienThoai());
        response.setAvatarUrl(nguoiDung.getAvatarUrl());
        response.setGioiTinh(nguoiDung.getGioiTinh());
        response.setNgaySinh(nguoiDung.getNgaySinh());
        
        // Thông tin từ ChuyenKhoa
        ChuyenKhoa chuyenKhoa = bacSi.getChuyenKhoa();
        response.setChuyenKhoaID(chuyenKhoa.getChuyenKhoaID());
        response.setTenChuyenKhoa(chuyenKhoa.getTenChuyenKhoa());
        response.setMoTaChuyenKhoa(chuyenKhoa.getMoTa()); // Thêm mô tả
        
        // Thông tin từ TrinhDo
        TrinhDo trinhDo = bacSi.getTrinhDo();
        response.setTrinhDoID(trinhDo.getTrinhDoID());
        response.setTenTrinhDo(trinhDo.getTenTrinhDo());
        response.setMoTaTrinhDo(trinhDo.getMoTa()); // Thêm mô tả
        
        // Thông tin nghề nghiệp cơ bản
        response.setSoNamKinhNghiem(bacSi.getSoNamKinhNghiem());
        response.setGioiThieu(bacSi.getGioiThieu());
        response.setGiaKham(bacSi.getGiaKham());
        response.setTrangThaiCongViec(bacSi.getTrangThaiCongViec());
        response.setSoBenhNhanToiDaMotNgay(bacSi.getSoBenhNhanToiDaMotNgay());
        response.setThoiGianKhamMotCa(bacSi.getThoiGianKhamMotCa());
        
        // Thông tin chi tiết (chỉ có trong DetailResponse)
        response.setQuaTrinhDaoTao(bacSi.getQuaTrinhDaoTao());
        response.setKinhNghiemLamViec(bacSi.getKinhNghiemLamViec());
        response.setThanhTich(bacSi.getThanhTich());
        response.setChungChi(bacSi.getChungChi());
        
        // Thống kê (tạm thời set null, sẽ implement sau)
        response.setTongLichKham(null);
        response.setLichDaHoanThanh(null);
        response.setDanhGiaTrungBinh(null);
        
        return response;
    }
}

