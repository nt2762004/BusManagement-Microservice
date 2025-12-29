-- =====================================================
-- HỆ THỐNG QUẢN LÝ XE BUÝT - DATABASE SCRIPT (Auth Service)
-- Phiên bản: 2.1 (Thay KỸ THUẬT bằng TÀI XẾ, seed tài xế)
-- Ngày: 11/11/2025
-- =====================================================

-- Tạo database
DROP DATABASE IF EXISTS user_and_auth_service_db;
CREATE DATABASE user_and_auth_service_db
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

USE user_and_auth_service_db;

-- =====================================================
-- BẢNG ROLES (VAI TRÒ NGƯỜI DÙNG)
-- =====================================================
CREATE TABLE roles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE COMMENT 'Tên vai trò: ADMIN, CHỦ NHÀ XE, ĐIỀU HÀNH, TÀI XẾ',
  description VARCHAR(200) COMMENT 'Mô tả vai trò',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_role_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng vai trò người dùng trong hệ thống quản lý xe buýt';

-- =====================================================
-- BẢNG USERS (NGƯỜI DÙNG)
-- =====================================================
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE COMMENT 'Tên đăng nhập duy nhất',
  password VARCHAR(100) NOT NULL COMMENT 'Mật khẩu (plain text trong demo)',
  email VARCHAR(100) UNIQUE COMMENT 'Email người dùng',
  full_name VARCHAR(100) NOT NULL COMMENT 'Họ và tên đầy đủ',
  phone VARCHAR(20) UNIQUE COMMENT 'Số điện thoại',
  active BOOLEAN DEFAULT TRUE COMMENT 'Trạng thái hoạt động',
  role_id BIGINT NOT NULL COMMENT 'ID vai trò (khóa ngoại)',
  last_login TIMESTAMP NULL COMMENT 'Lần đăng nhập cuối cùng',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id) 
    ON DELETE RESTRICT ON UPDATE CASCADE,
  INDEX idx_username (username),
  INDEX idx_email (email),
  INDEX idx_role_id (role_id),
  INDEX idx_active (active),
  INDEX idx_full_name (full_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng người dùng hệ thống quản lý xe buýt';

-- =====================================================
-- CHÈN DỮ LIỆU MẪU
-- =====================================================

-- Vai trò
INSERT INTO roles (name, description) VALUES 
('ADMIN', 'Quản trị hệ thống - có toàn quyền truy cập'),
('CHỦ NHÀ XE', 'Chủ nhà xe - quản lý xe buýt và tuyến đường'),
('ĐIỀU HÀNH', 'Điều hành - lập lịch và quản lý chuyến xe'),
('TÀI XẾ', 'Tài xế - xem lịch làm việc, chuyến đi');

-- Người dùng mẫu (mật khẩu plain text cho demo)
-- Admin
INSERT INTO users (username, password, email, full_name, phone, role_id) VALUES
('admin', 'admin123', 'admin@busmanagement.vn', 'Quản Trị Viên Hệ Thống', '0901234567', (SELECT id FROM roles WHERE name='ADMIN'));

-- Chủ nhà xe
INSERT INTO users (username, password, email, full_name, phone, role_id) VALUES 
('chunhaxe1', 'chunhaxe123', 'chunhaxe1@buscompany.vn', 'Nguyễn Văn An', '0912345678', (SELECT id FROM roles WHERE name='CHỦ NHÀ XE')),
('chunhaxe2', 'chunhaxe123', 'chunhaxe2@buscompany.vn', 'Trần Thị Bình', '0912345679', (SELECT id FROM roles WHERE name='CHỦ NHÀ XE'));

-- Điều hành
INSERT INTO users (username, password, email, full_name, phone, role_id) VALUES 
('dieuhanh1', 'dieuhanh123', 'dieuhanh1@buscompany.vn', 'Lê Văn Cường', '0923456789', (SELECT id FROM roles WHERE name='ĐIỀU HÀNH')),
('dieuhanh2', 'dieuhanh123', 'dieuhanh2@buscompany.vn', 'Phạm Thị Dung', '0923456790', (SELECT id FROM roles WHERE name='ĐIỀU HÀNH')),
('dieuhanh3', 'dieuhanh123', 'dieuhanh3@buscompany.vn', 'Hoàng Minh Đức', '0923456791', (SELECT id FROM roles WHERE name='ĐIỀU HÀNH'));

-- Tài xế (thay cho KỸ THUẬT) - tạo tài khoản khớp phone với driver_service seed
INSERT INTO users (username, password, email, full_name, phone, role_id) VALUES 
('taixe1', 'taixe123', 'taixe1@buscompany.vn', 'Nguyễn Văn A', '0905123456', (SELECT id FROM roles WHERE name='TÀI XẾ')),
('taixe2', 'taixe123', 'taixe2@buscompany.vn', 'Trần Thị B', '0912123456', (SELECT id FROM roles WHERE name='TÀI XẾ')),
('taixe3', 'taixe123', 'taixe3@buscompany.vn', 'Lê Văn C',   '0934567890', (SELECT id FROM roles WHERE name='TÀI XẾ')),
('taixe4', 'taixe123', 'taixe4@buscompany.vn', 'Phạm Minh D', '0945123789', (SELECT id FROM roles WHERE name='TÀI XẾ')),
('taixe5', 'taixe123', 'taixe5@buscompany.vn', 'Võ Thị E',   '0987123456', (SELECT id FROM roles WHERE name='TÀI XẾ'));

-- ==============================
-- KIỂM TRA DỮ LIỆU
-- ==============================
SELECT 'DANH SÁCH VAI TRÒ:' as title; SELECT id, name, description, created_at FROM roles ORDER BY id;
SELECT 'DANH SÁCH NGƯỜI DÙNG:' as title; 
SELECT u.id,u.username,u.password,u.full_name,u.email,u.phone,r.name as role_name,u.active,u.created_at
FROM users u JOIN roles r ON u.role_id=r.id ORDER BY u.id;
SELECT 'THỐNG KÊ THEO VAI TRÒ:' as title;
SELECT r.name as role_name, r.description, COUNT(u.id) as user_count,
       COUNT(CASE WHEN u.active = TRUE THEN 1 END) as active_users
FROM roles r LEFT JOIN users u ON r.id = u.role_id
GROUP BY r.id, r.name, r.description ORDER BY r.id;
