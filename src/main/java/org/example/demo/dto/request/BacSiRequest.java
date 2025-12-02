package org.example.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request tạo/cập nhật bác sĩ
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BacSiRequest {
    
    /**
     * ID của NguoiDung đã tồn tại (với VaiTro = BacSi)
     * Bắt buộc khi tạo mới
     */
    @NotNull(message = "Người dùng ID không được để trống")
    private Integer nguoiDungID;
    
    /**
     * ID Chuyên khoa
     */
    @NotNull(message = "Chuyên khoa không được để trống")
    private Integer chuyenKhoaID;
    
    /**
     * ID Trình độ (Thạc sĩ, Tiến sĩ, PGS.TS...)
     */
    @NotNull(message = "Trình độ không được để trống")
    private Integer trinhDoID;
    
    /**
     * Số năm kinh nghiệm (0-50 năm)
     */
    @Min(value = 0, message = "Số năm kinh nghiệm phải >= 0")
    @Max(value = 50, message = "Số năm kinh nghiệm không hợp lý")
    private Integer soNamKinhNghiem = 0;
    
    /**
     * Giới thiệu về bác sĩ
     */
    @Size(max = 1000, message = "Giới thiệu không được vượt quá 1000 ký tự")
    private String gioiThieu;
    
    /**
     * Quá trình đào tạo
     */
    @Size(max = 2000, message = "Quá trình đào tạo không được vượt quá 2000 ký tự")
    private String quaTrinhDaoTao;
    
    /**
     * Kinh nghiệm làm việc
     */
    @Size(max = 2000, message = "Kinh nghiệm làm việc không được vượt quá 2000 ký tự")
    private String kinhNghiemLamViec;
    
    /**
     * Thành tích
     */
    @Size(max = 1000, message = "Thành tích không được vượt quá 1000 ký tự")
    private String thanhTich;
    
    /**
     * Chứng chỉ (có thể lưu JSON array hoặc comma-separated URLs)
     */
    @Size(max = 500, message = "Chứng chỉ không được vượt quá 500 ký tự")
    private String chungChi;
    
    /**
     * ⚠️ GIÁ KHÁM TỰ ĐỘNG LẤY TỪ TRÌNH ĐỘ
     * Không cần nhập giá khám, hệ thống sẽ tự động lấy từ TrinhDo
     * Khi thay đổi TrinhDo, giá khám sẽ tự động cập nhật theo
     */
    
    /**
     * Số bệnh nhân tối đa trong 1 ngày
     */
    @Min(value = 1, message = "Số bệnh nhân tối thiểu là 1")
    @Max(value = 100, message = "Số bệnh nhân tối đa là 100")
    private Integer soBenhNhanToiDaMotNgay = 20;
    
    /**
     * Thời gian khám mỗi ca (phút)
     */
    @Min(value = 15, message = "Thời gian khám tối thiểu là 15 phút")
    @Max(value = 120, message = "Thời gian khám tối đa là 120 phút")
    private Integer thoiGianKhamMotCa = 30;
    
    /**
     * Trạng thái công việc (đang làm việc hay không)
     */
    private Boolean trangThaiCongViec = true;
}
