package org.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO cho response bác sĩ (thông tin cơ bản)
 * Dùng cho danh sách, tìm kiếm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BacSiResponse {
    
    // Thông tin cơ bản từ BacSi
    private Integer bacSiID;
    
    // Thông tin từ NguoiDung
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String avatarUrl;
    private Integer gioiTinh;
    private LocalDate ngaySinh;
    
    // Thông tin từ ChuyenKhoa
    private Integer chuyenKhoaID;
    private String tenChuyenKhoa;
    
    // Thông tin từ TrinhDo
    private Integer trinhDoID;
    private String tenTrinhDo;
    
    // Thông tin nghề nghiệp
    private Integer soNamKinhNghiem;
    private String gioiThieu;
    private BigDecimal giaKham;
    private Boolean trangThaiCongViec;
    
    // Metadata
    private Integer soBenhNhanToiDaMotNgay;
    private Integer thoiGianKhamMotCa;
}
