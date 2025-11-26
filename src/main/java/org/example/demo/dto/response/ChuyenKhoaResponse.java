package org.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChuyenKhoaResponse {
    private Integer chuyenKhoaID;
    private String tenChuyenKhoa;
    private String moTa;
    private String anhDaiDien;
    private Integer thuTuHienThi;
}

