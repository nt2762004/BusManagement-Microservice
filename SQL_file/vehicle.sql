-- Tạo database (nếu cần)
CREATE DATABASE IF NOT EXISTS vehicle_service_db
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE vehicle_service_db;

-- Bảng lưu thông tin xe
CREATE TABLE vehicles (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    plate_number VARCHAR(20) NOT NULL UNIQUE,          -- Biển số xe
    type VARCHAR(50) NOT NULL,                        -- Loại xe (bus, car...)
    seat_count INT NOT NULL,                          -- Số ghế
    year YEAR NOT NULL,                               -- Năm sản xuất
    active TINYINT(1) DEFAULT 1,                      -- 1: hoạt động, 0: ngừng
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Bảng lưu trạng thái vận hành của xe
CREATE TABLE vehicle_status (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT UNSIGNED NOT NULL,              -- Khóa ngoại đến vehicles
    status ENUM('running','idle','maintenance') NOT NULL,  -- Đang chạy, nghỉ, bảo dưỡng
    note TEXT NULL,                                   -- Lý do/ghi chú thay đổi trạng thái
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,   -- Thời gian cập nhật trạng thái
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Chỉ mục để truy vấn nhanh trạng thái xe theo vehicle_id
CREATE INDEX idx_vehicle_status_vehicle_id ON vehicle_status(vehicle_id);

-- Thêm nhiều dữ liệu mẫu cho vehicles
INSERT INTO vehicles (plate_number, type, seat_count, year, active)
VALUES
('29B-11111','Bus',40,2018,1),
('29B-22222','Bus',50,2020,1),
('29B-33333','Bus',30,2017,0),
('30C-44444','Bus',45,2019,1),
('30C-55555','Bus',60,2015,1),
('51D-66666','Car',12,2022,1),
('51D-77777','Car',7,2021,1),
('51D-88888','Car',16,2020,0),
('79A-99999','MiniBus',25,2016,1),
('79A-00000','MiniBus',35,2019,1);

INSERT INTO vehicle_status (vehicle_id, status, note) VALUES 
(1, 'running', 'Hoạt động tuyến A'),
(2, 'running', 'Hoạt động tuyến B'),
(3, 'maintenance', 'Hỏng lốp chờ thay'), -- Xe này active=0
(4, 'running', 'Hoạt động bình thường'),
(5, 'running', 'Hoạt động bình thường'),
(6, 'idle', 'Đang chờ tài xế'),
(7, 'idle', 'Đang chờ lệnh'),
(8, 'maintenance', 'Bảo dưỡng định kỳ'), -- Xe này active=0
(9, 'running', 'Xe tăng cường'),
(10, 'running', 'Xe mới nhập');