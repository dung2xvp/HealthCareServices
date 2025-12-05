package org.example.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.demo.dto.response.CoSoYTeResponse;
import org.example.demo.dto.response.ErrorResponse;
import org.example.demo.entity.CoSoYTe;
import org.example.demo.exception.ResourceNotFoundException;
import org.example.demo.repository.CoSoYTeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/facilities")
@Tag(name = "Cơ Sở Y Tế", description = "API lấy thông tin cơ sở y tế")
@RequiredArgsConstructor
public class CoSoYTeController {

    private final CoSoYTeRepository coSoYTeRepository;

    @Operation(summary = "Lấy danh sách cơ sở y tế")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    @GetMapping
    public ResponseEntity<List<CoSoYTeResponse>> getAll() {
        List<CoSoYTeResponse> list = coSoYTeRepository.findAll().stream()
            .map(CoSoYTeResponse::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Lấy cơ sở y tế theo ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
            content = @Content(schema = @Schema(implementation = CoSoYTeResponse.class))),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CoSoYTeResponse> getById(@PathVariable Integer id) {
        CoSoYTe coSo = coSoYTeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cơ sở y tế"));
        return ResponseEntity.ok(CoSoYTeResponse.fromEntity(coSo));
    }

    @Operation(summary = "Lấy cơ sở y tế mặc định (đầu tiên)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công"),
        @ApiResponse(responseCode = "404", description = "Không có dữ liệu cơ sở y tế")
    })
    @GetMapping("/default")
    public ResponseEntity<CoSoYTeResponse> getDefault() {
        CoSoYTe coSo = coSoYTeRepository.findAll().stream()
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Không có cơ sở y tế nào"));
        return ResponseEntity.ok(CoSoYTeResponse.fromEntity(coSo));
    }
}


