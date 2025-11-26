package org.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "CoSoYTe")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CoSoYTe extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CoSoID")
    private Integer coSoID;
    
    @Column(name = "TenCoSo", nullable = false, length = 200)
    private String tenCoSo;
    
    @Column(name = "DiaChi", length = 255)
    private String diaChi;
    
    @Column(name = "SoDienThoai", length = 20)
    private String soDienThoai;
    
    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "Website")
    private String website;

    @Column(name = "MoTa", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "AnhDaiDien")
    private String anhDaiDien;

    @Column(name = "Logo")
    private String logo;

    @Column(name = "GioLamViec", length = 100)
    private String gioLamViec;

    @Column(name = "NgayLamViec", length = 100)
    private String ngayLamViec;

    @OneToMany(mappedBy = "coSoYTe", cascade = CascadeType.ALL)
    private List<ChuyenKhoa> chuyenKhoas;
}
