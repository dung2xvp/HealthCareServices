package org.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO cho response bác sĩ (thông tin chi tiết đầy đủ)
 * Dùng cho trang chi tiết bác sĩ
 * 
 * Kế thừa từ BacSiResponse và thêm các thông tin chi tiết
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BacSiDetailResponse extends BacSiResponse {
    
    // Thông tin chi tiết nghề nghiệp
    private String quaTrinhDaoTao;
    private String kinhNghiemLamViec;
    private String thanhTich;
    private String chungChi;
    
    // Thông tin mở rộng từ ChuyenKhoa
    private String moTaChuyenKhoa;
    
    // Thông tin mở rộng từ TrinhDo
    private String moTaTrinhDo;
    
    // Thống kê (có thể thêm sau)
    private Integer tongLichKham; // Tổng số lịch đã khám
    private Integer lichDaHoanThanh; // Số lịch hoàn thành
    private Double danhGiaTrungBinh; // Rating trung bình (có thể thêm sau)
}

