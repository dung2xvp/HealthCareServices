package org.example.demo.service;

import org.example.demo.dto.request.LoginRequest;
import org.example.demo.dto.request.RegisterRequest;
import org.example.demo.dto.request.UpdateUserRequest;
import org.example.demo.dto.response.AuthResponse;
import org.example.demo.dto.response.UserResponse;
import org.example.demo.entity.NguoiDung;
import org.example.demo.enums.VaiTro;
import org.example.demo.exception.BadRequestException;
import org.example.demo.exception.ConflictException;
import org.example.demo.exception.ResourceNotFoundException;
import org.example.demo.exception.UnauthorizedException;
import org.example.demo.repository.NguoiDungRepository;
import org.example.demo.security.CustomUserDetails;
import org.example.demo.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * AuthService - Business logic cho Authentication
 * 
 * Chức năng:
 * 1. Đăng ký tài khoản bệnh nhân
 * 2. Đăng nhập
 * 3. Xác thực email
 * 4. Quên mật khẩu / Reset password
 * 5. Đổi mật khẩu
 */
@Service
public class AuthService {

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private EmailService emailService;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    /**
     * Đăng ký tài khoản BỆNH NHÂN
     */
    @Transactional
    public void register(RegisterRequest request) {
        // 1. Kiểm tra email đã tồn tại chưa
        if (nguoiDungRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email đã được sử dụng");
        }

        // 2. Tạo NguoiDung entity
        NguoiDung nguoiDung = new NguoiDung();
        nguoiDung.setHoTen(request.getHoTen());
        nguoiDung.setEmail(request.getEmail());
        nguoiDung.setMatKhau(passwordEncoder.encode(request.getPassword())); // Hash password
        nguoiDung.setSoDienThoai(request.getSoDienThoai());
        nguoiDung.setDiaChi(request.getDiaChi());
        nguoiDung.setNgaySinh(request.getNgaySinh());
        nguoiDung.setGioiTinh(request.getGioiTinh());
        nguoiDung.setVaiTro(VaiTro.BenhNhan); // Mặc định là bệnh nhân
        nguoiDung.setTrangThai(false); // Chưa verify email
        nguoiDung.setBadPoint(0);

        // 3. Generate verification code (6 số ngẫu nhiên)
        String verificationCode = generateVerificationCode();
        nguoiDung.setVerificationCode(verificationCode);
        nguoiDung.setCodeExpiry(LocalDateTime.now().plusMinutes(15)); // Hết hạn sau 15 phút

        // 4. Lưu vào database
        nguoiDungRepository.save(nguoiDung);

        // 5. Gửi email verification
        try {
            emailService.sendVerificationEmail(
                nguoiDung.getEmail(),
                nguoiDung.getHoTen(),
                verificationCode
            );
        } catch (Exception e) {
            // Log lỗi nhưng không throw để không block đăng ký
            System.err.println("⚠️ Failed to send verification email: " + e.getMessage());
            System.out.println("========================================");
            System.out.println("VERIFICATION CODE for " + nguoiDung.getEmail());
            System.out.println("Code: " + verificationCode);
            System.out.println("========================================");
        }
    }

    /**
     * Đăng nhập
     */
    public AuthResponse login(LoginRequest request) {
        // 1. Authenticate với Spring Security
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
        } catch (Exception e) {
            throw new UnauthorizedException("Email hoặc mật khẩu không chính xác");
        }

        // 2. Lấy user từ authentication
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        NguoiDung nguoiDung = userDetails.getNguoiDung();

        // 3. Kiểm tra đã verify email chưa
        if (!nguoiDung.getTrangThai()) {
            throw new UnauthorizedException("Tài khoản chưa được xác thực. Vui lòng kiểm tra email.");
        }

        // 4. Generate JWT token
        String token = jwtTokenProvider.generateToken(authentication);

        // 5. Convert NguoiDung → UserResponse
        UserResponse userResponse = convertToUserResponse(nguoiDung);

        // 6. Trả về AuthResponse
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs)
                .userInfo(userResponse)
                .build();
    }

    /**
     * Xác thực email
     */
    @Transactional
    public void verifyEmail(String email, String code) {
        // 1. Tìm user theo email
        NguoiDung nguoiDung = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        // 2. Kiểm tra mã xác thực
        if (!code.equals(nguoiDung.getVerificationCode())) {
            throw new BadRequestException("Mã xác thực không chính xác");
        }

        // 3. Kiểm tra mã còn hạn không
        if (nguoiDung.getCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Mã xác thực đã hết hạn. Vui lòng gửi lại mã mới.");
        }

        // 4. Xác thực thành công
        nguoiDung.setTrangThai(true);
        nguoiDung.setVerificationCode(null);
        nguoiDung.setCodeExpiry(null);
        nguoiDungRepository.save(nguoiDung);
    }

    /**
     * Gửi lại mã xác thực
     */
    @Transactional
    public void resendVerificationCode(String email) {
        // 1. Tìm user
        NguoiDung nguoiDung = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        // 2. Kiểm tra đã verify chưa
        if (nguoiDung.getTrangThai()) {
            throw new BadRequestException("Tài khoản đã được xác thực");
        }

        // 3. Generate mã mới
        String newCode = generateVerificationCode();
        nguoiDung.setVerificationCode(newCode);
        nguoiDung.setCodeExpiry(LocalDateTime.now().plusMinutes(15));
        nguoiDungRepository.save(nguoiDung);

        // 4. Gửi email
        try {
            emailService.resendVerificationEmail(
                nguoiDung.getEmail(),
                nguoiDung.getHoTen(),
                newCode
            );
        } catch (Exception e) {
            System.err.println("⚠️ Failed to resend verification email: " + e.getMessage());
            System.out.println("========================================");
            System.out.println("RESEND VERIFICATION CODE for " + nguoiDung.getEmail());
            System.out.println("Code: " + newCode);
            System.out.println("========================================");
        }
    }

    /**
     * Quên mật khẩu - Gửi mã reset qua email
     */
    @Transactional
    public void forgotPassword(String email) {
        // 1. Tìm user
        NguoiDung nguoiDung = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email này"));

        // 2. Generate reset code
        String resetCode = generateVerificationCode();
        nguoiDung.setVerificationCode(resetCode);
        nguoiDung.setCodeExpiry(LocalDateTime.now().plusMinutes(15));
        nguoiDungRepository.save(nguoiDung);

        // 3. Gửi email
        try {
            emailService.sendPasswordResetEmail(
                nguoiDung.getEmail(),
                nguoiDung.getHoTen(),
                resetCode
            );
        } catch (Exception e) {
            System.err.println("⚠️ Failed to send password reset email: " + e.getMessage());
            System.out.println("========================================");
            System.out.println("PASSWORD RESET CODE for " + nguoiDung.getEmail());
            System.out.println("Code: " + resetCode);
            System.out.println("========================================");
        }
    }

    /**
     * Đặt lại mật khẩu với mã từ email
     */
    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        // 1. Tìm user
        NguoiDung nguoiDung = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        // 2. Kiểm tra mã
        if (!code.equals(nguoiDung.getVerificationCode())) {
            throw new BadRequestException("Mã xác thực không chính xác");
        }

        // 3. Kiểm tra còn hạn không
        if (nguoiDung.getCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Mã xác thực đã hết hạn");
        }

        // 4. Đổi password
        nguoiDung.setMatKhau(passwordEncoder.encode(newPassword));
        nguoiDung.setVerificationCode(null);
        nguoiDung.setCodeExpiry(null);
        nguoiDungRepository.save(nguoiDung);
    }

    /**
     * Đổi mật khẩu (khi đã đăng nhập)
     */
    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        // 1. Tìm user
        NguoiDung nguoiDung = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        // 2. Verify old password
        if (!passwordEncoder.matches(oldPassword, nguoiDung.getMatKhau())) {
            throw new BadRequestException("Mật khẩu cũ không chính xác");
        }

        // 3. Kiểm tra new password khác old password
        if (oldPassword.equals(newPassword)) {
            throw new BadRequestException("Mật khẩu mới phải khác mật khẩu cũ");
        }

        // 4. Đổi password
        nguoiDung.setMatKhau(passwordEncoder.encode(newPassword));
        nguoiDungRepository.save(nguoiDung);
    }

    /**
     * Cập nhật thông tin người dùng đang đăng nhập
     */
    @Transactional
    public UserResponse updateCurrentUser(UpdateUserRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
            || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new UnauthorizedException("Bạn chưa đăng nhập");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Integer userId = userDetails.getNguoiDungID();

        NguoiDung nguoiDung = nguoiDungRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        // Không cho đổi email/vai trò
        nguoiDung.setHoTen(request.getHoTen());
        nguoiDung.setSoDienThoai(request.getSoDienThoai());
        nguoiDung.setDiaChi(request.getDiaChi());
        nguoiDung.setNgaySinh(request.getNgaySinh());
        if (request.getGioiTinh() != null) {
            nguoiDung.setGioiTinh(request.getGioiTinh());
        }
        nguoiDung.setAvatarUrl(request.getAvatarUrl());

        nguoiDungRepository.save(nguoiDung);
        return convertToUserResponse(nguoiDung);
    }

    /**
     * Lấy thông tin user hiện tại từ SecurityContext
     */
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() 
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new UnauthorizedException("Bạn chưa đăng nhập");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        NguoiDung nguoiDung = userDetails.getNguoiDung();

        return convertToUserResponse(nguoiDung);
    }

    /**
     * Generate verification code (6 số ngẫu nhiên)
     */
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6 digits: 100000-999999
        return String.valueOf(code);
    }

    /**
     * Convert NguoiDung Entity → UserResponse DTO
     */
    private UserResponse convertToUserResponse(NguoiDung nguoiDung) {
        return UserResponse.builder()
                .nguoiDungID(nguoiDung.getNguoiDungID())
                .hoTen(nguoiDung.getHoTen())
                .email(nguoiDung.getEmail())
                .soDienThoai(nguoiDung.getSoDienThoai())
                .diaChi(nguoiDung.getDiaChi())
                .ngaySinh(nguoiDung.getNgaySinh())
                .gioiTinh(nguoiDung.getGioiTinh())
                .vaiTro(nguoiDung.getVaiTro().name())
                .trangThai(nguoiDung.getTrangThai())
                .avatarUrl(nguoiDung.getAvatarUrl())
                .createdAt(nguoiDung.getCreatedAt())
                .build();
    }
}

