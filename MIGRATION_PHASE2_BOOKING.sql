-- ==========================================
-- MIGRATION SCRIPT FOR PHASE 2: BOOKING SYSTEM
-- ==========================================
-- Date: 2025-11-30
-- Description: Migrate from LichDatKham to DatLichKham with full Phase 2 features
--              - VNPay integration
--              - Doctor confirmation workflow  
--              - Reminder system
--              - Refund support
--              - Rating system
--              - Notification system

USE DatLichKham;

-- ==========================================
-- STEP 1: BACKUP OLD DATA (if exists)
-- ==========================================
CREATE TABLE IF NOT EXISTS LichDatKham_Backup_20251130 AS SELECT * FROM LichDatKham;

-- ==========================================
-- STEP 2: DROP OLD TABLES
-- ==========================================
DROP TABLE IF EXISTS LichDatKham;
DROP TABLE IF EXISTS ThongBao;

-- ==========================================
-- STEP 3: CREATE NEW DatLichKham TABLE
-- ==========================================
CREATE TABLE DatLichKham (
    DatLichID INT AUTO_INCREMENT PRIMARY KEY,
    
    -- ========== THÔNG TIN CƠ BẢN ==========
    BenhNhanID INT NOT NULL,
    BacSiID INT NOT NULL,
    CoSoID INT NOT NULL DEFAULT 1,
    NgayKham DATE NOT NULL,
    Ca ENUM('SANG','CHIEU','TOI') NOT NULL COMMENT 'Ca làm việc',
    GioKham TIME NOT NULL COMMENT 'Giờ cụ thể trong ca (VD: 08:30, 09:00)',
    LyDoKham VARCHAR(1000) NOT NULL,
    GhiChu VARCHAR(500),
    
    -- ========== TRẠNG THÁI ==========
    TrangThai ENUM(
        'CHO_XAC_NHAN_BAC_SI',   -- Chờ bác sĩ xác nhận
        'TU_CHOI',                -- Bác sĩ từ chối
        'CHO_THANH_TOAN',         -- Chờ thanh toán
        'DA_XAC_NHAN',            -- Đã xác nhận
        'DANG_KHAM',              -- Đang khám
        'HOAN_THANH',             -- Hoàn thành
        'HUY_BOI_BENH_NHAN',      -- Hủy bởi bệnh nhân
        'HUY_BOI_BAC_SI',         -- Hủy bởi bác sĩ
        'HUY_BOI_ADMIN',          -- Hủy bởi admin
        'KHONG_DEN',              -- Không đến
        'QUA_HAN'                 -- Quá hạn
    ) NOT NULL DEFAULT 'CHO_XAC_NHAN_BAC_SI',
    MaXacNhan VARCHAR(8) UNIQUE NOT NULL COMMENT 'Mã xác nhận booking (8 ký tự)',
    NgayDat DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- ========== THANH TOÁN ==========
    GiaKham DECIMAL(10,2) NOT NULL,
    PhuongThucThanhToan ENUM('TIEN_MAT','CHUYEN_KHOAN','VNPAY','MOMO','ZALO_PAY'),
    TrangThaiThanhToan ENUM('CHUA_THANH_TOAN','DANG_XU_LY','THANH_CONG','THAT_BAI','HOAN_TIEN') 
        NOT NULL DEFAULT 'CHUA_THANH_TOAN',
    MaGiaoDich VARCHAR(100) COMMENT 'Transaction ID từ VNPay/MoMo/ZaloPay',
    NgayThanhToan DATETIME,
    ThongTinThanhToan TEXT COMMENT 'JSON chi tiết thanh toán',
    
    -- ========== XÁC NHẬN BÁC SĨ ==========
    NgayBacSiXacNhan DATETIME COMMENT 'Thời gian bác sĩ xác nhận',
    LyDoTuChoi VARCHAR(500) COMMENT 'Lý do bác sĩ từ chối',
    
    -- ========== HỦY LỊCH ==========
    NgayHuy DATETIME,
    LyDoHuy VARCHAR(500),
    NguoiHuy INT COMMENT 'NguoiDungID người hủy',
    
    -- ========== KHÁM BỆNH ==========
    NgayCheckIn DATETIME COMMENT 'Thời gian check-in tại phòng khám',
    NgayKhamThucTe DATETIME COMMENT 'Thời gian bắt đầu khám',
    NgayHoanThanh DATETIME COMMENT 'Thời gian hoàn thành khám',
    KetQuaKham TEXT,
    DonThuoc TEXT,
    ChanDoan VARCHAR(500),
    LoiDanBacSi TEXT,
    
    -- ========== REMINDER ==========
    DaNhacNho BIT NOT NULL DEFAULT 0,
    NgayNhacNho DATETIME COMMENT 'Thời gian gửi email nhắc nhở',
    
    -- ========== HOÀN TIỀN ==========
    NgayHoanTien DATETIME,
    SoTienHoan DECIMAL(10,2),
    LyDoHoanTien VARCHAR(500),
    
    -- ========== ĐÁNH GIÁ SAU KHÁM ==========
    SoSao INT CHECK (SoSao BETWEEN 1 AND 5),
    NhanXet TEXT,
    NgayDanhGia DATETIME,
    
    -- ========== TÁI KHÁM ==========
    NgayTaiKham DATE COMMENT 'Ngày hẹn tái khám (bác sĩ chỉ định)',
    
    -- ========== AUDIT FIELDS ==========
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    UpdatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CreatedBy INT,
    UpdatedBy INT,
    IsDeleted BIT DEFAULT 0,
    DeletedAt DATETIME,
    DeletedBy INT,
    
    -- ========== FOREIGN KEYS ==========
    FOREIGN KEY (BenhNhanID) REFERENCES NguoiDung(NguoiDungID),
    FOREIGN KEY (BacSiID) REFERENCES BacSi(BacSiID),
    FOREIGN KEY (CoSoID) REFERENCES CoSoYTe(CoSoID),
    FOREIGN KEY (NguoiHuy) REFERENCES NguoiDung(NguoiDungID),
    
    -- ========== INDEXES ==========
    INDEX idx_benhnhan_trangthai (BenhNhanID, TrangThai),
    INDEX idx_bacsi_ngay (BacSiID, NgayKham, Ca),
    INDEX idx_ngaykham (NgayKham, Ca, TrangThai),
    INDEX idx_maxacnhan (MaXacNhan),
    INDEX idx_trangthai_ngay (TrangThai, NgayKham),
    INDEX idx_bacsi_trangthai (BacSiID, TrangThai),
    
    -- ========== UNIQUE CONSTRAINTS ==========
    UNIQUE KEY unique_booking_slot (BacSiID, NgayKham, Ca, GioKham, TrangThai)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng đặt lịch khám - Phase 2 với VNPay, Doctor Confirmation, Reminder, Rating';

-- ==========================================
-- STEP 4: CREATE NEW ThongBao TABLE
-- ==========================================
CREATE TABLE ThongBao (
    ThongBaoID INT AUTO_INCREMENT PRIMARY KEY,
    
    -- ========== NGƯỜI NHẬN ==========
    NguoiNhanID INT NOT NULL,
    
    -- ========== NỘI DUNG ==========
    LoaiThongBao ENUM(
        'DAT_LICH_MOI',
        'BAC_SI_XAC_NHAN',
        'BAC_SI_TU_CHOI',
        'HUY_LICH',
        'NHAC_LICH_KHAM',
        'LICH_KHAM_HON_THANH',
        'THANH_TOAN_THANH_CONG',
        'THANH_TOAN_THAT_BAI',
        'HOAN_TIEN',
        'NGAY_NGHI_MOI',
        'NGAY_NGHI_DUYET',
        'NGAY_NGHI_TU_CHOI',
        'HE_THONG',
        'KHAC'
    ) NOT NULL,
    TieuDe VARCHAR(200) NOT NULL,
    NoiDung TEXT NOT NULL,
    ThoiGian DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- ========== TRẠNG THÁI ==========
    DaDoc BIT NOT NULL DEFAULT 0,
    NgayDoc DATETIME,
    
    -- ========== LIÊN KẾT ==========
    DatLichID INT COMMENT 'Liên kết đến lịch khám (nếu có)',
    LinkDinhKem VARCHAR(500) COMMENT 'Link đến trang chi tiết',
    
    -- ========== EMAIL ==========
    DaGuiEmail BIT NOT NULL DEFAULT 0,
    NgayGuiEmail DATETIME,
    
    -- ========== METADATA ==========
    MetaData TEXT COMMENT 'JSON string chứa dữ liệu bổ sung',
    
    -- ========== AUDIT FIELDS ==========
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    UpdatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CreatedBy INT,
    UpdatedBy INT,
    IsDeleted BIT DEFAULT 0,
    DeletedAt DATETIME,
    DeletedBy INT,
    
    -- ========== FOREIGN KEYS ==========
    FOREIGN KEY (NguoiNhanID) REFERENCES NguoiDung(NguoiDungID),
    FOREIGN KEY (DatLichID) REFERENCES DatLichKham(DatLichID) ON DELETE SET NULL,
    
    -- ========== INDEXES ==========
    INDEX idx_nguoinhan_dadoc (NguoiNhanID, DaDoc),
    INDEX idx_nguoinhan_thoigian (NguoiNhanID, ThoiGian),
    INDEX idx_loai (LoaiThongBao),
    INDEX idx_datlich (DatLichID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Hệ thống thông báo với email tracking';

-- ==========================================
-- STEP 5: MIGRATE DATA (if needed)
-- ==========================================
-- Uncomment if you want to migrate old data from LichDatKham to DatLichKham
/*
INSERT INTO DatLichKham (
    BenhNhanID, BacSiID, CoSoID, NgayKham, Ca, GioKham, 
    LyDoKham, GhiChu, TrangThai, GiaKham, PhuongThucThanhToan,
    TrangThaiThanhToan, MaGiaoDich, NgayThanhToan, ChanDoan, 
    DonThuoc, SoSao, NhanXet, NgayDanhGia, NgayTaiKham,
    CreatedAt, UpdatedAt, CreatedBy, UpdatedBy
)
SELECT 
    BenhNhanID, BacSiID, 1 AS CoSoID,
    NgayKham,
    -- Map time to Ca (need to adjust based on your logic)
    CASE 
        WHEN HOUR(GioKham) < 12 THEN 'SANG'
        WHEN HOUR(GioKham) < 18 THEN 'CHIEU'
        ELSE 'TOI'
    END AS Ca,
    GioKham,
    COALESCE(LyDoKham, 'Khám bệnh') AS LyDoKham,
    GhiChu,
    -- Map old TrangThai to new
    CASE TrangThai
        WHEN 'ChoXacNhan' THEN 'CHO_XAC_NHAN_BAC_SI'
        WHEN 'DaXacNhan' THEN 'DA_XAC_NHAN'
        WHEN 'DangKham' THEN 'DANG_KHAM'
        WHEN 'HoanThanh' THEN 'HOAN_THANH'
        WHEN 'DaHuy' THEN 'HUY_BOI_BENH_NHAN'
        WHEN 'KhongDen' THEN 'KHONG_DEN'
        ELSE 'QUA_HAN'
    END AS TrangThai,
    GiaKham,
    -- Map PhuongThucThanhToan
    CASE PhuongThucThanhToan
        WHEN 'TienMat' THEN 'TIEN_MAT'
        WHEN 'ChuyenKhoan' THEN 'CHUYEN_KHOAN'
        WHEN 'VNPay' THEN 'VNPAY'
        WHEN 'Momo' THEN 'MOMO'
        WHEN 'ZaloPay' THEN 'ZALO_PAY'
        ELSE NULL
    END AS PhuongThucThanhToan,
    -- Map TrangThaiThanhToan
    CASE TrangThaiThanhToan
        WHEN 'ChuaThanhToan' THEN 'CHUA_THANH_TOAN'
        WHEN 'DangXuLy' THEN 'DANG_XU_LY'
        WHEN 'DaThanhToan' THEN 'THANH_CONG'
        WHEN 'HoanTien' THEN 'HOAN_TIEN'
        ELSE 'CHUA_THANH_TOAN'
    END AS TrangThaiThanhToan,
    MaGiaoDich,
    NgayThanhToan,
    ChanDoan,
    DonThuoc,
    SoSao,
    NhanXet,
    NgayDanhGia,
    NgayTaiKham,
    CreatedAt,
    UpdatedAt,
    CreatedBy,
    UpdatedBy
FROM LichDatKham_Backup_20251130;

-- Generate MaXacNhan for migrated records
UPDATE DatLichKham 
SET MaXacNhan = CONCAT(
    UPPER(SUBSTRING(MD5(CONCAT(DatLichID, BenhNhanID, BacSiID)), 1, 8))
)
WHERE MaXacNhan IS NULL OR MaXacNhan = '';
*/

-- ==========================================
-- STEP 6: VERIFY DATA
-- ==========================================
SELECT 'Migration completed successfully!' AS Status;
SELECT COUNT(*) AS TotalBookings FROM DatLichKham;
SELECT COUNT(*) AS TotalNotifications FROM ThongBao;

-- ==========================================
-- STEP 7: CLEANUP (Optional - run after verification)
-- ==========================================
-- DROP TABLE IF EXISTS LichDatKham_Backup_20251130;

