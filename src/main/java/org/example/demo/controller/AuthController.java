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
import org.example.demo.dto.request.LoginRequest;
import org.example.demo.dto.request.RegisterRequest;
import org.example.demo.dto.request.UpdateUserRequest;
import org.example.demo.dto.response.AuthResponse;
import org.example.demo.dto.response.ErrorResponse;
import org.example.demo.dto.response.MessageResponse;
import org.example.demo.dto.response.UserResponse;
import org.example.demo.security.CustomUserDetails;
import org.example.demo.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController - REST API cho Authentication
 * 
 * Endpoints:
 * - POST /api/auth/register - Đăng ký bệnh nhân
 * - POST /api/auth/login - Đăng nhập
 * - POST /api/auth/verify-email - Xác thực email
 * - POST /api/auth/resend-code - Gửi lại mã xác thực
 * - POST /api/auth/forgot-password - Quên mật khẩu
 * - POST /api/auth/reset-password - Đặt lại mật khẩu
 * - PUT /api/auth/change-password - Đổi mật khẩu
 * - GET /api/auth/me - Lấy thông tin user hiện tại
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API quản lý đăng ký, đăng nhập và xác thực người dùng")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Đăng ký tài khoản bệnh nhân
     */
    @Operation(summary = "Đăng ký tài khoản bệnh nhân", 
               description = "Tạo tài khoản mới cho bệnh nhân. Sau khi đăng ký, mã xác thực sẽ được gửi qua email.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Đăng ký thành công",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email đã tồn tại",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(
            new MessageResponse("Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.")
        );
    }

    /**
     * Đăng nhập
     */
    @Operation(summary = "Đăng nhập", 
               description = "Đăng nhập bằng email và mật khẩu. Trả về JWT token để sử dụng cho các API khác.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Đăng nhập thành công",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Email hoặc mật khẩu không đúng",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Xác thực email
     */
    @Operation(summary = "Xác thực email", 
               description = "Xác thực tài khoản bằng mã được gửi qua email.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Xác thực thành công",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Mã xác thực không hợp lệ hoặc đã hết hạn",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(
            @Parameter(description = "Email người dùng") @RequestParam String email,
            @Parameter(description = "Mã xác thực") @RequestParam String code) {
        authService.verifyEmail(email, code);
        return ResponseEntity.ok(
            new MessageResponse("Xác thực email thành công. Bạn có thể đăng nhập ngay bây giờ.")
        );
    }

    /**
     * Gửi lại mã xác thực
     */
    @Operation(summary = "Gửi lại mã xác thực", 
               description = "Gửi lại mã xác thực qua email nếu mã cũ đã hết hạn hoặc bị mất.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Gửi mã thành công",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/resend-code")
    public ResponseEntity<MessageResponse> resendCode(
            @Parameter(description = "Email người dùng") @RequestParam String email) {
        authService.resendVerificationCode(email);
        return ResponseEntity.ok(
            new MessageResponse("Mã xác thực đã được gửi lại. Vui lòng kiểm tra email.")
        );
    }

    /**
     * Quên mật khẩu - Gửi mã reset qua email
     */
    @Operation(summary = "Quên mật khẩu", 
               description = "Gửi mã đặt lại mật khẩu qua email.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Gửi mã thành công",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Parameter(description = "Email người dùng") @RequestParam String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok(
            new MessageResponse("Mã đặt lại mật khẩu đã được gửi qua email.")
        );
    }

    /**
     * Đặt lại mật khẩu với mã từ email
     */
    @Operation(summary = "Đặt lại mật khẩu", 
               description = "Đặt lại mật khẩu mới bằng mã được gửi qua email.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Đặt lại mật khẩu thành công",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Mã không hợp lệ hoặc đã hết hạn",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Parameter(description = "Email người dùng") @RequestParam String email,
            @Parameter(description = "Mã đặt lại mật khẩu") @RequestParam String code,
            @Parameter(description = "Mật khẩu mới") @RequestParam String newPassword) {
        authService.resetPassword(email, code, newPassword);
        return ResponseEntity.ok(
            new MessageResponse("Đặt lại mật khẩu thành công. Bạn có thể đăng nhập với mật khẩu mới.")
        );
    }

    /**
     * Đổi mật khẩu (cần đăng nhập)
     */
    @Operation(summary = "Đổi mật khẩu", 
               description = "Đổi mật khẩu khi đã đăng nhập. Cần cung cấp mật khẩu cũ để xác thực.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Mật khẩu cũ không đúng",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            Authentication authentication,
            @Parameter(description = "Mật khẩu cũ") @RequestParam String oldPassword,
            @Parameter(description = "Mật khẩu mới") @RequestParam String newPassword) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();
        
        authService.changePassword(email, oldPassword, newPassword);
        return ResponseEntity.ok(
            new MessageResponse("Đổi mật khẩu thành công.")
        );
    }

    /**
     * Lấy thông tin user hiện tại
     */
    @Operation(summary = "Lấy thông tin người dùng hiện tại", 
               description = "Lấy thông tin của người dùng đang đăng nhập.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UserResponse userResponse = authService.getCurrentUser();
        return ResponseEntity.ok(userResponse);
    }

    /**
     * Cập nhật thông tin người dùng hiện tại
     */
    @Operation(summary = "Cập nhật thông tin cá nhân",
               description = "Cập nhật họ tên, số điện thoại, địa chỉ, ngày sinh, giới tính, avatar",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse userResponse = authService.updateCurrentUser(request);
        return ResponseEntity.ok(userResponse);
    }
}

