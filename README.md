# Bus Management System

The bus management project is built on Microservices architecture. It helps manage all bus operations like vehicles, drivers, routes, ticket booking, and notifications.

**Note: The project is deployed on AWS with a Free Tier account, so performance may be unstable.**

Deployed Link: **AWS Free Tier expired**

## 🏗 System Architecture

The system is divided into independent services (Microservices). They communicate with each other through an API Gateway.

| Service Name | Port | Function Description |
| :--- | :--- | :--- |
| **API Gateway** | `8080` | The only entry point of the system. Routes requests from client to the correct service. Handles CORS and basic security. |
| **Frontend Service** | `8000` | User Interface (Web Application) for Admin and Customers. Uses Thymeleaf to render interface on the server side. |
| **User & Auth Service** | `8081` | Manages user accounts, registration, login, authentication, and authorization. |
| **Vehicle Service** | `8082` | Manages bus information (license plate, type, capacity, maintenance status...). |
| **Route Service** | `8083` | Manages list of routes, stops, and travel paths. |
| **Driver Service** | `8084` | Manages driver profiles, licenses, and work history. |
| **Trip Service** | `8085` | Manages specific trips based on Routes and Vehicles. Handles scheduling and notifications. |

## 🛠 Technologies Used

*   **Language:** Java 17+
*   **Framework:** Spring Boot (Spring Web, Spring Data JPA, Spring Cloud Gateway).
*   **Frontend:** Thymeleaf, HTML5, CSS3, JavaScript.
*   **Database:** MySQL (Each important service has its own database to ensure independence).
*   **Build Tool:** Maven (Multi-module project).
*   **Containerization:** Docker.
*   **Deployment:** AWS EC2 (Ubuntu).

## 🚀 Installation and Run Guide (Local Development)

### 1. Environment Requirements
*   Java Development Kit (JDK) 17 or higher.
*   Maven.
*   Docker & Docker Compose (Recommended).
*   MySQL Server (If running directly without Docker).

### 2. Database Configuration
The following services need a database connection: `driver_service`, `trip_service`, `user_and_auth_service`.
*   Go to `db/mysql/` folder in each service to get the `.sql` script.
*   Create the corresponding database in MySQL and run the script to create tables.
*   Update connection info (URL, Username, Password) in `application.properties` of each service if needed.

### 3. Build Project
At the root folder (`bus_management_system`), run the following command to build all modules:
```bash
./mvnw clean install -DskipTests
```

### 4. Start System
You need to run services in the following priority order to avoid connection errors:

1.  **Database:** Ensure MySQL is started.
2.  **Core Services:** `user_and_auth_service`, `vehicle_service`, `route_service`, `driver_service`, `trip_service`.
3.  **API Gateway:** `api_gateway_service` (Wait for child services to start).
4.  **Frontend:** `frontend_service`.

How to run each service (open a separate terminal for each):
```bash
cd <service_folder_name>
../mvnw spring-boot:run
```

After running successfully, access the Web App at: `http://localhost:8000`

## 🐳 Deployment Process (Docker & AWS)

The project is designed to deploy on AWS EC2 using Docker.

### Step 1: Package and Push Image to Docker Hub (On Local Machine)
```bash
# 1. Build the whole project to .jar file
./mvnw clean install -DskipTests

# 2. Build Docker Image for each service (Example: frontend)
# Note: Replace 'tuannguyen276' with your Docker Hub username
docker build -t tuannguyen276/bus-frontend:v1 ./frontend_service

# 3. Push image to Docker Hub
docker push tuannguyen276/bus-frontend:v1
```
*(Repeat step 2 and 3 for other services if there are code changes)*

### Step 2: Update on Server (SSH into AWS)
```bash
# 1. SSH into server
ssh -i "awskey.pem" ubuntu@<IP_SERVER>

# 2. Check running containers
sudo docker ps

# 3. Update service (Example: frontend)
sudo docker rm -f frontend-service      # Remove old container
sudo docker rmi tuannguyen276/bus-frontend:v1  # Remove old image
sudo docker run -d --name frontend-service -p 8000:8000 tuannguyen276/bus-frontend:v1 # Run new container
```

## 🔗 Routing Configuration (API Gateway)

All API requests are routed through Gateway (`port 8080`):

*   `http://localhost:8080/api/auth/**` ➡️ **User Service**
*   `http://localhost:8080/api/vehicles/**` ➡️ **Vehicle Service**
*   `http://localhost:8080/api/routes/**` ➡️ **Route Service**
*   `http://localhost:8080/api/drivers/**` ➡️ **Driver Service**
*   `http://localhost:8080/api/trips/**` ➡️ **Trip Service**

---

# Hệ thống quản lý nhà xe

Dự án quản lý hệ thống xe buýt được xây dựng dựa trên kiến trúc Microservices, giúp quản lý toàn diện các hoạt động vận hành xe buýt từ quản lý xe, tài xế, lộ trình đến đặt vé và thông báo.

**Note: Dự án được deploy trên AWS với tài khoản FreeTier nên dự án sẽ có hiệu suất không ổn định.**

Link deployed: http://13.214.207.121/
- Tài khoản Chủ nhà xe: TK: chunhaxe1, MK: 123 | TK: chunhaxe2, MK: 123.
- Tài khoản Điều hành: TK: dieuhanh1, MK: 123 | TK: dieuhanh2, MK: 123 | TK: dieuhanh3, MK: 123.
- Tài khoản Tài xế: TK: taixe1, MK: 123 | TK: taixe2, MK: 123 | TK: taixe3, MK: 123.

## 🏗 Kiến trúc hệ thống

Hệ thống được chia nhỏ thành các dịch vụ độc lập (Microservices), giao tiếp với nhau thông qua API Gateway.

| Service Name | Port | Mô tả Chức năng |
| :--- | :--- | :--- |
| **API Gateway** | `8080` | Cổng giao tiếp duy nhất của hệ thống. Điều hướng (route) các request từ client đến đúng service xử lý. Xử lý CORS và bảo mật cơ bản. |
| **Frontend Service** | `8000` | Giao diện người dùng (Web Application) dành cho Admin và Khách hàng. Sử dụng Thymeleaf để render giao diện phía server. |
| **User & Auth Service** | `8081` | Quản lý tài khoản người dùng, đăng ký, đăng nhập, xác thực (Authentication) và phân quyền (Authorization). |
| **Vehicle Service** | `8082` | Quản lý thông tin các xe buýt (biển số, loại xe, sức chứa, trạng thái bảo trì...). |
| **Route Service** | `8083` | Quản lý danh sách các tuyến đường, điểm dừng và lộ trình di chuyển. |
| **Driver Service** | `8084` | Quản lý hồ sơ tài xế, bằng lái và lịch sử làm việc. |
| **Trip Service** | `8085` | Quản lý các chuyến đi cụ thể (Trip) dựa trên Lộ trình và Xe. Xử lý việc xếp lịch và thông báo. |

## 🛠 Công nghệ sử dụng

*   **Ngôn ngữ:** Java 17+
*   **Framework:** Spring Boot (Spring Web, Spring Data JPA, Spring Cloud Gateway).
*   **Frontend:** Thymeleaf, HTML5, CSS3, JavaScript.
*   **Database:** MySQL (Mỗi service quan trọng đều có database riêng biệt để đảm bảo tính độc lập).
*   **Build Tool:** Maven (Dự án dạng Multi-module).
*   **Containerization:** Docker.
*   **Deployment:** AWS EC2 (Ubuntu).

## 🚀 Hướng dẫn cài đặt và chạy (Local Development)

### 1. Yêu cầu môi trường
*   Java Development Kit (JDK) 17 trở lên.
*   Maven.
*   Docker & Docker Compose (Khuyên dùng).
*   MySQL Server (Nếu chạy trực tiếp không qua Docker).

### 2. Cấu hình Database
Các service sau cần kết nối Database: `driver_service`, `trip_service`, `user_and_auth_service`.
*   Truy cập thư mục `db/mysql/` trong từng service để lấy file script `.sql`.
*   Tạo database tương ứng trong MySQL và chạy script để khởi tạo bảng dữ liệu.
*   Cập nhật thông tin kết nối (URL, Username, Password) trong file `application.properties` của từng service nếu cần.

### 3. Build dự án
Tại thư mục gốc (`bus_management_system`), chạy lệnh sau để build toàn bộ các module:
```bash
./mvnw clean install -DskipTests
```

### 4. Khởi chạy hệ thống
Bạn cần chạy các service theo thứ tự ưu tiên sau để tránh lỗi kết nối:

1.  **Database:** Đảm bảo MySQL đã start.
2.  **Core Services:** `user_and_auth_service`, `vehicle_service`, `route_service`, `driver_service`, `trip_service`.
3.  **API Gateway:** `api_gateway_service` (Chờ các service con start xong).
4.  **Frontend:** `frontend_service`.

Cách chạy từng service (mở terminal riêng cho mỗi service):
```bash
cd <tên_thư_mục_service>
../mvnw spring-boot:run
```

Sau khi chạy thành công, truy cập Web App tại: `http://localhost:8000`

## 🐳 Quy trình Deployment (Docker & AWS)

Dự án được thiết kế để deploy lên AWS EC2 sử dụng Docker.

### Bước 1: Đóng gói và đẩy Image lên Docker Hub (Tại máy Local)
```bash
# 1. Build toàn bộ project ra file .jar
./mvnw clean install -DskipTests

# 2. Build Docker Image cho từng service (Ví dụ: frontend)
# Lưu ý: Thay 'tuannguyen276' bằng Docker Hub username của bạn
docker build -t tuannguyen276/bus-frontend:v1 ./frontend_service

# 3. Push image lên Docker Hub
docker push tuannguyen276/bus-frontend:v1
```
*(Lặp lại bước 2 và 3 cho các service khác nếu có thay đổi code)*

### Bước 2: Cập nhật trên Server (SSH vào AWS)
```bash
# 1. SSH vào server
ssh -i "awskey.pem" ubuntu@<IP_SERVER>

# 2. Kiểm tra container đang chạy
sudo docker ps

# 3. Cập nhật service (Ví dụ: frontend)
sudo docker rm -f frontend-service      # Xóa container cũ
sudo docker rmi tuannguyen276/bus-frontend:v1  # Xóa image cũ
sudo docker run -d --name frontend-service -p 8000:8000 tuannguyen276/bus-frontend:v1 # Chạy container mới
```

## 🔗 Cấu hình Routing (API Gateway)

Tất cả các request API đều được định tuyến qua Gateway (`port 8080`):

*   `http://localhost:8080/api/auth/**` ➡️ **User Service**
*   `http://localhost:8080/api/vehicles/**` ➡️ **Vehicle Service**
*   `http://localhost:8080/api/routes/**` ➡️ **Route Service**
*   `http://localhost:8080/api/drivers/**` ➡️ **Driver Service**
*   `http://localhost:8080/api/trips/**` ➡️ **Trip Service**
