package org.example.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.demo.dto.response.ApiResponseDTO;
import org.example.demo.dto.response.UserResponse;
import org.example.demo.enums.VaiTro;
import org.example.demo.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "API quản trị người dùng")
public class UserController {

    @Autowired
    private AuthService authService;

    @GetMapping
    @PreAuthorize("hasAuthority('Admin')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Danh sách người dùng", description = "Tìm kiếm, lọc, phân trang người dùng")
    public ResponseEntity<ApiResponseDTO<Page<UserResponse>>> searchUsers(
            @Parameter(description = "Từ khóa: tên, email, số điện thoại")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Vai trò", schema = @Schema(implementation = VaiTro.class))
            @RequestParam(required = false) VaiTro role,
            @Parameter(description = "Trạng thái hoạt động")
            @RequestParam(required = false) Boolean active,
            @Parameter(description = "Đã xóa mềm")
            @RequestParam(required = false) Boolean deleted,
            @Parameter(description = "Trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Trường sắp xếp", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Chiều sắp xếp", example = "DESC")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Page<UserResponse> result = authService.searchUsers(
                keyword,
                role,
                active,
                deleted,
                page,
                size,
                sortBy,
                direction
        );
        return ResponseEntity.ok(ApiResponseDTO.success(result, "Lấy danh sách người dùng thành công"));
    }

    @GetMapping("/patients")
    @PreAuthorize("hasAuthority('Admin')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Danh sách bệnh nhân (phân trang)", description = "Lọc theo từ khóa/trạng thái/xóa mềm, mặc định role=BenhNhan")
    public ResponseEntity<ApiResponseDTO<Page<UserResponse>>> searchPatients(
            @Parameter(description = "Từ khóa: tên, email, số điện thoại")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Trạng thái hoạt động")
            @RequestParam(required = false) Boolean active,
            @Parameter(description = "Đã xóa mềm")
            @RequestParam(required = false) Boolean deleted,
            @Parameter(description = "Trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Trường sắp xếp", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Chiều sắp xếp", example = "DESC")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Page<UserResponse> result = authService.searchUsers(
                keyword,
                VaiTro.BenhNhan,
                active,
                deleted,
                page,
                size,
                sortBy,
                direction
        );
        return ResponseEntity.ok(ApiResponseDTO.success(result, "Lấy danh sách bệnh nhân thành công"));
    }
}

