package org.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.demo.enums.VaiTro;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "NguoiDung", indexes = {
    @Index(name = "idx_email", columnList = "Email"),
    @Index(name = "idx_vai_tro", columnList = "VaiTro"),
    @Index(name = "idx_trang_thai", columnList = "TrangThai")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class NguoiDung extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NguoiDungID")
    private Integer nguoiDungID;
    
    @Column(name = "HoTen", length = 100)
    private String hoTen;
    
    @Column(name = "Email", unique = true, nullable = false, length = 100)
    private String email;
    
    @Column(name = "MatKhau", nullable = false)
    private String matKhau;
    
    @Column(name = "SoDienThoai", length = 20)
    private String soDienThoai;
    
    @Column(name = "DiaChi", length = 200)
    private String diaChi;
    
    @Column(name = "NgaySinh")
    private LocalDate ngaySinh;
    
    @Column(name = "GioiTinh")
    private Integer gioiTinh = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "VaiTro", nullable = false)
    private VaiTro vaiTro;
    
    @Column(name = "TrangThai")
    private Boolean trangThai = true;
    
    @Column(name = "AvatarUrl")
    private String avatarUrl;

    @Column(name = "VerificationCode", length = 6)
    private String verificationCode;

    @Column(name = "CodeExpiry")
    private LocalDateTime codeExpiry;

    @Column(name = "BadPoint")
    private Integer badPoint = 0;
}
