-- ==============================
-- DRIVER SERVICE DATABASE (MySQL 8, utf8mb4)
-- Domain: Drivers + Work Schedules (no trips table here)
-- ==============================

DROP DATABASE IF EXISTS driver_service_db;
CREATE DATABASE IF NOT EXISTS driver_service_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE driver_service_db;

-- 1) drivers – thông tin tài xế
CREATE TABLE drivers (
  id             INT AUTO_INCREMENT PRIMARY KEY,
  full_name      VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  license_type   ENUM('D','D1','D2') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  license_expiry DATE NOT NULL,
  phone          VARCHAR(20) NOT NULL UNIQUE,
  auth_user_id   BIGINT UNIQUE NULL,
  status         ENUM('available','inactive') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT 'available',
  created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2) work_schedules – lịch/ca làm việc theo DATETIME (hỗ trợ ca qua đêm)
CREATE TABLE work_schedules (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  driver_id   INT NOT NULL,
  shift_start DATETIME NOT NULL,
  shift_end   DATETIME NOT NULL,
  status      ENUM('PLANNED','CONFIRMED','CANCELLED') DEFAULT 'PLANNED',
  note        TEXT NULL,
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_ws_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE,
  CONSTRAINT chk_ws_time CHECK (shift_end > shift_start),
  INDEX idx_ws_driver_start (driver_id, shift_start),
  INDEX idx_ws_driver_end (driver_id, shift_end)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE driver_assignments (
  id INT AUTO_INCREMENT PRIMARY KEY,
  driver_id INT NOT NULL,
  trip_id INT NOT NULL,
  assigned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_da_driver FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE,
  UNIQUE KEY uq_driver_trip (driver_id, trip_id),
  INDEX idx_da_driver (driver_id),
  INDEX idx_da_trip (trip_id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Seed data
INSERT INTO drivers (full_name, license_type, license_expiry, phone, status) VALUES
 ('Nguyễn Văn A', 'D1', '2026-05-10', '0905123456', 'available'),
 ('Trần Thị B', 'D2',  '2025-12-31', '0912123456', 'inactive'),
 ('Lê Văn C',   'D1', '2027-03-15', '0934567890', 'available'),
 ('Phạm Minh D','D',  '2026-10-20', '0945123789', 'inactive'),
 ('Võ Thị E',   'D2', '2027-01-05', '0987123456', 'available');

-- Ví dụ seed ca làm việc (PLANNED)
INSERT INTO work_schedules (driver_id, shift_start, shift_end, status) VALUES
  (1, '2025-10-14 08:00:00', '2025-10-14 12:00:00', 'PLANNED'),
  (1, '2025-10-14 13:00:00', '2025-10-14 17:00:00', 'PLANNED'),
  (2, '2025-10-14 07:00:00', '2025-10-14 15:00:00', 'PLANNED');

-- Liên kết tài xế với user bên user_and_auth_service theo số điện thoại (tùy chọn)
-- Nếu muốn backfill tự động, bỏ comment 3 dòng dưới đây sau khi user_and_auth_service_db đã có dữ liệu:
UPDATE driver_service_db.drivers d
JOIN user_and_auth_service_db.users u ON u.phone = d.phone
SET d.auth_user_id = u.id
WHERE d.auth_user_id IS NULL;

-- Migration helper (run once on existing DB if it still has 'on_trip'):
-- UPDATE drivers SET status='inactive' WHERE status='on_trip';

