package org.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "ChuyenKhoa")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChuyenKhoa extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ChuyenKhoaID")
    private Integer chuyenKhoaID;
    
    @ManyToOne
    @JoinColumn(name = "CoSoID", nullable = false)
    private CoSoYTe coSoYTe;
    
    @Column(name = "TenChuyenKhoa", nullable = false, length = 100)
    private String tenChuyenKhoa;

    @Column(name = "MoTa", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "AnhDaiDien")
    private String anhDaiDien;

    @Column(name = "ThuTuHienThi")
    private Integer thuTuHienThi = 0;

    @OneToMany(mappedBy = "chuyenKhoa", cascade = CascadeType.ALL)
    private List<BacSi> bacSis;
}
