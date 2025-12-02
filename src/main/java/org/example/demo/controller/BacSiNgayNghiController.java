package org.example.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.demo.dto.request.ApproveNgayNghiRequest;
import org.example.demo.dto.request.CreateNgayNghiRequest;
import org.example.demo.dto.request.SearchNgayNghiRequest;
import org.example.demo.dto.request.UpdateNgayNghiRequest;
import org.example.demo.dto.response.*;
import org.example.demo.service.BacSiNgayNghiService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller cho Yêu Cầu Nghỉ của Bác Sĩ
 * 
 * Endpoints:
 * - POST   /api/leave-requests                           - Bác sĩ tạo yêu cầu nghỉ
 * - GET    /api/leave-requests                           - Lấy tất cả yêu cầu
 * - GET    /api/leave-requests/{id}                      - Lấy chi tiết
 * - GET    /api/leave-requests/pending                   - Admin: Lấy yêu cầu chờ duyệt
 * - GET    /api/leave-requests/my-requests/{bacSiID}     - Bác sĩ: Xem yêu cầu của mình
 * - GET    /api/leave-requests/search                    - Tìm kiếm/filter
 * - GET    /api/leave-requests/statistics                - Thống kê
 * - PUT    /api/leave-requests/{id}                      - Bác sĩ: Chỉnh sửa yêu cầu
 * - PATCH  /api/leave-requests/approve                   - Admin: Duyệt/từ chối
 * - PATCH  /api/leave-requests/{id}/cancel               - Hủy yêu cầu
 * - DELETE /api/leave-requests/{id}                      - Xóa yêu cầu
 * 
 * @author Healthcare System Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
@Tag(name = "Yêu Cầu Nghỉ Bác Sĩ", description = "API quản lý yêu cầu nghỉ của bác sĩ (với approval workflow)")
public class BacSiNgayNghiController {
    
    private final BacSiNgayNghiService bacSiNgayNghiService;
    
    // ==========================================
    // CREATE ENDPOINTS
    // ==========================================
    
    @Operation(
        summary = "Bác sĩ tạo yêu cầu nghỉ mới",
        description = "Bác sĩ tạo yêu cầu nghỉ. Hỗ trợ 3 loại: Nghỉ ngày cụ thể, nghỉ ca cụ thể, nghỉ ca hàng tuần. Chỉ Bác sĩ có quyền."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tạo yêu cầu thành công",
            content = @Content(schema = @Schema(implementation = NgayNghiResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dữ liệu không hợp lệ hoặc không đủ ngày phép",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Yêu cầu bị trùng với lịch nghỉ đã duyệt",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('BacSi')")
    @PostMapping
    public ResponseEntity<NgayNghiResponse> create(
            @Parameter(description = "ID bác sĩ", example = "10")
            @RequestParam Integer bacSiID,
            @Valid @RequestBody CreateNgayNghiRequest request) {
        NgayNghiResponse response = bacSiNgayNghiService.create(bacSiID, request);
        return ResponseEntity.ok(response);
    }
    
    // ==========================================
    // READ ENDPOINTS
    // ==========================================
    
    @Operation(
        summary = "Lấy tất cả yêu cầu nghỉ",
        description = "Lấy danh sách tất cả yêu cầu nghỉ. Chỉ Admin có quyền."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lấy danh sách thành công",
            content = @Content(schema = @Schema(implementation = NgayNghiResponse.class))
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping
    public ResponseEntity<List<NgayNghiResponse>> getAll() {
        List<NgayNghiResponse> responses = bacSiNgayNghiService.getAll();
        return ResponseEntity.ok(responses);
    }
    
    @Operation(
        summary = "Lấy chi tiết yêu cầu nghỉ",
        description = "Lấy thông tin chi tiết 1 yêu cầu nghỉ. Admin hoặc bác sĩ sở hữu có quyền."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lấy thông tin thành công",
            content = @Content(schema = @Schema(implementation = NgayNghiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy yêu cầu nghỉ",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('Admin') or hasAuthority('BacSi')")
    @GetMapping("/{id}")
    public ResponseEntity<NgayNghiResponse> getById(
            @Parameter(description = "ID yêu cầu nghỉ", example = "1")
            @PathVariable Integer id) {
        NgayNghiResponse response = bacSiNgayNghiService.getById(id);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Admin: Lấy yêu cầu chờ duyệt",
        description = "Lấy danh sách yêu cầu nghỉ chờ duyệt, sắp xếp theo thời gian tạo (cũ nhất trước). Chỉ Admin có quyền."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lấy danh sách thành công",
            content = @Content(schema = @Schema(implementation = NgayNghiPendingResponse.class))
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('Admin')")
    @GetMapping("/pending")
    public ResponseEntity<List<NgayNghiPendingResponse>> getPending() {
        List<NgayNghiPendingResponse> responses = bacSiNgayNghiService.getPendingRequests();
        return ResponseEntity.ok(responses);
    }
    
    @Operation(
        summary = "Bác sĩ: Xem yêu cầu của mình",
        description = "Bác sĩ xem lịch sử yêu cầu nghỉ của mình. Bác sĩ chỉ xem được yêu cầu của mình."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lấy danh sách thành công",
            content = @Content(schema = @Schema(implementation = NgayNghiMyRequestResponse.class))
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('BacSi')")
    @GetMapping("/my-requests/{bacSiID}")
    public ResponseEntity<List<NgayNghiMyRequestResponse>> getMyRequests(
            @Parameter(description = "ID bác sĩ", example = "10")
            @PathVariable Integer bacSiID) {
        List<NgayNghiMyRequestResponse> responses = bacSiNgayNghiService.getMyRequests(bacSiID);
        return ResponseEntity.ok(responses);
    }
    
    @Operation(
        summary = "Tìm kiếm/filter yêu cầu nghỉ",
        description = "Tìm kiếm yêu cầu nghỉ với nhiều tiêu chí. Chỉ Admin có quyền."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tìm kiếm thành công",
            content = @Content(schema = @Schema(implementation = NgayNghiResponse.class))
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping("/search")
    public ResponseEntity<List<NgayNghiResponse>> search(
            @Valid @RequestBody SearchNgayNghiRequest request) {
        List<NgayNghiResponse> responses = bacSiNgayNghiService.search(request);
        return ResponseEntity.ok(responses);
    }
    
    @Operation(
        summary = "Lấy thống kê yêu cầu nghỉ",
        description = "Lấy thống kê tổng quan yêu cầu nghỉ. Admin xem tất cả, bác sĩ chỉ xem của mình."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lấy thống kê thành công",
            content = @Content(schema = @Schema(implementation = NgayNghiStatisticsResponse.class))
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('Admin') or hasAuthority('BacSi')")
    @GetMapping("/statistics")
    public ResponseEntity<NgayNghiStatisticsResponse> getStatistics(
            @Parameter(description = "ID bác sĩ (optional, null = tất cả)", example = "10")
            @RequestParam(required = false) Integer bacSiID) {
        NgayNghiStatisticsResponse response = bacSiNgayNghiService.getStatistics(bacSiID);
        return ResponseEntity.ok(response);
    }
    
    // ==========================================
    // UPDATE ENDPOINTS
    // ==========================================
    
    @Operation(
        summary = "Bác sĩ: Chỉnh sửa yêu cầu nghỉ",
        description = "Bác sĩ chỉnh sửa yêu cầu nghỉ (CHỈ khi status = CHO_DUYET). Chỉ cho phép sửa lý do và file đính kèm. Chỉ Bác sĩ có quyền."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cập nhật thành công",
            content = @Content(schema = @Schema(implementation = NgayNghiResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Chỉ có thể sửa yêu cầu đang chờ duyệt",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy yêu cầu nghỉ",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('BacSi')")
    @PutMapping("/{id}")
    public ResponseEntity<NgayNghiResponse> update(
            @Parameter(description = "ID yêu cầu nghỉ", example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody UpdateNgayNghiRequest request) {
        NgayNghiResponse response = bacSiNgayNghiService.update(id, request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Admin: Duyệt/từ chối yêu cầu nghỉ (Batch)",
        description = "Admin duyệt hoặc từ chối nhiều yêu cầu nghỉ cùng lúc. Chỉ Admin có quyền."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Xử lý yêu cầu thành công",
            content = @Content(schema = @Schema(implementation = NgayNghiResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Yêu cầu không ở trạng thái chờ duyệt hoặc lý do từ chối không hợp lệ",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy một hoặc nhiều yêu cầu nghỉ",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('Admin')")
    @PatchMapping("/approve")
    public ResponseEntity<List<NgayNghiResponse>> approve(
            @Valid @RequestBody ApproveNgayNghiRequest request) {
        List<NgayNghiResponse> responses = bacSiNgayNghiService.approve(request);
        return ResponseEntity.ok(responses);
    }
    
    @Operation(
        summary = "Hủy yêu cầu nghỉ",
        description = "Bác sĩ hoặc Admin hủy yêu cầu nghỉ. CHỈ cho phép hủy khi status = CHO_DUYET hoặc DA_DUYET. Nếu đã duyệt + PHEP_NAM thì sẽ hoàn lại ngày phép."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Hủy yêu cầu thành công",
            content = @Content(schema = @Schema(implementation = NgayNghiResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Chỉ có thể hủy yêu cầu đang chờ duyệt hoặc đã duyệt",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy yêu cầu nghỉ",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('Admin') or hasAuthority('BacSi')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<NgayNghiResponse> cancel(
            @Parameter(description = "ID yêu cầu nghỉ", example = "1")
            @PathVariable Integer id) {
        NgayNghiResponse response = bacSiNgayNghiService.cancel(id);
        return ResponseEntity.ok(response);
    }
    
    // ==========================================
    // DELETE ENDPOINTS
    // ==========================================
    
    @Operation(
        summary = "Xóa yêu cầu nghỉ",
        description = "Xóa yêu cầu nghỉ (hard delete). CHỈ cho phép xóa khi status = CHO_DUYET, TU_CHOI, hoặc HUY. Không thể xóa yêu cầu đã duyệt. Chỉ Admin hoặc bác sĩ sở hữu có quyền."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Xóa thành công",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Không thể xóa yêu cầu đã được duyệt",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy yêu cầu nghỉ",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('Admin') or hasAuthority('BacSi')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(
            @Parameter(description = "ID yêu cầu nghỉ", example = "1")
            @PathVariable Integer id) {
        bacSiNgayNghiService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Xóa yêu cầu nghỉ thành công"));
    }
}

