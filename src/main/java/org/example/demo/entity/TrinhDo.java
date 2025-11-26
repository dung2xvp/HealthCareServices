package org.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "TrinhDo")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TrinhDo extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TrinhDoID")
    private Integer trinhDoID;
    
    @Column(name = "TenTrinhDo", length = 100)
    private String tenTrinhDo;
    
    @Column(name = "MoTa", length = 200)
    private String moTa;
    
    @Column(name = "GiaKham", nullable = false, precision = 10, scale = 2)
    private BigDecimal giaKham = new BigDecimal("150000");

    @Column(name = "ThuTuUuTien")
    private Integer thuTuUuTien = 0;
}
