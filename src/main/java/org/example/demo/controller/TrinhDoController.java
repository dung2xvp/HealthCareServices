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
import java.util.List;
import org.example.demo.dto.request.TrinhDoRequest;
import org.example.demo.dto.response.TrinhDoResponse;
import org.example.demo.dto.response.ErrorResponse;
import org.example.demo.dto.response.MessageResponse;
import org.example.demo.service.TrinhDoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/degrees")
@Tag(name = "Trình độ", description = "API quản lý trình độ bác sĩ")
public class TrinhDoController {

    @Autowired
    private TrinhDoService trinhDoService;
    
    
    @Operation(
        summary = "Tạo trình độ mới",
        description = "Tạo trình độ mới cho bác sĩ. Chỉ Admin mới có quyền thực hiện."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tạo trình độ thành công",
            content = @Content(schema = @Schema(implementation = TrinhDoResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dữ liệu không hợp lệ",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Chưa đăng nhập",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Không có quyền truy cập",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping
    public ResponseEntity<TrinhDoResponse> createTrinhDo(
        @Valid @RequestBody TrinhDoRequest request) {
        TrinhDoResponse response = trinhDoService.create(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Lấy danh sách tất cả trình độ",
        description = "Lấy danh sách tất cả trình độ chưa bị xóa. API công khai, không cần đăng nhập."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lấy danh sách thành công",
            content = @Content(schema = @Schema(implementation = TrinhDoResponse.class))
        )
    })
    @GetMapping
    public ResponseEntity<List<TrinhDoResponse>> getAllTrinhDo() {
        List<TrinhDoResponse> list = trinhDoService.getAllTrinhDo();
        return ResponseEntity.ok(list);
    }

    @Operation(
        summary = "Lấy chi tiết trình độ",
        description = "Lấy thông tin chi tiết của trình độ theo ID. API công khai."
    )
    @ApiResponses( value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lấy thông tin thành công",
            content = @Content(schema = @Schema(implementation = TrinhDoResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy trình độ",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )

    })
    @GetMapping("/{id}")
    public ResponseEntity<TrinhDoResponse> getTrinhDoById(
        @Parameter(description = "ID của trình độ", required = true)
        @PathVariable Integer id) {
        TrinhDoResponse response = trinhDoService.getById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Tìm kiếm trình độ theo tên",
        description = "Tìm kiếm trình độ theo từ khóa (không phân biệt hoa thường). API công khai."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tìm kiếm thành công",
            content = @Content(schema = @Schema(implementation = TrinhDoResponse.class))
        )
    })
    @GetMapping("/search")
    public ResponseEntity<List<TrinhDoResponse>> searchTrinhDo(
        @Parameter(description = "Từ khóa tìm kiếm", required = true, example = "Bác sĩ nội khoa")
        @RequestParam String keyword) {
        List<TrinhDoResponse> list = trinhDoService.searchByName(keyword);
        return ResponseEntity.ok(list);
    }

    @Operation(
        summary = "Cập nhật trình độ",
        description = "Cập nhật thông tin của trình độ theo ID. Chỉ Admin mới có quyền thực hiện."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cập nhật thành công",
            content = @Content(schema = @Schema(implementation = TrinhDoResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dữ liệu không hợp lệ",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Chưa đăng nhập",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Không có quyền truy cập",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy trình độ",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping("/{id}")
    public ResponseEntity<TrinhDoResponse> updateTrinhDo(
            @Parameter(description = "ID của trình độ", required = true)
            @PathVariable Integer id,
            @Valid @RequestBody TrinhDoRequest request) {
        TrinhDoResponse response = trinhDoService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Xóa trình độ",
        description = "Xóa mềm một trình độ (soft delete). Chỉ Admin mới có quyền thực hiện."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Xóa thành công",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Chưa đăng nhập",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Không có quyền truy cập",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Không tìm thấy trình độ",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteTrinhDo(
            @Parameter(description = "ID của trình độ", required = true)
            @PathVariable Integer id) {
        trinhDoService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Xóa trình độ thành công"));
    }
}
