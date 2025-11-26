package org.example.demo.repository;

import org.example.demo.entity.NguoiDung;
import org.example.demo.enums.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho NguoiDung (User)
 * Xử lý: Authentication, User Management
 */
@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, Integer> {
    
    /**
     * Tìm user theo email (dùng cho LOGIN)
     * @return Optional<NguoiDung> - có thể null nếu không tìm thấy
     */
    Optional<NguoiDung> findByEmail(String email);
    
    /**
     * Kiểm tra email đã tồn tại chưa (dùng cho REGISTER)
     * @return true nếu email đã tồn tại
     */
    Boolean existsByEmail(String email);
    
    /**
     * Tìm user theo email VÀ trạng thái active
     * Dùng khi muốn lấy user đã verify email
     */
    Optional<NguoiDung> findByEmailAndTrangThai(String email, Boolean trangThai);
    
    /**
     * Lấy danh sách user theo vai trò
     * Ví dụ: Lấy tất cả bệnh nhân, tất cả bác sĩ
     */
    List<NguoiDung> findByVaiTro(VaiTro vaiTro);
    
    /**
     * Lấy danh sách user theo vai trò và trạng thái
     * Ví dụ: Lấy tất cả bệnh nhân đang active
     */
    List<NguoiDung> findByVaiTroAndTrangThai(VaiTro vaiTro, Boolean trangThai);
    
    /**
     * Tìm kiếm user theo tên (LIKE search)
     * Ví dụ: Tìm "Nguyễn" sẽ ra "Nguyễn Văn A", "Nguyễn Thị B"...
     */
    List<NguoiDung> findByHoTenContainingIgnoreCase(String hoTen);
}

