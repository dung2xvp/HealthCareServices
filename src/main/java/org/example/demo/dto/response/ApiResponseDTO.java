package org.example.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Generic API Response wrapper
 * 
 * Chuẩn hóa format response cho tất cả APIs
 * 
 * @param <T> Type of data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Generic API Response")
public class ApiResponseDTO<T> {
    
    @Schema(description = "Success status", example = "true")
    private Boolean success;
    
    @Schema(description = "Message", example = "Operation successful")
    private String message;
    
    @Schema(description = "Response data")
    private T data;
    
    @Schema(description = "Error code (if failed)", example = "VALIDATION_ERROR")
    private String errorCode;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Timestamp", example = "2025-12-03 10:30:00")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Success response with data and message
     */
    public static <T> ApiResponseDTO<T> success(T data, String message) {
        return ApiResponseDTO.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * Success response with data only
     */
    public static <T> ApiResponseDTO<T> success(T data) {
        return success(data, "Success");
    }
    
    /**
     * Success response with message only
     */
    public static <T> ApiResponseDTO<T> success(String message) {
        return ApiResponseDTO.<T>builder()
            .success(true)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * Error response with message and error code
     */
    public static <T> ApiResponseDTO<T> error(String message, String errorCode) {
        return ApiResponseDTO.<T>builder()
            .success(false)
            .message(message)
            .errorCode(errorCode)
            .timestamp(LocalDateTime.now())
            .build();
    }
    
    /**
     * Error response with message only
     */
    public static <T> ApiResponseDTO<T> error(String message) {
        return error(message, "ERROR");
    }
}

