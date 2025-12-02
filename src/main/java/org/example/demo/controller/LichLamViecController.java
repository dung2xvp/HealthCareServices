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
import org.example.demo.dto.request.LichLamViecBulkCreateRequest;
import org.example.demo.dto.request.LichLamViecRequest;
import org.example.demo.dto.request.ToggleScheduleActiveRequest;
import org.example.demo.dto.response.ErrorResponse;
import org.example.demo.dto.response.LichLamViecResponse;
import org.example.demo.dto.response.LichLamViecSummaryResponse;
import org.example.demo.dto.response.LichLamViecWeeklyResponse;
import org.example.demo.dto.response.MessageResponse;
import org.example.demo.service.LichLamViecService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller cho Lịch Làm Việc Mặc Định
 * 
 * Endpoints:
 * - POST   /api/schedules                  - Tạo 1 ca làm việc
 * - POST   /api/schedules/bulk             - Tạo nhiều ca cùng lúc
 * - GET    /api/schedules                  - Lấy tất cả
 * - GET    /api/schedules/{id}             - Lấy chi tiết
 * - GET    /api/schedules/day/{thu}        - Lấy theo ngày
 * - GET    /api/schedules/active           - Lấy ca đang active
 * - GET    /api/schedules/weekly           - Lấy theo tuần (calendar view)
 * - GET    /api/schedules/summary          - Lấy tổng quan/thống kê
 * - PUT    /api/schedules/{id}             - Cập nhật
 * - PATCH  /api/schedules/toggle-active    - Bật/tắt nhiều ca
 * - DELETE /api/schedules/{id}             - Xóa
 * 
 * @author Healthcare System Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Tag(name = "Lịch Làm Việc Mặc Định", description = "API quản lý lịch làm việc mặc định toàn bệnh viện")
public class LichLamViecController {
    
    private final LichLamViecService lichLamViecService;
    
    // ==========================================
    // CREATE ENDPOINTS
    // ==========================================
    
    @Operation(
        summary = "Tạo lịch làm việc mới",
        description = "Tạo 1 ca làm việc mặc định mới. Chỉ Admin có quyền."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tạo thành công",
            content = @Content(schema = @Schema(implementation = LichLamViecResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dữ liệu không hợp lệ",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Lịch đã tồn tại (conflict)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping
    public ResponseEntity<LichLamViecResponse> create(
            @Valid @RequestBody LichLamViecRequest request) {
        LichLamViecResponse response = lichLamViecService.create(request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Tạo nhiều ca làm việc cùng lúc (Bulk Create)",
        description = "Tạo nhiều ca làm việc mặc định cùng lúc. Dùng để setup lịch ban đầu (14 ca/tuần). Chỉ Admin có quyền."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tạo thành công",
            content = @Content(schema = @Schema(implementation = LichLamViecResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dữ liệu không hợp lệ",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping("/bulk")
    public ResponseEntity<List<LichLamViecResponse>> bulkCreate(
            @Valid @RequestBody LichLamViecBulkCreateRequest request) {
        List<LichLamViecResponse> responses = lichLamViecService.bulkCreate(request);
        return ResponseEntity.ok(responses);
    }
    
    // ==========================================
    // READ ENDPOINTS
    // ==========================================
    
    @Operation(
        summary = "Lấy tất cả lịch làm việc",
        description = "Lấy danh sách tất cả ca làm việc mặc định. API công khai."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lấy danh sách thành công",
            content = @Content(schema = @Schema(implementation = LichLamViecResponse.class))
        )
    })
    @GetMapping
    public ResponseEntity<List<LichLamViecResponse>> getAll() {
        List<LichLamViecResponse> responses = lichLamViecService.getAll();
        return ResponseEntity.ok(responses);
    }
    
    @Operation(
        summary = "Lấy lịch làm việc theo ID",
        description = "Lấy chi tiết 1 ca làm việc mặc định. API công khai."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lấy thông tin thành công",
            content = @Content(schema = @Schema(implementation = LichLamViecResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy lịch làm việc",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<LichLamViecResponse> getById(
            @Parameter(description = "ID cấu hình lịch làm việc", example = "1")
            @PathVariable Integer id) {
        LichLamViecResponse response = lichLamViecService.getById(id);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Lấy lịch làm việc theo ngày trong tuần",
        description = "Lấy tất cả ca làm việc của 1 ngày cụ thể (2=Thứ 2, 8=Chủ nhật). API công khai."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lấy danh sách thành công",
            content = @Content(schema = @Schema(implementation = LichLamViecResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Thứ trong tuần không hợp lệ (phải 2-8)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping("/day/{thu}")
    public ResponseEntity<List<LichLamViecResponse>> getByDay(
            @Parameter(description = "Thứ trong tuần (2-8)", example = "2")
            @PathVariable Integer thu) {
        List<LichLamViecResponse> responses = lichLamViecService.getByDay(thu);
        return ResponseEntity.ok(responses);
    }
    
    @Operation(
        summary = "Lấy lịch làm việc đang active",
        description = "Lấy tất cả ca làm việc đang được áp dụng (isActive = true). API công khai."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lấy danh sách thành công",
            content = @Content(schema = @Schema(implementation = LichLamViecResponse.class))
        )
    })
    @GetMapping("/active")
    public ResponseEntity<List<LichLamViecResponse>> getActive() {
        List<LichLamViecResponse> responses = lichLamViecService.getActiveSchedules();
        return ResponseEntity.ok(responses);
    }
    
    @Operation(
        summary = "Lấy lịch làm việc theo tuần (Calendar view)",
        description = "Lấy lịch làm việc được group theo 7 ngày trong tuần. Dùng để hiển thị calendar. API công khai."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lấy lịch theo tuần thành công",
            content = @Content(schema = @Schema(implementation = LichLamViecWeeklyResponse.class))
        )
    })
    @GetMapping("/weekly")
    public ResponseEntity<List<LichLamViecWeeklyResponse>> getWeekly() {
        List<LichLamViecWeeklyResponse> responses = lichLamViecService.getWeeklyView();
        return ResponseEntity.ok(responses);
    }
    
    @Operation(
        summary = "Lấy tổng quan lịch làm việc (Statistics)",
        description = "Lấy thống kê tổng quan lịch làm việc: tổng ca, ca active/inactive, tổng giờ làm việc, etc. API công khai."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lấy tổng quan thành công",
            content = @Content(schema = @Schema(implementation = LichLamViecSummaryResponse.class))
        )
    })
    @GetMapping("/summary")
    public ResponseEntity<LichLamViecSummaryResponse> getSummary() {
        LichLamViecSummaryResponse response = lichLamViecService.getSummary();
        return ResponseEntity.ok(response);
    }
    
    // ==========================================
    // UPDATE ENDPOINTS
    // ==========================================
    
    @Operation(
        summary = "Cập nhật lịch làm việc",
        description = "Cập nhật thông tin ca làm việc mặc định. Chỉ Admin có quyền."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cập nhật thành công",
            content = @Content(schema = @Schema(implementation = LichLamViecResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy lịch làm việc",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Lịch đã tồn tại (conflict)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping("/{id}")
    public ResponseEntity<LichLamViecResponse> update(
            @Parameter(description = "ID cấu hình lịch làm việc", example = "1")
            @PathVariable Integer id,
            @Valid @RequestBody LichLamViecRequest request) {
        LichLamViecResponse response = lichLamViecService.update(id, request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Bật/tắt nhiều ca làm việc (Batch operation)",
        description = "Bật hoặc tắt nhiều ca làm việc cùng lúc. Use cases: Tắt tất cả ca Chủ nhật, tắt ca TOI, tắt/bật khi nghỉ lễ. Chỉ Admin có quyền."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Toggle thành công",
            content = @Content(schema = @Schema(implementation = LichLamViecResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy một hoặc nhiều ca làm việc",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('Admin')")
    @PatchMapping("/toggle-active")
    public ResponseEntity<List<LichLamViecResponse>> toggleActive(
            @Valid @RequestBody ToggleScheduleActiveRequest request) {
        List<LichLamViecResponse> responses = lichLamViecService.toggleActive(request);
        return ResponseEntity.ok(responses);
    }
    
    // ==========================================
    // DELETE ENDPOINTS
    // ==========================================
    
    @Operation(
        summary = "Xóa lịch làm việc",
        description = "Xóa ca làm việc mặc định (hard delete). Chỉ Admin có quyền."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Xóa thành công",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy lịch làm việc",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(
            @Parameter(description = "ID cấu hình lịch làm việc", example = "1")
            @PathVariable Integer id) {
        lichLamViecService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Xóa lịch làm việc thành công"));
    }
}

