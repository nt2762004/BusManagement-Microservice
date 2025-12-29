-- DB và schema
CREATE DATABASE IF NOT EXISTS route_service_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE route_service_db;

-- Bảng locations
CREATE TABLE IF NOT EXISTS locations (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  latitude DECIMAL(10,7) NULL,
  longitude DECIMAL(10,7) NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Bảng routes (không dùng CHECK để tránh lỗi version)
CREATE TABLE IF NOT EXISTS routes (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  origin_id BIGINT UNSIGNED NOT NULL,
  destination_id BIGINT UNSIGNED NOT NULL,
  eta_minutes INT UNSIGNED NOT NULL,
  distance_km DECIMAL(6,2) NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_route_origin FOREIGN KEY (origin_id) REFERENCES locations(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_route_destination FOREIGN KEY (destination_id) REFERENCES locations(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT uq_route UNIQUE (origin_id, destination_id)
) ENGINE=InnoDB;

-- (Tùy chọn) Bảng route_logs — nếu MySQL < 5.7, bỏ JSON hoặc đổi thành TEXT
-- Nếu không chắc version, BỎ QUA phần này để tránh fail làm dừng cả script.
CREATE TABLE route_logs (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  route_id BIGINT UNSIGNED,
  action_type ENUM('CREATE','UPDATE','DELETE') NOT NULL,
  old_data JSON NULL,
  new_data JSON NULL,
  changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_route_logs_route FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Seed địa điểm (idempotent)
INSERT IGNORE INTO locations (name, latitude, longitude) VALUES
('Hà Nội', 21.0285, 105.8542),
('Hải Phòng', 20.8449, 106.6881),
('Đà Nẵng', 16.0471, 108.2068),
('Huế', 16.4637, 107.5909),
('TP. Hồ Chí Minh', 10.7769, 106.7009),
('Cần Thơ', 10.0452, 105.7469),
('Nha Trang', 12.2388, 109.1967),
('Đà Lạt', 11.9404, 108.4583),
('Vinh', 18.6796, 105.6813),
('Quy Nhơn', 13.7820, 109.2196);

-- Biến ID cho routes
SET @hn  := (SELECT id FROM locations WHERE name='Hà Nội');
SET @hp  := (SELECT id FROM locations WHERE name='Hải Phòng');
SET @dn  := (SELECT id FROM locations WHERE name='Đà Nẵng');
SET @hue := (SELECT id FROM locations WHERE name='Huế');
SET @hcm := (SELECT id FROM locations WHERE name='TP. Hồ Chí Minh');
SET @ct  := (SELECT id FROM locations WHERE name='Cần Thơ');
SET @nt  := (SELECT id FROM locations WHERE name='Nha Trang');
SET @dl  := (SELECT id FROM locations WHERE name='Đà Lạt');
SET @vinh:= (SELECT id FROM locations WHERE name='Vinh');
SET @qn  := (SELECT id FROM locations WHERE name='Quy Nhơn');

-- Seed tuyến (INSERT IGNORE để không vấp unique nếu chạy lại)
INSERT IGNORE INTO routes (origin_id, destination_id, eta_minutes, distance_km) VALUES
(@hn , @hp  , 120, 105.0),
(@hn , @vinh, 300, 300.0),
(@hp , @vinh, 360, 340.0),
(@dn , @hue , 150,  95.0),
(@hue, @vinh, 420, 380.0),
(@dn , @qn  , 420, 325.0),
(@dn , @nt  , 660, 525.0),
(@hcm, @ct  , 210, 165.0),
(@hcm, @nt  , 600, 430.0),
(@hcm, @dl  , 480, 310.0),
(@ct , @dl  , 540, 400.0),
(@nt , @dl  , 210, 135.0);