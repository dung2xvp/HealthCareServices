package org.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.example.demo.dto.request.BacSiRequest;
import org.example.demo.dto.request.CreateDoctorAccountRequest;
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
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    
    /**
     * COMBINED API: Tạo tài khoản bác sĩ (NguoiDung + BacSi) trong 1 transaction
     * Admin only
     * Logic:
     * 1. Kiểm tra email đã tồn tại chưa
     * 2. Tạo NguoiDung với VaiTro = BacSi
     * 3. Tạo BacSi và link với NguoiDung
     * 4. Auto set giá khám từ TrinhDo nếu không có giá custom
     */
    @Transactional
    public BacSiResponse createAccount(CreateDoctorAccountRequest request) {
        // 1. Kiểm tra email đã tồn tại chưa
        if (nguoiDungRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email đã được sử dụng");
        }
        
        // 2. Kiểm tra số điện thoại đã tồn tại chưa
        if (nguoiDungRepository.existsBySoDienThoai(request.getSoDienThoai())) {
            throw new ConflictException("Số điện thoại đã được sử dụng");
        }
        
        // 3. Kiểm tra ChuyenKhoa
        ChuyenKhoa chuyenKhoa = chuyenKhoaRepository.findById(request.getChuyenKhoaID())
                .orElseThrow(() -> new ResourceNotFoundException("Chuyên khoa không tồn tại"));
        
        // 4. Kiểm tra TrinhDo
        TrinhDo trinhDo = trinhDoRepository.findById(request.getTrinhDoID())
                .orElseThrow(() -> new ResourceNotFoundException("Trình độ không tồn tại"));
        
        // 5. Tạo NguoiDung
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setHoTen(request.getHoTen());
        nguoiDung.setEmail(request.getEmail());
        nguoiDung.setMatKhau(passwordEncoder.encode(request.getPassword())); // Hash password
        nguoiDung.setSoDienThoai(request.getSoDienThoai());
        nguoiDung.setDiaChi(request.getDiaChi());
        nguoiDung.setNgaySinh(request.getNgaySinh());
        nguoiDung.setGioiTinh(request.getGioiTinh());
        nguoiDung.setVaiTro(VaiTro.BacSi); // Tự động set vai trò
        nguoiDung.setTrangThai(true);
        nguoiDung.setIsDeleted(false);
        nguoiDung.setBadPoint(0);
        
        // Lưu NguoiDung trước
        NguoiDung savedNguoiDung = nguoiDungRepository.save(nguoiDung);
        
        // 6. Tạo BacSi
        BacSi bacSi = new BacSi();
        bacSi.setNguoiDung(savedNguoiDung);
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
        bacSi.setIsDeleted(false);
        
        // 7. Set giá khám: TỰ ĐỘNG lấy từ TrinhDo (không cho custom)
        bacSi.setGiaKham(trinhDo.getGiaKham());
        
        // 8. Lưu BacSi
        BacSi savedBacSi = bacSiRepository.save(bacSi);
        
        // 9. Convert sang Response
        return convertToResponse(savedBacSi);
    }
    
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
        
        // 7. Set giá khám: TỰ ĐỘNG lấy từ TrinhDo (không cho custom)
        bacSi.setGiaKham(trinhDo.getGiaKham());
        
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
            
            // TỰ ĐỘNG update giá theo TrinhDo mới (không cho custom)
            bacSi.setGiaKham(trinhDo.getGiaKham());
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
        
        // 5. ⚠️ GIÁ KHÁM KHÔNG CẬP NHẬT RIÊNG
        // Giá khám chỉ thay đổi khi đổi TrinhDo (đã xử lý ở bước 3)
        
        // 6. Lưu vào DB
        BacSi updated = bacSiRepository.save(bacSi);
        
        return convertToResponse(updated);
    }
    
    /**
     * Xóa bác sĩ (soft delete)
     * Cascade: Xóa mềm cả BacSi và NguoiDung
     */
    @Transactional
    public void delete(Integer id) {
        BacSi bacSi = bacSiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bác sĩ không tồn tại"));
        
        // Soft delete BacSi
        bacSi.setIsDeleted(true);
        bacSi.setTrangThaiCongViec(false); // Tắt trạng thái làm việc
        bacSiRepository.save(bacSi);
        
        // Cascade: Soft delete NguoiDung
        NguoiDung nguoiDung = bacSi.getNguoiDung();
        nguoiDung.setIsDeleted(true);
        nguoiDung.setTrangThai(false); // Cũng disable tài khoản
        nguoiDungRepository.save(nguoiDung);
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
     * Khôi phục bác sĩ đã xóa (Restore)
     * Cascade: Khôi phục cả BacSi và NguoiDung
     */
    @Transactional
    public BacSiResponse restore(Integer id) {
        BacSi bacSi = bacSiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bác sĩ không tồn tại"));
        
        // Kiểm tra bác sĩ có bị xóa không
        if (!bacSi.getIsDeleted()) {
            throw new BadRequestException("Bác sĩ chưa bị xóa, không cần khôi phục");
        }
        
        // Restore BacSi
        bacSi.setIsDeleted(false);
        bacSi.setTrangThaiCongViec(true); // Bật lại trạng thái làm việc
        bacSiRepository.save(bacSi);
        
        // Cascade: Restore NguoiDung
        NguoiDung nguoiDung = bacSi.getNguoiDung();
        nguoiDung.setIsDeleted(false);
        nguoiDung.setTrangThai(true); // Enable tài khoản
        nguoiDungRepository.save(nguoiDung);
        
        return convertToResponse(bacSi);
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

