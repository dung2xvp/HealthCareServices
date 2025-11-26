package org.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "BacSi_NgayNghi", indexes = {
    @Index(name = "idx_bac_si_ngay", columnList = "BacSiID,NgayBatDau,NgayKetThuc")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BacSiNgayNghi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NgayNghiID")
    private Integer ngayNghiID;
    
    @ManyToOne
    @JoinColumn(name = "BacSiID", nullable = false)
    private BacSi bacSi;
    
    @Column(name = "NgayBatDau", nullable = false)
    private LocalDate ngayBatDau;
    
    @Column(name = "NgayKetThuc", nullable = false)
    private LocalDate ngayKetThuc;
    
    @Column(name = "LyDo")
    private String lyDo;
    
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "IsDeleted")
    private Boolean isDeleted = false;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
