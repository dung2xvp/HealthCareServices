package org.example.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.demo.dto.request.UpdateMedicalRecordRequest;
import org.example.demo.dto.response.ApiResponseDTO;
import org.example.demo.dto.response.MedicalRecordResponse;
import org.example.demo.security.CustomUserDetails;
import org.example.demo.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medical-records")
@Tag(name = "Medical Records", description = "APIs quản lý hồ sơ bệnh án")
@SecurityRequirement(name = "bearerAuth")
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('BenhNhan')")
    @Operation(summary = "Xem hồ sơ của tôi")
    public ResponseEntity<ApiResponseDTO<MedicalRecordResponse>> getMyRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MedicalRecordResponse response = medicalRecordService.getOwnRecord(userDetails.getNguoiDungID());
        return ResponseEntity.ok(ApiResponseDTO.success(response, "Lấy hồ sơ thành công"));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAuthority('BenhNhan')")
    @Operation(summary = "Cập nhật hồ sơ của tôi")
    public ResponseEntity<ApiResponseDTO<MedicalRecordResponse>> updateMyRecord(
            @Valid @RequestBody UpdateMedicalRecordRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MedicalRecordResponse response = medicalRecordService.updateOwnRecord(userDetails.getNguoiDungID(), request);
        return ResponseEntity.ok(ApiResponseDTO.success(response, "Cập nhật hồ sơ thành công"));
    }

    @GetMapping("/patient/{id}")
    @PreAuthorize("hasAnyAuthority('BacSi', 'Admin')")
    @Operation(summary = "Xem hồ sơ của bệnh nhân (bác sĩ/admin)")
    public ResponseEntity<ApiResponseDTO<MedicalRecordResponse>> getRecordByPatient(
            @PathVariable("id") Integer patientId
    ) {
        MedicalRecordResponse response = medicalRecordService.getRecordByPatient(patientId);
        return ResponseEntity.ok(ApiResponseDTO.success(response, "Lấy hồ sơ thành công"));
    }

    @PutMapping("/patient/{id}")
    @PreAuthorize("hasAnyAuthority('BacSi', 'Admin')")
    @Operation(summary = "Cập nhật hồ sơ bệnh nhân (bác sĩ/admin)")
    public ResponseEntity<ApiResponseDTO<MedicalRecordResponse>> updateRecordByDoctor(
            @PathVariable("id") Integer patientId,
            @Valid @RequestBody UpdateMedicalRecordRequest request
    ) {
        MedicalRecordResponse response = medicalRecordService.updateRecordByDoctor(patientId, request);
        return ResponseEntity.ok(ApiResponseDTO.success(response, "Cập nhật hồ sơ thành công"));
    }
}

