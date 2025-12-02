package org.example.demo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO Response danh sách lịch khám (với pagination)
 * 
 * Structure:
 * {
 *   "bookings": [...],
 *   "currentPage": 0,
 *   "totalPages": 5,
 *   "totalElements": 48,
 *   "pageSize": 10,
 *   "hasNext": true,
 *   "hasPrevious": false
 * }
 * 
 * @author Healthcare System Team
 * @version 1.0 - Phase 2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response danh sách lịch khám (paginated)")
public class BookingListResponse {
    
    @Schema(description = "Danh sách lịch khám")
    private List<BookingResponse> bookings;
    
    // ===== PAGINATION =====
    
    @Schema(description = "Trang hiện tại (bắt đầu từ 0)", example = "0")
    private Integer currentPage;
    
    @Schema(description = "Tổng số trang", example = "5")
    private Integer totalPages;
    
    @Schema(description = "Tổng số phần tử", example = "48")
    private Long totalElements;
    
    @Schema(description = "Số phần tử mỗi trang", example = "10")
    private Integer pageSize;
    
    @Schema(description = "Còn trang sau không", example = "true")
    private Boolean hasNext;
    
    @Schema(description = "Còn trang trước không", example = "false")
    private Boolean hasPrevious;
    
    @Schema(description = "Là trang đầu tiên không", example = "true")
    private Boolean isFirst;
    
    @Schema(description = "Là trang cuối cùng không", example = "false")
    private Boolean isLast;
    
    // ===== FACTORY METHOD =====
    
    /**
     * Convert từ Spring Data Page
     */
    public static BookingListResponse from(Page<BookingResponse> page) {
        return BookingListResponse.builder()
                .bookings(page.getContent())
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }
}

