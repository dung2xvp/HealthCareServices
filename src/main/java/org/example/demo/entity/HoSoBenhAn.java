package org.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "HoSoBenhAn")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoSoBenhAn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HoSoID")
    private Integer hoSoID;
    
    @OneToOne
    @JoinColumn(name = "BenhNhanID", nullable = false, unique = true)
    private NguoiDung benhNhan;
    
    // Thông tin y tế cơ bản
    @Column(name = "NhomMau", length = 10)
    private String nhomMau;
    
    @Column(name = "ChieuCao", precision = 5, scale = 2)
    private BigDecimal chieuCao;
    
    @Column(name = "CanNang", precision = 5, scale = 2)
    private BigDecimal canNang;
    
    // Tiền sử bệnh
    @Column(name = "DiUng", columnDefinition = "TEXT")
    private String diUng;
    
    @Column(name = "BenhManTinh", columnDefinition = "TEXT")
    private String benhManTinh;
    
    @Column(name = "PhauThuatDaQua", columnDefinition = "TEXT")
    private String phauThuatDaQua;
    
    @Column(name = "TienSuGiaDinh", columnDefinition = "TEXT")
    private String tienSuGiaDinh;
    
    // Thói quen
    @Column(name = "HutThuoc")
    private Boolean hutThuoc = false;
    
    @Column(name = "UongRuou")
    private Boolean uongRuou = false;
    
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
