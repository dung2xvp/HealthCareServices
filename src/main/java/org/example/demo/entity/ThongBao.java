package org.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ThongBao", indexes = {
    @Index(name = "idx_nguoi_dung", columnList = "NguoiDungID"),
    @Index(name = "idx_da_doc", columnList = "DaDoc"),
    @Index(name = "idx_created", columnList = "CreatedAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThongBao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ThongBaoID")
    private Integer thongBaoID;
    
    @ManyToOne
    @JoinColumn(name = "NguoiDungID", nullable = false)
    private NguoiDung nguoiDung;
    
    @Column(name = "LoaiThongBao", length = 50)
    private String loaiThongBao;
    
    @Column(name = "TieuDe", nullable = false)
    private String tieuDe;
    
    @Column(name = "NoiDung", columnDefinition = "TEXT")
    private String noiDung;
    
    @Column(name = "LienKetID")
    private Integer lienKetID;
    
    @Column(name = "DaDoc")
    private Boolean daDoc = false;
    
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
