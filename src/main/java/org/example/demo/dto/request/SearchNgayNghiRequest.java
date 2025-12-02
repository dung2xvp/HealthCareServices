package org.example.demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo.enums.LoaiNghi;
import org.example.demo.enums.LoaiNghiPhep;
import org.example.demo.enums.TrangThaiNghi;

import java.time.LocalDate;

/**
 * DTO cho request tìm kiếm/filter yêu cầu nghỉ
 * 
 * Use Cases:
 * 1. Admin xem tất cả yêu cầu (có thể filter theo bác sĩ, trạng thái)
 * 2. Admin tìm yêu cầu chờ duyệt
 * 3. Bác sĩ xem yêu cầu của mình
 * 4. Tìm yêu cầu theo khoảng thời gian
 * 
 * All fields are optional - nếu null thì không filter theo field đó
 * 
 * @author Healthcare System Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request tìm kiếm/filter yêu cầu nghỉ")
public class SearchNgayNghiRequest {
    
    /**
     * ID bác sĩ (optional)
     * Nếu có: Chỉ lấy yêu cầu của bác sĩ này
     * Nếu null: Lấy tất cả bác sĩ (dành cho Admin)
     */
    @Schema(
        description = "ID bác sĩ (optional, null = tất cả)",
        example = "10"
    )
    private Integer bacSiID;
    
    /**
     * Trạng thái yêu cầu (optional)
     * Nếu có: Chỉ lấy yêu cầu với trạng thái này
     * Nếu null: Lấy tất cả trạng thái
     */
    @Schema(
        description = "Trạng thái yêu cầu (optional, null = tất cả)",
        example = "CHO_DUYET",
        allowableValues = {"CHO_DUYET", "DA_DUYET", "TU_CHOI", "HUY"}
    )
    private TrangThaiNghi trangThai;
    
    /**
     * Loại nghỉ (optional)
     */
    @Schema(
        description = "Loại nghỉ (optional, null = tất cả)",
        example = "NGAY_CU_THE",
        allowableValues = {"NGAY_CU_THE", "CA_CU_THE", "CA_HANG_TUAN"}
    )
    private LoaiNghi loaiNghi;
    
    /**
     * Loại nghỉ phép (optional)
     */
    @Schema(
        description = "Loại nghỉ phép (optional, null = tất cả)",
        example = "PHEP_NAM",
        allowableValues = {"PHEP_NAM", "OM", "CONG_TAC", "KHAC"}
    )
    private LoaiNghiPhep loaiNghiPhep;
    
    /**
     * Từ ngày (optional)
     * Filter yêu cầu có ngayNghiCuThe >= fromDate
     */
    @Schema(
        description = "Từ ngày (optional, filter ngày nghỉ)",
        example = "2025-12-01"
    )
    private LocalDate fromDate;
    
    /**
     * Đến ngày (optional)
     * Filter yêu cầu có ngayNghiCuThe <= toDate
     */
    @Schema(
        description = "Đến ngày (optional, filter ngày nghỉ)",
        example = "2025-12-31"
    )
    private LocalDate toDate;
    
    /**
     * Tìm kiếm theo tên bác sĩ (optional)
     * Search trong NguoiDung.hoTen
     */
    @Schema(
        description = "Tìm kiếm theo tên bác sĩ (optional, không phân biệt hoa thường)",
        example = "Nguyễn Văn"
    )
    private String tenBacSi;
    
    /**
     * Chuyên khoa ID (optional)
     * Filter theo chuyên khoa của bác sĩ
     */
    @Schema(
        description = "ID chuyên khoa (optional)",
        example = "1"
    )
    private Integer chuyenKhoaID;
    
    /**
     * ID người duyệt (optional)
     * Filter yêu cầu được duyệt bởi admin này
     */
    @Schema(
        description = "ID người duyệt (optional)",
        example = "1"
    )
    private Integer nguoiDuyetID;
    
    /**
     * Từ ngày tạo (optional)
     * Filter yêu cầu được tạo từ ngày này
     */
    @Schema(
        description = "Từ ngày tạo (optional)",
        example = "2025-11-01"
    )
    private LocalDate fromCreatedDate;
    
    /**
     * Đến ngày tạo (optional)
     * Filter yêu cầu được tạo đến ngày này
     */
    @Schema(
        description = "Đến ngày tạo (optional)",
        example = "2025-11-30"
    )
    private LocalDate toCreatedDate;
    
    /**
     * Sort by field (optional)
     * Default: createdAt
     */
    @Schema(
        description = "Sắp xếp theo field (optional)",
        example = "createdAt",
        allowableValues = {"createdAt", "ngayNghiCuThe", "trangThai"}
    )
    private String sortBy;
    
    /**
     * Sort direction (optional)
     * Default: DESC
     */
    @Schema(
        description = "Hướng sắp xếp (optional)",
        example = "DESC",
        allowableValues = {"ASC", "DESC"}
    )
    private String sortDirection;
    
    /**
     * Page number (optional, 0-based)
     * Default: 0
     */
    @Schema(
        description = "Số trang (0-based, optional)",
        example = "0"
    )
    private Integer page;
    
    /**
     * Page size (optional)
     * Default: 20
     */
    @Schema(
        description = "Số items per page (optional)",
        example = "20"
    )
    private Integer size;
    
    /**
     * Custom validation method
     */
    public void validateBusinessRules() {
        // Rule 1: fromDate phải <= toDate
        if (fromDate != null && toDate != null) {
            if (fromDate.isAfter(toDate)) {
                throw new IllegalArgumentException(
                    "Từ ngày phải nhỏ hơn hoặc bằng đến ngày"
                );
            }
        }
        
        // Rule 2: fromCreatedDate phải <= toCreatedDate
        if (fromCreatedDate != null && toCreatedDate != null) {
            if (fromCreatedDate.isAfter(toCreatedDate)) {
                throw new IllegalArgumentException(
                    "Từ ngày tạo phải nhỏ hơn hoặc bằng đến ngày tạo"
                );
            }
        }
        
        // Rule 3: Page size hợp lý
        if (size != null && (size < 1 || size > 100)) {
            throw new IllegalArgumentException(
                "Page size phải từ 1-100"
            );
        }
        
        // Rule 4: Page number >= 0
        if (page != null && page < 0) {
            throw new IllegalArgumentException(
                "Page number phải >= 0"
            );
        }
    }
    
    /**
     * Helper method: Check xem có filter nào không
     */
    public boolean hasAnyFilter() {
        return bacSiID != null
            || trangThai != null
            || loaiNghi != null
            || loaiNghiPhep != null
            || fromDate != null
            || toDate != null
            || tenBacSi != null
            || chuyenKhoaID != null
            || nguoiDuyetID != null
            || fromCreatedDate != null
            || toCreatedDate != null;
    }
    
    /**
     * Helper method: Lấy page number (default 0)
     */
    public int getPageOrDefault() {
        return page != null ? page : 0;
    }
    
    /**
     * Helper method: Lấy page size (default 20)
     */
    public int getSizeOrDefault() {
        return size != null ? size : 20;
    }
    
    /**
     * Helper method: Lấy sort by (default createdAt)
     */
    public String getSortByOrDefault() {
        return sortBy != null ? sortBy : "createdAt";
    }
    
    /**
     * Helper method: Lấy sort direction (default DESC)
     */
    public String getSortDirectionOrDefault() {
        return sortDirection != null ? sortDirection.toUpperCase() : "DESC";
    }
    
    /**
     * Static factory: Tạo request chỉ lấy yêu cầu chờ duyệt
     */
    public static SearchNgayNghiRequest pendingOnly() {
        return SearchNgayNghiRequest.builder()
            .trangThai(TrangThaiNghi.CHO_DUYET)
            .sortBy("createdAt")
            .sortDirection("ASC") // Cũ nhất trước
            .build();
    }
    
    /**
     * Static factory: Tạo request lấy yêu cầu của 1 bác sĩ
     */
    public static SearchNgayNghiRequest forDoctor(Integer bacSiID) {
        return SearchNgayNghiRequest.builder()
            .bacSiID(bacSiID)
            .sortBy("createdAt")
            .sortDirection("DESC")
            .build();
    }
    
    /**
     * Static factory: Tạo request lấy yêu cầu trong khoảng thời gian
     */
    public static SearchNgayNghiRequest byDateRange(LocalDate from, LocalDate to) {
        return SearchNgayNghiRequest.builder()
            .fromDate(from)
            .toDate(to)
            .sortBy("ngayNghiCuThe")
            .sortDirection("ASC")
            .build();
    }
}

