package org.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO Response thống kê lịch khám
 * 
 * Use cases:
 * - Admin dashboard: Thống kê tổng quan hệ thống
 * - Doctor dashboard: Thống kê lịch khám của bác sĩ
 * - Patient dashboard: Thống kê lịch khám của bệnh nhân
 * 
 * Structure:
 * {
 *   "totalBookings": 150,
 *   "pendingApproval": 5,
 *   "confirmed": 10,
 *   "completed": 120,
 *   "cancelled": 10,
 *   "noShow": 5,
 *   "totalRevenue": 45000000,
 *   "averageRating": 4.5
 * }
 * 
 * @author Healthcare System Team
 * @version 1.0 - Phase 2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response thống kê lịch khám")
public class BookingStatisticsResponse {
    
    // ===== THỐNG KÊ SỐ LƯỢNG =====
    
    @Schema(description = "Tổng số lịch khám", example = "150")
    private Long totalBookings;
    
    @Schema(description = "Chờ bác sĩ xác nhận", example = "5")
    private Long pendingApproval;
    
    @Schema(description = "Chờ thanh toán", example = "3")
    private Long pendingPayment;
    
    @Schema(description = "Đã xác nhận", example = "10")
    private Long confirmed;
    
    @Schema(description = "Đang khám", example = "2")
    private Long inProgress;
    
    @Schema(description = "Hoàn thành", example = "120")
    private Long completed;
    
    @Schema(description = "Đã hủy", example = "10")
    private Long cancelled;
    
    @Schema(description = "Không đến khám (no-show)", example = "5")
    private Long noShow;
    
    @Schema(description = "Bác sĩ từ chối", example = "3")
    private Long rejected;
    
    // ===== THỐNG KÊ THEO THỜI GIAN =====
    
    @Schema(description = "Lịch khám hôm nay", example = "8")
    private Long todayBookings;
    
    @Schema(description = "Lịch khám tuần này", example = "25")
    private Long thisWeekBookings;
    
    @Schema(description = "Lịch khám tháng này", example = "60")
    private Long thisMonthBookings;
    
    // ===== THỐNG KÊ DOANH THU =====
    
    @Schema(description = "Tổng doanh thu", example = "45000000")
    private BigDecimal totalRevenue;
    
    @Schema(description = "Doanh thu hôm nay", example = "2400000")
    private BigDecimal todayRevenue;
    
    @Schema(description = "Doanh thu tuần này", example = "7500000")
    private BigDecimal thisWeekRevenue;
    
    @Schema(description = "Doanh thu tháng này", example = "18000000")
    private BigDecimal thisMonthRevenue;
    
    @Schema(description = "Tổng doanh thu (formatted)", example = "45.000.000 đ")
    private String totalRevenueDisplay;
    
    // ===== THỐNG KÊ ĐÁNH GIÁ =====
    
    @Schema(description = "Rating trung bình (1-5 sao)", example = "4.5")
    private Double averageRating;
    
    @Schema(description = "Tổng số đánh giá", example = "95")
    private Long totalRatings;
    
    @Schema(description = "5 sao", example = "60")
    private Long fiveStars;
    
    @Schema(description = "4 sao", example = "25")
    private Long fourStars;
    
    @Schema(description = "3 sao", example = "7")
    private Long threeStars;
    
    @Schema(description = "2 sao", example = "2")
    private Long twoStars;
    
    @Schema(description = "1 sao", example = "1")
    private Long oneStar;
    
    // ===== THỐNG KÊ THANH TOÁN =====
    
    @Schema(description = "Đã thanh toán", example = "140")
    private Long paidBookings;
    
    @Schema(description = "Chưa thanh toán", example = "10")
    private Long unpaidBookings;
    
    @Schema(description = "Số lần hoàn tiền", example = "5")
    private Long refundCount;
    
    @Schema(description = "Tổng tiền hoàn", example = "1500000")
    private BigDecimal totalRefund;
    
    // ===== COMPUTED FIELDS =====
    
    @Schema(description = "Tỷ lệ hoàn thành (%)", example = "80.0")
    private Double completionRate;
    
    @Schema(description = "Tỷ lệ hủy (%)", example = "6.7")
    private Double cancellationRate;
    
    @Schema(description = "Tỷ lệ no-show (%)", example = "3.3")
    private Double noShowRate;
    
    @Schema(description = "Tỷ lệ đánh giá (%)", example = "79.2")
    private Double ratingRate;
    
    // ===== HELPER METHODS =====
    
    /**
     * Calculate computed fields
     */
    public void calculate() {
        if (totalBookings != null && totalBookings > 0) {
            this.completionRate = completed != null 
                    ? (completed * 100.0 / totalBookings) 
                    : 0.0;
                    
            this.cancellationRate = cancelled != null 
                    ? (cancelled * 100.0 / totalBookings) 
                    : 0.0;
                    
            this.noShowRate = noShow != null 
                    ? (noShow * 100.0 / totalBookings) 
                    : 0.0;
                    
            this.ratingRate = totalRatings != null 
                    ? (totalRatings * 100.0 / totalBookings) 
                    : 0.0;
        }
        
        if (totalRevenue != null) {
            this.totalRevenueDisplay = String.format("%,.0f đ", totalRevenue);
        }
    }
    
    /**
     * Factory method với giá trị mặc định 0
     */
    public static BookingStatisticsResponse empty() {
        return BookingStatisticsResponse.builder()
                .totalBookings(0L)
                .pendingApproval(0L)
                .pendingPayment(0L)
                .confirmed(0L)
                .inProgress(0L)
                .completed(0L)
                .cancelled(0L)
                .noShow(0L)
                .rejected(0L)
                .todayBookings(0L)
                .thisWeekBookings(0L)
                .thisMonthBookings(0L)
                .totalRevenue(BigDecimal.ZERO)
                .todayRevenue(BigDecimal.ZERO)
                .thisWeekRevenue(BigDecimal.ZERO)
                .thisMonthRevenue(BigDecimal.ZERO)
                .averageRating(0.0)
                .totalRatings(0L)
                .fiveStars(0L)
                .fourStars(0L)
                .threeStars(0L)
                .twoStars(0L)
                .oneStar(0L)
                .paidBookings(0L)
                .unpaidBookings(0L)
                .refundCount(0L)
                .totalRefund(BigDecimal.ZERO)
                .completionRate(0.0)
                .cancellationRate(0.0)
                .noShowRate(0.0)
                .ratingRate(0.0)
                .build();
    }
}

