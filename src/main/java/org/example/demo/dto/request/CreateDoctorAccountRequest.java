package org.example.demo.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO cho API tạo tài khoản bác sĩ (Combined API)
 * Tạo cả NguoiDung + BacSi trong 1 transaction
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDoctorAccountRequest {
    
    // ============= THÔNG TIN TÀI KHOẢN (NguoiDung) =============
    
    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ tên phải từ 2-100 ký tự")
    private String hoTen;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 50, message = "Mật khẩu phải từ 6-50 ký tự")
    private String password;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String soDienThoai;
    
    @Size(max = 200, message = "Địa chỉ không được quá 200 ký tự")
    private String diaChi;
    
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate ngaySinh;
    
    @Min(value = 0, message = "Giới tính không hợp lệ (0=Nữ, 1=Nam, 2=Khác)")
    @Max(value = 2, message = "Giới tính không hợp lệ (0=Nữ, 1=Nam, 2=Khác)")
    private Integer gioiTinh = 1;
    
    // ============= THÔNG TIN BÁC SĨ =============
    
    @NotNull(message = "Chuyên khoa không được để trống")
    private Integer chuyenKhoaID;
    
    @NotNull(message = "Trình độ không được để trống")
    private Integer trinhDoID;
    
    @Min(value = 0, message = "Số năm kinh nghiệm phải >= 0")
    @Max(value = 50, message = "Số năm kinh nghiệm không hợp lý")
    private Integer soNamKinhNghiem = 0;
    
    @Size(max = 1000, message = "Giới thiệu không được quá 1000 ký tự")
    private String gioiThieu;
    
    @Size(max = 2000, message = "Quá trình đào tạo không được quá 2000 ký tự")
    private String quaTrinhDaoTao;
    
    @Size(max = 2000, message = "Kinh nghiệm làm việc không được quá 2000 ký tự")
    private String kinhNghiemLamViec;
    
    @Size(max = 1000, message = "Thành tích không được quá 1000 ký tự")
    private String thanhTich;
    
    @Size(max = 1000, message = "Chứng chỉ không được quá 1000 ký tự")
    private String chungChi;
    
    /**
     * ⚠️ GIÁ KHÁM TỰ ĐỘNG LẤY TỪ TRÌNH ĐỘ
     * Không cần nhập giá khám, hệ thống sẽ tự động lấy từ TrinhDo
     */
    
    @Min(value = 1, message = "Số bệnh nhân tối đa phải >= 1")
    @Max(value = 100, message = "Số bệnh nhân tối đa không hợp lý")
    private Integer soBenhNhanToiDaMotNgay = 20;
    
    @Min(value = 5, message = "Thời gian khám phải >= 5 phút")
    @Max(value = 120, message = "Thời gian khám không hợp lý")
    private Integer thoiGianKhamMotCa = 30; // Mặc định 30 phút
    
    /**
     * Trạng thái công việc
     * - true: Đang làm việc (có thể nhận lịch khám)
     * - false: Tạm nghỉ
     */
    private Boolean trangThaiCongViec = true;
}

