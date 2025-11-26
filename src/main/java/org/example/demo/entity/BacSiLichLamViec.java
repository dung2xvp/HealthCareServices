package org.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.demo.enums.CaLamViec;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "BacSi_LichLamViec", 
    uniqueConstraints = @UniqueConstraint(name = "unique_schedule", columnNames = {"BacSiID", "ThuTrongTuan", "CaLamViec"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BacSiLichLamViec {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LichLamViecID")
    private Integer lichLamViecID;
    
    @ManyToOne
    @JoinColumn(name = "BacSiID", nullable = false)
    private BacSi bacSi;
    
    @Column(name = "ThuTrongTuan", nullable = false)
    private Integer thuTrongTuan;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "CaLamViec", nullable = false)
    private CaLamViec caLamViec;
    
    @Column(name = "GioBatDau", nullable = false)
    private LocalTime gioBatDau;
    
    @Column(name = "GioKetThuc", nullable = false)
    private LocalTime gioKetThuc;
    
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
    
    @Column(name = "IsDeleted")
    private Boolean isDeleted = false;
    
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
