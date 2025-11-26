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
import org.example.demo.dto.request.ChuyenKhoaRequest;
import org.example.demo.dto.response.ChuyenKhoaResponse;
import org.example.demo.dto.response.ErrorResponse;
import org.example.demo.dto.response.MessageResponse;
import org.example.demo.service.ChuyenKhoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specialties")
@Tag(name = "Chuyên khoa", description = "API quản lý chuyên khoa y tế")
public class ChuyenKhoaController {

    @Autowired
    private ChuyenKhoaService chuyenKhoaService;

    @Operation(
            summary = "Tạo chuyên khoa mới",
            description = "Tạo chuyên khoa mới cho cơ sở y tế. Chỉ Admin mới có quyền thực hiện."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tạo chuyên khoa thành công",
                    content = @Content(schema = @Schema(implementation = ChuyenKhoaResponse.class))
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
                    description = "Không tìm thấy cơ sở y tế",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('Admin')")
    @PostMapping
    public ResponseEntity<ChuyenKhoaResponse> createChuyenKhoa(
            @Valid @RequestBody ChuyenKhoaRequest request) {
        ChuyenKhoaResponse response = chuyenKhoaService.create(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Lấy danh sách tất cả chuyên khoa",
            description = "Lấy danh sách tất cả chuyên khoa chưa bị xóa. API công khai, không cần đăng nhập."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lấy danh sách thành công",
                    content = @Content(schema = @Schema(implementation = ChuyenKhoaResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<List<ChuyenKhoaResponse>> getAllChuyenKhoa() {
        List<ChuyenKhoaResponse> list = chuyenKhoaService.getAllChuyenKhoa();
        return ResponseEntity.ok(list);
    }

    @Operation(
            summary = "Lấy danh sách chuyên khoa sắp xếp",
            description = "Lấy danh sách chuyên khoa sắp xếp theo thứ tự hiển thị. API công khai."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lấy danh sách thành công",
                    content = @Content(schema = @Schema(implementation = ChuyenKhoaResponse.class))
            )
    })
    @GetMapping("/sorted")
    public ResponseEntity<List<ChuyenKhoaResponse>> getAllChuyenKhoaSorted() {
        List<ChuyenKhoaResponse> list = chuyenKhoaService.getAllChuyenKhoaSorted();
        return ResponseEntity.ok(list);
    }

    @Operation(
            summary = "Lấy chi tiết chuyên khoa",
            description = "Lấy thông tin chi tiết của một chuyên khoa theo ID. API công khai."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lấy thông tin thành công",
                    content = @Content(schema = @Schema(implementation = ChuyenKhoaResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy chuyên khoa",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ChuyenKhoaResponse> getChuyenKhoaById(
            @Parameter(description = "ID của chuyên khoa", required = true)
            @PathVariable Integer id) {
        ChuyenKhoaResponse response = chuyenKhoaService.getById(id);
        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "Tìm kiếm chuyên khoa theo tên",
            description = "Tìm kiếm chuyên khoa theo từ khóa (không phân biệt hoa thường). API công khai."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tìm kiếm thành công",
                    content = @Content(schema = @Schema(implementation = ChuyenKhoaResponse.class))
            )
    })
    @GetMapping("/search")
    public ResponseEntity<List<ChuyenKhoaResponse>> searchChuyenKhoa(
            @Parameter(description = "Từ khóa tìm kiếm", required = true, example = "Tim mạch")
            @RequestParam String keyword) {
        List<ChuyenKhoaResponse> list = chuyenKhoaService.searchByName(keyword);
        return ResponseEntity.ok(list);
    }

    @Operation(
            summary = "Cập nhật thông tin chuyên khoa",
            description = "Cập nhật thông tin của một chuyên khoa. Chỉ Admin mới có quyền thực hiện."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = ChuyenKhoaResponse.class))
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
                    description = "Không tìm thấy chuyên khoa",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('Admin')")
    @PutMapping("/{id}")
    public ResponseEntity<ChuyenKhoaResponse> updateChuyenKhoa(
            @Parameter(description = "ID của chuyên khoa", required = true)
            @PathVariable Integer id,
            @Valid @RequestBody ChuyenKhoaRequest request) {
        ChuyenKhoaResponse response = chuyenKhoaService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Xóa chuyên khoa",
            description = "Xóa mềm một chuyên khoa (soft delete). Chỉ Admin mới có quyền thực hiện."
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
                    description = "Không tìm thấy chuyên khoa",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAuthority('Admin')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteChuyenKhoa(
            @Parameter(description = "ID của chuyên khoa", required = true)
            @PathVariable Integer id) {
        chuyenKhoaService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Xóa chuyên khoa thành công"));
    }
}

