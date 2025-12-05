package org.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo.enums.CaLamViec;
import org.example.demo.enums.LoaiNghi;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Lịch làm việc thực tế của bác sĩ theo ngày/ca")
public class DoctorScheduleItemResponse {

    @Schema(description = "Ngày áp dụng", example = "2025-12-05")
    private LocalDate ngay;

    @Schema(description = "Thứ trong tuần (2-8)", example = "2")
    private Integer thu;

    @Schema(description = "Tên thứ", example = "Thứ 2")
    private String tenThu;

    @Schema(description = "Ca làm việc", example = "SANG")
    private CaLamViec ca;

    @Schema(description = "Tên ca", example = "Ca sáng")
    private String tenCa;

    @Schema(description = "Giờ bắt đầu", example = "08:00")
    private String gioBatDau;

    @Schema(description = "Giờ kết thúc", example = "12:00")
    private String gioKetThuc;

    @Schema(description = "Có đang nghỉ ca này không", example = "false")
    private Boolean isOnLeave;

    @Schema(description = "Loại nghỉ (nếu có)", example = "CA_CU_THE")
    private LoaiNghi loaiNghi;

    @Schema(description = "Ghi chú nghỉ (nếu có)")
    private String ghiChuNghi;

    @Schema(description = "Số slot đã đặt trong ca", example = "1")
    private Integer soSlotDaDat;

    @Schema(description = "Tổng số slot trong ca (tính theo bước cấu hình)", example = "8")
    private Integer tongSlot;

    @Schema(description = "Số slot còn trống trong ca", example = "5")
    private Integer slotConLai;

    @Schema(description = "Danh sách giờ đã đặt", example = "[\"08:00\", \"08:30\"]")
    private List<String> gioDaDat;

    @Schema(description = "Còn trống để đặt", example = "true")
    private Boolean available;

    public static String formatTime(LocalTime time) {
        if (time == null) return "";
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }
}


