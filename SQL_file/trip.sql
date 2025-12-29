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
  id                INT AUTO_INCREMENT PRIMARY KEY,
  trip_id           INT NOT NULL,
  user_id           INT NOT NULL,
  notification_type VARCHAR(50) NOT NULL, -- TRIP_CREATED, TRIP_UPDATED, TRIP_CANCELLED, DRIVER_ASSIGNED, STATUS_CHANGED
  title             VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  message           TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  is_read           BOOLEAN DEFAULT FALSE,
  created_at        DATETIME DEFAULT CURRENT_TIMESTAMP,
  read_at           DATETIME NULL,
  INDEX idx_notif_trip (trip_id),
  INDEX idx_notif_user_read (user_id, is_read),
  INDEX idx_notif_created (created_at DESC),
  CONSTRAINT fk_notif_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Optional: lịch sử phân công tài xế cho chuyến (không FK ra ngoài service)
CREATE TABLE trip_assignment_history (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  trip_id            INT NOT NULL,
  previous_driver_id INT NULL,
  new_driver_id      INT NULL,
  changed_by         VARCHAR(100) NULL,
  changed_at         DATETIME DEFAULT CURRENT_TIMESTAMP,
  note               TEXT NULL,
  INDEX idx_hist_trip (trip_id),
  INDEX idx_hist_new_driver (new_driver_id),
  INDEX idx_hist_prev_driver (previous_driver_id)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Seed data
INSERT INTO trips (route_id, vehicle_id, driver_id, planned_start, planned_end, actual_start, actual_end, status, note) VALUES
  (1, 1, 1, '2025-10-19 07:00:00', '2025-10-19 10:00:00', NULL, NULL, 'NOT_STARTED', 'Chuyến sáng Hà Nội - Hải Phòng'),
  (2, 2, 2, '2025-10-18 08:00:00', '2025-10-18 12:00:00', '2025-10-18 08:05:00', NULL, 'IN_PROGRESS', 'Đang trên đường đến Hải Dương'),
  (3, 3, 3, '2025-10-17 09:00:00', '2025-10-17 11:00:00', '2025-10-17 09:10:00', '2025-10-17 11:05:00', 'COMPLETED', 'Hoàn thành đúng giờ');

INSERT INTO trip_notifications (trip_id, user_id, notification_type, title, message, is_read, read_at) VALUES
  (1, 1, 'TRIP_CREATED', 'Chuyến mới được tạo', 'Chuyến #1 đã được tạo, khởi hành dự kiến lúc 07:00 ngày 19/10/2025.', FALSE, NULL),
  (2, 1, 'STATUS_CHANGED', 'Chuyến đang chạy', 'Chuyến #2 đã bắt đầu lúc 08:05 ngày 18/10/2025.', FALSE, NULL),
  (3, 1, 'TRIP_COMPLETED', 'Chuyến hoàn thành', 'Chuyến #3 đã hoàn thành lúc 11:05 ngày 17/10/2025.', TRUE, '2025-10-17 11:10:00');

-- Gợi ý truy vấn
-- 1) Chuyến theo trạng thái
-- SELECT * FROM trips WHERE status = 'NOT_STARTED';
-- 2) Lịch sử phân công theo trip
-- SELECT * FROM trip_assignment_history WHERE trip_id = ? ORDER BY changed_at DESC;
