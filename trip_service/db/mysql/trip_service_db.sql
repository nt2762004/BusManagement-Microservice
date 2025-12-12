-- ==============================
-- TRIP SERVICE DATABASE (MySQL 8, utf8mb4)
-- Domain: Trips + Trip Notifications (+ optional assignment history)
-- ==============================

DROP DATABASE IF EXISTS trip_service_db;
CREATE DATABASE IF NOT EXISTS trip_service_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE trip_service_db;

-- 1) trips – thông tin chuyến (độc lập, không FK sang service khác)
CREATE TABLE trips (
  id             INT AUTO_INCREMENT PRIMARY KEY,
  route_id       INT NOT NULL,
  vehicle_id     INT NOT NULL,
  driver_id      INT NULL, -- cho phép NULL khi chưa phân công
  planned_start  DATETIME NOT NULL,
  planned_end    DATETIME NOT NULL,
  actual_start   DATETIME NULL,
  actual_end     DATETIME NULL,
  status         ENUM('NOT_STARTED','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'NOT_STARTED',
  note           TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT chk_planned_time CHECK (planned_end > planned_start),
  INDEX idx_trips_status (status),
  INDEX idx_trips_route (route_id),
  INDEX idx_trips_vehicle (vehicle_id),
  INDEX idx_trips_driver (driver_id),
  INDEX idx_trips_planned_start (planned_start)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2) trip_notifications – thông báo theo chuyến (FK nội bộ tới trips)
CREATE TABLE trip_notifications (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  trip_id    INT NOT NULL,
  user_id    INT NULL,  -- Nullable: cho phép thông báo hệ thống không gắn với user cụ thể
  message    TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  status     ENUM('unread','read') DEFAULT 'unread',
  INDEX idx_notif_trip (trip_id),
  INDEX idx_notif_user (user_id),
  CONSTRAINT fk_notif_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


-- Seed data
INSERT INTO trips (route_id, vehicle_id, driver_id, planned_start, planned_end, actual_start, actual_end, status, note) VALUES
  (1, 1, 1, '2025-10-19 07:00:00', '2025-10-19 10:00:00', NULL, NULL, 'NOT_STARTED', 'Chuyến sáng Hà Nội - Hải Phòng'),
  (2, 2, 2, '2025-10-18 08:00:00', '2025-10-18 12:00:00', '2025-10-18 08:05:00', NULL, 'IN_PROGRESS', 'Đang trên đường đến Hải Dương'),
  (3, 3, 3, '2025-10-17 09:00:00', '2025-10-17 11:00:00', '2025-10-17 09:10:00', '2025-10-17 11:05:00', 'COMPLETED', 'Hoàn thành đúng giờ');


-- Gợi ý truy vấn
-- 1) Chuyến theo trạng thái
-- SELECT * FROM trips WHERE status = 'NOT_STARTED';
-- 2) Lịch sử phân công theo trip
-- SELECT * FROM trip_assignment_history WHERE trip_id = ? ORDER BY changed_at DESC;
