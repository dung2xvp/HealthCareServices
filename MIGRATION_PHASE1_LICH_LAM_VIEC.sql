-- ==========================================
-- MIGRATION SCRIPT: PHASE 1 - LỊCH LÀM VIỆC
-- ==========================================
-- Mục đích:
-- 1. Thay thế BacSi_LichLamViec (lịch riêng) → LichLamViecMacDinh (lịch chung)
-- 2. Thay thế BacSi_NgayNghi (khoảng thời gian) → BacSiNgayNghi (approval workflow)
-- 3. Thêm quản lý ngày phép vào BacSi
-- ==========================================

USE DatLichKham;

-- ==========================================
-- BƯỚC 1: BACKUP DATA CŨ (NẾU CẦN)
-- ==========================================
-- Tạo bảng backup trước khi xóa
CREATE TABLE IF NOT EXISTS BacSi_LichLamViec_Backup AS SELECT * FROM BacSi_LichLamViec;
CREATE TABLE IF NOT EXISTS BacSi_NgayNghi_Backup AS SELECT * FROM BacSi_NgayNghi;

-- ==========================================
-- BƯỚC 2: XÓA TABLES CŨ
-- ==========================================
-- Xóa tables cũ (không dùng nữa)
DROP TABLE IF EXISTS BacSi_NgayNghi;
DROP TABLE IF EXISTS BacSi_LichLamViec;

-- ==========================================
-- BƯỚC 3: CẬP NHẬT TABLE BACSI (THÊM NGÀY PHÉP)
-- ==========================================
ALTER TABLE BacSi
ADD COLUMN SoNgayPhepNam INT DEFAULT 12 COMMENT 'Tổng số ngày phép/năm',
ADD COLUMN SoNgayPhepDaSuDung INT DEFAULT 0 COMMENT 'Số ngày phép đã sử dụng',
ADD COLUMN NamApDung INT COMMENT 'Năm áp dụng (reset đầu năm)';

-- Update năm áp dụng cho bác sĩ hiện có
UPDATE BacSi SET NamApDung = YEAR(CURDATE()) WHERE NamApDung IS NULL;

-- ==========================================
-- BƯỚC 4: TẠO TABLE LỊCH LÀM VIỆC MẶC ĐỊNH
-- ==========================================
CREATE TABLE LichLamViecMacDinh (
    ConfigID INT AUTO_INCREMENT PRIMARY KEY,
    CoSoID INT NOT NULL DEFAULT 1,
    
    -- Thời gian
    ThuTrongTuan INT NOT NULL COMMENT '2=Thứ 2, 3=Thứ 3, ..., 8=Chủ nhật',
    Ca VARCHAR(20) NOT NULL COMMENT 'SANG, CHIEU, TOI',
    ThoiGianBatDau TIME NOT NULL COMMENT 'VD: 08:00:00',
    ThoiGianKetThuc TIME NOT NULL COMMENT 'VD: 12:00:00',
    
    -- Status
    IsActive BIT DEFAULT 1 COMMENT 'Đang áp dụng hay không',
    GhiChu VARCHAR(500),
    
    -- Audit fields
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    UpdatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CreatedBy INT,
    UpdatedBy INT,
    IsDeleted BIT DEFAULT 0,
    DeletedAt DATETIME,
    DeletedBy INT,
    
    FOREIGN KEY (CoSoID) REFERENCES CoSoYTe(CoSoID),
    
    -- Constraints
    CONSTRAINT chk_thu CHECK (ThuTrongTuan BETWEEN 2 AND 8),
    CONSTRAINT chk_ca CHECK (Ca IN ('SANG', 'CHIEU', 'TOI')),
    CONSTRAINT chk_time CHECK (ThoiGianKetThuc > ThoiGianBatDau),
    
    -- Unique: Không được trùng (CoSoID + Thu + Ca)
    UNIQUE KEY unique_schedule (CoSoID, ThuTrongTuan, Ca),
    
    -- Indexes
    INDEX idx_active (IsActive),
    INDEX idx_thu (ThuTrongTuan)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- BƯỚC 5: TẠO TABLE YÊU CẦU NGHỈ MỚI
-- ==========================================
CREATE TABLE BacSiNgayNghi (
    NghiID INT AUTO_INCREMENT PRIMARY KEY,
    BacSiID INT NOT NULL,
    
    -- ===== LOẠI NGHỈ =====
    LoaiNghi VARCHAR(20) NOT NULL COMMENT 'NGAY_CU_THE, CA_CU_THE, CA_HANG_TUAN',
    
    -- ===== THỜI GIAN NGHỈ =====
    NgayNghiCuThe DATE COMMENT 'Dùng cho NGAY_CU_THE, CA_CU_THE',
    ThuTrongTuan INT COMMENT 'Dùng cho CA_HANG_TUAN (2-8)',
    Ca VARCHAR(20) COMMENT 'SANG, CHIEU, TOI hoặc NULL (cả ngày)',
    
    -- ===== THÔNG TIN ĐƠN =====
    LyDo VARCHAR(500) NOT NULL COMMENT 'Lý do xin nghỉ',
    LoaiNghiPhep VARCHAR(20) NOT NULL DEFAULT 'PHEP_NAM' COMMENT 'PHEP_NAM, OM, CONG_TAC, KHAC',
    FileDinhKem VARCHAR(255) COMMENT 'Path đến file đơn xin nghỉ',
    
    -- ===== APPROVAL WORKFLOW =====
    TrangThai VARCHAR(20) NOT NULL DEFAULT 'CHO_DUYET' COMMENT 'CHO_DUYET, DA_DUYET, TU_CHOI, HUY',
    NguoiDuyet INT COMMENT 'Admin ID',
    NgayDuyet DATETIME COMMENT 'Thời điểm duyệt',
    LyDoTuChoi VARCHAR(500) COMMENT 'Lý do từ chối',
    
    -- ===== AUDIT =====
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    UpdatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CreatedBy INT,
    UpdatedBy INT,
    IsDeleted BIT DEFAULT 0,
    DeletedAt DATETIME,
    DeletedBy INT,
    
    FOREIGN KEY (BacSiID) REFERENCES BacSi(BacSiID) ON DELETE CASCADE,
    FOREIGN KEY (NguoiDuyet) REFERENCES NguoiDung(NguoiDungID),
    
    -- Constraints
    CONSTRAINT chk_loai_nghi CHECK (LoaiNghi IN ('NGAY_CU_THE', 'CA_CU_THE', 'CA_HANG_TUAN')),
    CONSTRAINT chk_ca_nghi CHECK (Ca IS NULL OR Ca IN ('SANG', 'CHIEU', 'TOI')),
    CONSTRAINT chk_trang_thai_nghi CHECK (TrangThai IN ('CHO_DUYET', 'DA_DUYET', 'TU_CHOI', 'HUY')),
    CONSTRAINT chk_loai_phep CHECK (LoaiNghiPhep IN ('PHEP_NAM', 'OM', 'CONG_TAC', 'KHAC')),
    
    -- Indexes
    INDEX idx_bacsi_trangthai (BacSiID, TrangThai),
    INDEX idx_pending (TrangThai, CreatedAt),
    INDEX idx_check_nghi_ngay (BacSiID, TrangThai, NgayNghiCuThe),
    INDEX idx_check_nghi_tuan (BacSiID, TrangThai, ThuTrongTuan)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- BƯỚC 6: INSERT DATA MẪU - LỊCH MẶC ĐỊNH
-- ==========================================
-- Lịch làm việc toàn bệnh viện: Thứ 2-8 (Chủ nhật), mỗi ngày 2 ca (SANG + CHIEU)

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

-- Chủ nhật (ThuTrongTuan = 8) ⭐ QUAN TRỌNG
(1, 8, 'SANG', '08:00:00', '12:00:00', 1, 'Lịch Chủ nhật', 1),
(1, 8, 'CHIEU', '14:00:00', '17:00:00', 1, 'Lịch Chủ nhật', 1);

-- ⚠️ Kết quả: 14 ca/tuần (7 ngày x 2 ca)
-- ⚠️ Áp dụng cho TẤT CẢ bác sĩ
-- ⚠️ Bác sĩ muốn nghỉ → Đăng ký trong BacSiNgayNghi

-- ==========================================
-- BƯỚC 7: DATA MẪU - YÊU CẦU NGHỈ
-- ==========================================
-- Ví dụ: Bác sĩ ID 1 nghỉ mỗi Chủ nhật
-- (Chỉ chạy nếu đã có bác sĩ trong DB)

-- INSERT INTO BacSiNgayNghi (BacSiID, LoaiNghi, ThuTrongTuan, Ca, LyDo, LoaiNghiPhep, TrangThai, NguoiDuyet, NgayDuyet) VALUES
-- (1, 'CA_HANG_TUAN', 8, NULL, 'Nghỉ Chủ nhật hàng tuần', 'PHEP_NAM', 'DA_DUYET', 1, NOW());

-- ==========================================
-- VERIFY
-- ==========================================
-- Kiểm tra lịch mặc định
SELECT 
    ConfigID,
    ThuTrongTuan,
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
    ThoiGianBatDau,
    ThoiGianKetThuc,
    IsActive
FROM LichLamViecMacDinh
ORDER BY ThuTrongTuan, 
    FIELD(Ca, 'SANG', 'CHIEU', 'TOI');

-- Kiểm tra fields mới trong BacSi
SELECT 
    BacSiID,
    SoNgayPhepNam,
    SoNgayPhepDaSuDung,
    (SoNgayPhepNam - SoNgayPhepDaSuDung) AS SoNgayPhepConLai,
    NamApDung
FROM BacSi
LIMIT 5;

-- ==========================================
-- GHI CHÚ
-- ==========================================
/*
KIẾN TRÚC MỚI:
--------------
1. LichLamViecMacDinh: Lịch chung cho toàn bệnh viện
   - Admin setup 1 lần
   - Áp dụng cho TẤT CẢ bác sĩ
   - Thứ 2-8 (Chủ nhật): Ca SANG + CHIEU

2. BacSiNgayNghi: Yêu cầu nghỉ của bác sĩ (Exception)
   - Bác sĩ tạo yêu cầu nghỉ
   - Admin phê duyệt (CHO_DUYET → DA_DUYET)
   - 3 loại: NGAY_CU_THE, CA_CU_THE, CA_HANG_TUAN

3. BacSi: Thêm quản lý ngày phép
   - SoNgayPhepNam: 12 ngày
   - SoNgayPhepDaSuDung: Đã dùng bao nhiêu
   - Computed: SoNgayPhepConLai = PhepNam - DaSuDung

LOGIC AVAILABLE SLOTS:
---------------------
Available Slots = Lịch Mặc Định - Nghỉ Đã Duyệt - Lịch Đã Đặt

VD: Bác sĩ A vào Thứ 2:
1. Lịch mặc định: SANG (8-12), CHIEU (14-17)
2. BS A nghỉ ca CHIEU (đã duyệt) → Bỏ ca CHIEU
3. Kết quả: Chỉ có SANG (8-12) available
4. Chia slots: 8:00, 8:30, 9:00, 9:30, 10:00, 10:30, 11:00, 11:30
5. Bỏ slots đã đặt
6. Return slots còn lại
*/

-- ==========================================
-- CLEANUP (TÙY CHỌN)
-- ==========================================
-- Xóa backup nếu migration thành công
-- DROP TABLE IF EXISTS BacSi_LichLamViec_Backup;
-- DROP TABLE IF EXISTS BacSi_NgayNghi_Backup;

