-- ==========================================
-- SAMPLE DATA: LỊCH LÀM VIỆC & YÊU CẦU NGHỈ
-- ==========================================
-- Dữ liệu mẫu để test Phase 1
-- ==========================================

USE DatLichKham;

-- ==========================================
-- 1. LỊCH LÀM VIỆC MẶC ĐỊNH (FULL WEEK)
-- ==========================================
-- Xóa data cũ (nếu có)
DELETE FROM LichLamViecMacDinh;

-- Insert lịch mặc định: Thứ 2-8, mỗi ngày 2 ca (SANG + CHIEU)
INSERT INTO LichLamViecMacDinh (CoSoID, ThuTrongTuan, Ca, ThoiGianBatDau, ThoiGianKetThuc, IsActive, GhiChu, CreatedBy) VALUES
-- Thứ 2
(1, 2, 'SANG', '08:00:00', '12:00:00', 1, 'Lịch chuẩn Thứ 2', 1),
(1, 2, 'CHIEU', '14:00:00', '17:00:00', 1, 'Lịch chuẩn Thứ 2', 1),

-- Thứ 3
(1, 3, 'SANG', '08:00:00', '12:00:00', 1, 'Lịch chuẩn Thứ 3', 1),
(1, 3, 'CHIEU', '14:00:00', '17:00:00', 1, 'Lịch chuẩn Thứ 3', 1),

-- Thứ 4
(1, 4, 'SANG', '08:00:00', '12:00:00', 1, 'Lịch chuẩn Thứ 4', 1),
(1, 4, 'CHIEU', '14:00:00', '17:00:00', 1, 'Lịch chuẩn Thứ 4', 1),

-- Thứ 5
(1, 5, 'SANG', '08:00:00', '12:00:00', 1, 'Lịch chuẩn Thứ 5', 1),
(1, 5, 'CHIEU', '14:00:00', '17:00:00', 1, 'Lịch chuẩn Thứ 5', 1),

-- Thứ 6
(1, 6, 'SANG', '08:00:00', '12:00:00', 1, 'Lịch chuẩn Thứ 6', 1),
(1, 6, 'CHIEU', '14:00:00', '17:00:00', 1, 'Lịch chuẩn Thứ 6', 1),

-- Thứ 7
(1, 7, 'SANG', '08:00:00', '12:00:00', 1, 'Lịch chuẩn Thứ 7', 1),
(1, 7, 'CHIEU', '14:00:00', '17:00:00', 1, 'Lịch chuẩn Thứ 7', 1),

-- Chủ nhật (ThuTrongTuan = 8) ⭐
(1, 8, 'SANG', '08:00:00', '12:00:00', 1, 'Lịch Chủ nhật', 1),
(1, 8, 'CHIEU', '14:00:00', '17:00:00', 1, 'Lịch Chủ nhật', 1);

-- Verify
SELECT 
    ConfigID,
    CASE ThuTrongTuan
        WHEN 2 THEN 'Thứ 2'
        WHEN 3 THEN 'Thứ 3'
        WHEN 4 THEN 'Thứ 4'
        WHEN 5 THEN 'Thứ 5'
        WHEN 6 THEN 'Thứ 6'
        WHEN 7 THEN 'Thứ 7'
        WHEN 8 THEN 'Chủ nhật'
    END AS TenThu,
    Ca,
    CONCAT(TIME_FORMAT(ThoiGianBatDau, '%H:%i'), ' - ', TIME_FORMAT(ThoiGianKetThuc, '%H:%i')) AS ThoiGian,
    IsActive
FROM LichLamViecMacDinh
ORDER BY ThuTrongTuan, FIELD(Ca, 'SANG', 'CHIEU', 'TOI');

-- ==========================================
-- 2. YÊU CẦU NGHỈ MẪU (CHỈ CHẠY NẾU ĐÃ CÓ BÁC SĨ)
-- ==========================================
-- Uncomment và thay BacSiID phù hợp

-- Example 1: Bác sĩ nghỉ mỗi Chủ nhật (hàng tuần)
/*
INSERT INTO BacSiNgayNghi (
    BacSiID, 
    LoaiNghi, 
    ThuTrongTuan, 
    Ca, 
    LyDo, 
    LoaiNghiPhep, 
    TrangThai, 
    NguoiDuyet, 
    NgayDuyet
) VALUES (
    1,                          -- BacSiID (thay theo thực tế)
    'CA_HANG_TUAN',            -- Loại nghỉ
    8,                         -- Chủ nhật
    NULL,                      -- NULL = nghỉ cả ngày
    'Nghỉ Chủ nhật hàng tuần', -- Lý do
    'PHEP_NAM',                -- Loại phép
    'DA_DUYET',                -- Đã duyệt
    1,                         -- Admin ID
    NOW()                      -- Ngày duyệt
);
*/

-- Example 2: Bác sĩ nghỉ ca CHIEU mỗi Thứ 6
/*
INSERT INTO BacSiNgayNghi (
    BacSiID, 
    LoaiNghi, 
    ThuTrongTuan, 
    Ca, 
    LyDo, 
    LoaiNghiPhep, 
    TrangThai
) VALUES (
    2,                              -- BacSiID
    'CA_HANG_TUAN',                -- Loại nghỉ
    6,                             -- Thứ 6
    'CHIEU',                       -- Ca CHIEU
    'Không làm ca chiều Thứ 6',   -- Lý do
    'PHEP_NAM',                    -- Loại phép
    'CHO_DUYET'                    -- Chờ duyệt
);
*/

-- Example 3: Bác sĩ nghỉ ngày cụ thể (25/12/2025 - Giáng sinh)
/*
INSERT INTO BacSiNgayNghi (
    BacSiID, 
    LoaiNghi, 
    NgayNghiCuThe, 
    Ca, 
    LyDo, 
    LoaiNghiPhep, 
    TrangThai
) VALUES (
    3,                          -- BacSiID
    'NGAY_CU_THE',             -- Loại nghỉ
    '2025-12-25',              -- Ngày Giáng sinh
    NULL,                      -- NULL = nghỉ cả ngày
    'Nghỉ lễ Giáng sinh',     -- Lý do
    'PHEP_NAM',                -- Loại phép
    'CHO_DUYET'                -- Chờ duyệt
);
*/

-- Example 4: Bác sĩ nghỉ ốm 1 ca cụ thể
/*
INSERT INTO BacSiNgayNghi (
    BacSiID, 
    LoaiNghi, 
    NgayNghiCuThe, 
    Ca, 
    LyDo, 
    LoaiNghiPhep, 
    TrangThai,
    FileDinhKem
) VALUES (
    4,                              -- BacSiID
    'CA_CU_THE',                   -- Loại nghỉ
    '2025-12-01',                  -- Ngày cụ thể
    'SANG',                        -- Ca SANG
    'Đi khám bệnh',               -- Lý do
    'OM',                          -- Nghỉ ốm (không trừ phép)
    'CHO_DUYET',                   -- Chờ duyệt
    '/uploads/giay-kham-benh.pdf'  -- File đính kèm
);
*/

-- ==========================================
-- QUERIES HỮU ÍCH
-- ==========================================

-- Query 1: Xem yêu cầu nghỉ chờ duyệt
/*
SELECT 
    n.NghiID,
    b.HoTen AS TenBacSi,
    n.LoaiNghi,
    CASE 
        WHEN n.LoaiNghi = 'NGAY_CU_THE' THEN CONCAT('Ngày ', DATE_FORMAT(n.NgayNghiCuThe, '%d/%m/%Y'))
        WHEN n.LoaiNghi = 'CA_CU_THE' THEN CONCAT(DATE_FORMAT(n.NgayNghiCuThe, '%d/%m/%Y'), ' - ', n.Ca)
        WHEN n.LoaiNghi = 'CA_HANG_TUAN' THEN CONCAT('Mỗi ', 
            CASE n.ThuTrongTuan
                WHEN 2 THEN 'Thứ 2'
                WHEN 3 THEN 'Thứ 3'
                WHEN 4 THEN 'Thứ 4'
                WHEN 5 THEN 'Thứ 5'
                WHEN 6 THEN 'Thứ 6'
                WHEN 7 THEN 'Thứ 7'
                WHEN 8 THEN 'Chủ nhật'
            END,
            IF(n.Ca IS NULL, ' (cả ngày)', CONCAT(' - ', n.Ca))
        )
    END AS ThoiGianNghi,
    n.LyDo,
    n.LoaiNghiPhep,
    n.TrangThai,
    n.CreatedAt
FROM BacSiNgayNghi n
JOIN BacSi bs ON n.BacSiID = bs.BacSiID
JOIN NguoiDung b ON bs.BacSiID = b.NguoiDungID
WHERE n.TrangThai = 'CHO_DUYET'
ORDER BY n.CreatedAt ASC;
*/

-- Query 2: Thống kê ngày phép của bác sĩ
/*
SELECT 
    b.BacSiID,
    u.HoTen,
    b.SoNgayPhepNam AS TongPhep,
    b.SoNgayPhepDaSuDung AS DaSuDung,
    (b.SoNgayPhepNam - b.SoNgayPhepDaSuDung) AS ConLai,
    COUNT(n.NghiID) AS SoLanXinNghi,
    SUM(CASE WHEN n.TrangThai = 'DA_DUYET' THEN 1 ELSE 0 END) AS SoLanDuocDuyet
FROM BacSi b
JOIN NguoiDung u ON b.BacSiID = u.NguoiDungID
LEFT JOIN BacSiNgayNghi n ON b.BacSiID = n.BacSiID
WHERE b.IsDeleted = 0
GROUP BY b.BacSiID
ORDER BY ConLai ASC;
*/

