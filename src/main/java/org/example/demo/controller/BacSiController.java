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
import org.example.demo.dto.request.BacSiRequest;
import org.example.demo.dto.request.CreateDoctorAccountRequest;
import org.example.demo.dto.response.BacSiDetailResponse;
import org.example.demo.dto.response.BacSiResponse;
import org.example.demo.dto.response.ErrorResponse;
import org.example.demo.dto.response.MessageResponse;
import org.example.demo.service.BacSiService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller quản lý Bác Sĩ
 * 
 * APIs:
 * - POST   /api/doctors/create-account (Admin) - Tạo tài khoản bác sĩ (Combined API)
 * - POST   /api/doctors              (Admin) - Tạo bác sĩ (từ NguoiDung có sẵn)
 * - GET    /api/doctors              (Public) - Lấy danh sách
 * - GET    /api/doctors/{id}         (Public) - Lấy chi tiết
 * - GET    /api/doctors/specialty/{chuyenKhoaId}  (Public) - Lấy theo chuyên khoa
 * - GET    /api/doctors/search       (Public) - Tìm kiếm
 * - GET    /api/doctors/top-experienced (Public) - Lấy top bác sĩ kinh nghiệm
 * - PUT    /api/doctors/{id}         (Admin) - Cập nhật
 * - DELETE /api/doctors/{id}         (Admin) - Xóa (soft delete cascade)
 * - PUT    /api/doctors/{id}/toggle-status (Admin) - Bật/tắt trạng thái
 * - PUT    /api/doctors/{id}/restore (Admin) - Khôi phục bác sĩ đã xóa
 */
@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctor Management", description = "APIs quản lý Bác Sĩ")
public class BacSiController {
    
    private final BacSiService bacSiService;
    
    // ==================== CREATE ====================
    
    /**
     * COMBINED API: Tạo tài khoản bác sĩ (NguoiDung + BacSi) trong 1 lần
     * Đây là API khuyến nghị để tạo bác sĩ mới
     */
    @PostMapping("/create-account")
    @PreAuthorize("hasAuthority('Admin')")
    @Operation(
        summary = "Tạo tài khoản bác sĩ mới (Admin only) - RECOMMENDED",
        description = "Tạo cả tài khoản người dùng VÀ hồ sơ bác sĩ trong 1 transaction. " +
                      "API này giúp đơn giản hóa việc tạo bác sĩ mới. " +
                      "⚠️ Giá khám TỰ ĐỘNG lấy từ Trình Độ (không cần nhập).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Tạo tài khoản bác sĩ thành công",
            content = @Content(schema = @Schema(implementation = BacSiResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dữ liệu không hợp lệ",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Chuyên khoa/Trình độ không tồn tại",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Email hoặc số điện thoại đã được sử dụng",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<BacSiResponse> createDoctorAccount(
            @Valid @RequestBody CreateDoctorAccountRequest request
    ) {
        BacSiResponse response = bacSiService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * API cũ: Tạo bác sĩ từ NguoiDung có sẵn
     * Sử dụng khi đã có tài khoản NguoiDung với VaiTro = BacSi
     */
    @PostMapping
    @PreAuthorize("hasAuthority('Admin')")
    @Operation(
        summary = "Tạo hồ sơ bác sĩ từ tài khoản có sẵn (Admin only)",
        description = "Tạo hồ sơ bác sĩ từ NguoiDung có VaiTro = BacSi. " +
                      "⚠️ Giá khám TỰ ĐỘNG lấy từ Trình Độ (không cần nhập). " +
                      "Nên dùng /create-account để đơn giản hơn.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Tạo bác sĩ thành công",
            content = @Content(schema = @Schema(implementation = BacSiResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dữ liệu không hợp lệ",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Người dùng/Chuyên khoa/Trình độ không tồn tại",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Người dùng này đã là bác sĩ",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<BacSiResponse> createDoctor(
            @Valid @RequestBody BacSiRequest request
    ) {
        BacSiResponse response = bacSiService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    // ==================== READ ====================
    
    @GetMapping
    @Operation(
        summary = "Lấy danh sách bác sĩ (Public)",
        description = "Lấy danh sách tất cả bác sĩ với phân trang và sắp xếp"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lấy danh sách thành công"
        )
    })
    public ResponseEntity<Page<BacSiResponse>> getAllDoctors(
            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Số lượng bản ghi mỗi trang")
            @RequestParam(defaultValue = "10") int size,
            
            @Parameter(description = "Sắp xếp theo field (mặc định: hoTen)")
            @RequestParam(defaultValue = "nguoiDung.hoTen") String sortBy,
            
            @Parameter(description = "Hướng sắp xếp (asc/desc)")
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<BacSiResponse> doctors = bacSiService.getAll(pageable);
        
        return ResponseEntity.ok(doctors);
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Lấy chi tiết bác sĩ (Public)",
        description = "Lấy thông tin chi tiết đầy đủ của bác sĩ bao gồm quá trình đào tạo, kinh nghiệm..."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lấy chi tiết thành công",
            content = @Content(schema = @Schema(implementation = BacSiDetailResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Bác sĩ không tồn tại",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<BacSiDetailResponse> getDoctorById(
            @Parameter(description = "ID của bác sĩ")
            @PathVariable Integer id
    ) {
        BacSiDetailResponse response = bacSiService.getById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/specialty/{chuyenKhoaId}")
    @Operation(
        summary = "Lấy bác sĩ theo chuyên khoa (Public)",
        description = "Lấy danh sách bác sĩ thuộc một chuyên khoa cụ thể (chỉ bác sĩ đang làm việc)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lấy danh sách thành công"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Chuyên khoa không tồn tại",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<List<BacSiResponse>> getDoctorsBySpecialty(
            @Parameter(description = "ID của chuyên khoa")
            @PathVariable Integer chuyenKhoaId
    ) {
        List<BacSiResponse> doctors = bacSiService.getByChuyenKhoa(chuyenKhoaId);
        return ResponseEntity.ok(doctors);
    }
    
    @GetMapping("/search")
    @Operation(
        summary = "Tìm kiếm bác sĩ (Public)",
        description = "Tìm kiếm bác sĩ theo tên hoặc tên chuyên khoa"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tìm kiếm thành công"
        )
    })
    public ResponseEntity<Page<BacSiResponse>> searchDoctors(
            @Parameter(description = "Từ khóa tìm kiếm (tên bác sĩ hoặc chuyên khoa)")
            @RequestParam String keyword,
            
            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Số lượng bản ghi mỗi trang")
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BacSiResponse> doctors = bacSiService.search(keyword, pageable);
        
        return ResponseEntity.ok(doctors);
    }
    
    @GetMapping("/top-experienced")
    @Operation(
        summary = "Lấy top bác sĩ có kinh nghiệm cao (Public)",
        description = "Lấy top 10 bác sĩ có số năm kinh nghiệm cao nhất (đang hoạt động)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lấy danh sách thành công"
        )
    })
    public ResponseEntity<List<BacSiResponse>> getTopExperiencedDoctors() {
        List<BacSiResponse> doctors = bacSiService.getTopExperienced();
        return ResponseEntity.ok(doctors);
    }
    
    // ==================== UPDATE ====================
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('Admin')")
    @Operation(
        summary = "Cập nhật thông tin bác sĩ (Admin only)",
        description = "Cập nhật thông tin bác sĩ bao gồm chuyên khoa, trình độ... " +
                      "⚠️ Giá khám TỰ ĐỘNG cập nhật khi đổi Trình Độ (không cập nhật riêng).",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cập nhật thành công",
            content = @Content(schema = @Schema(implementation = BacSiResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dữ liệu không hợp lệ",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Bác sĩ không tồn tại",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<BacSiResponse> updateDoctor(
            @Parameter(description = "ID của bác sĩ")
            @PathVariable Integer id,
            
            @Valid @RequestBody BacSiRequest request
    ) {
        BacSiResponse response = bacSiService.update(id, request);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasAuthority('Admin')")
    @Operation(
        summary = "Bật/Tắt trạng thái làm việc của bác sĩ (Admin only)",
        description = "Chuyển đổi trạng thái làm việc của bác sĩ (đang làm ↔ nghỉ việc)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Đổi trạng thái thành công",
            content = @Content(schema = @Schema(implementation = BacSiResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Bác sĩ không tồn tại",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<BacSiResponse> toggleDoctorStatus(
            @Parameter(description = "ID của bác sĩ")
            @PathVariable Integer id
    ) {
        BacSiResponse response = bacSiService.toggleStatus(id);
        return ResponseEntity.ok(response);
    }
    
    // ==================== DELETE ====================
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('Admin')")
    @Operation(
        summary = "Xóa bác sĩ (Admin only)",
        description = "Xóa mềm bác sĩ (soft delete). " +
                      "Bác sĩ vẫn tồn tại trong DB nhưng bị đánh dấu xóa. " +
                      "Cascade: Tài khoản người dùng cũng bị vô hiệu hóa. " +
                      "Có thể khôi phục bằng API /restore.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Xóa thành công"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Bác sĩ không tồn tại",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<MessageResponse> deleteDoctor(
            @Parameter(description = "ID của bác sĩ")
            @PathVariable Integer id
    ) {
        bacSiService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Xóa bác sĩ thành công"));
    }
    
    @PutMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('Admin')")
    @Operation(
        summary = "Khôi phục bác sĩ đã xóa (Admin only)",
        description = "Khôi phục bác sĩ đã bị xóa mềm. " +
                      "Cascade: Tài khoản người dùng cũng được kích hoạt lại. " +
                      "Trạng thái làm việc sẽ được bật lại.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Khôi phục thành công",
            content = @Content(schema = @Schema(implementation = BacSiResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bác sĩ chưa bị xóa",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Bác sĩ không tồn tại",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<BacSiResponse> restoreDoctor(
            @Parameter(description = "ID của bác sĩ cần khôi phục")
            @PathVariable Integer id
    ) {
        BacSiResponse response = bacSiService.restore(id);
        return ResponseEntity.ok(response);
    }
}

