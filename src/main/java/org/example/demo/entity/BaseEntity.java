package org.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@MappedSuperclass
@Data
public abstract class BaseEntity {
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
    
    @Column(name = "CreatedBy")
    private Integer createdBy;
    
    @Column(name = "UpdatedBy")
    private Integer updatedBy;
    
    @Column(name = "IsDeleted")
    private Boolean isDeleted = false;
    
    @Column(name = "DeletedAt")
    private LocalDateTime deletedAt;
    
    @Column(name = "DeletedBy")
    private Integer deletedBy;
    
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
