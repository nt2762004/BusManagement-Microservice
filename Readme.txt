## .\mvnw clean install

1. KẾT NỐI VÀO SERVER AWS (Bước đầu tiên)
Mở Terminal (VS Code hoặc CMD) tại thư mục chứa file khóa .pem.

# Thay đường dẫn file key nếu cần
ssh -i "awskey.pem" ubuntu@13.214.207.121


2. KIỂM TRA SỨC KHỎE HỆ THỐNG (Monitor)
Sau khi vào được SSH, dùng các lệnh này để xem server còn sống không.

# 1. Xem danh sách container đang chạy (Quan trọng nhất)
# Kiểm tra cột STATUS xem có cái nào bị "Restarting" không
sudo docker ps

# 2. Xem mức độ tiêu tốn RAM/CPU (Để biết máy có bị quá tải không)
sudo docker stats

# 3. Xem log thời gian thực của một service (Để bắt lỗi)
# Thay tên service: api-gateway, auth-service, frontend-service...
sudo docker logs -f --tail 50 api-gateway


3. QUY TRÌNH CẬP NHẬT CODE (Code -> Build -> Deploy)
Đây là quy trình bạn làm nhiều nhất. Nếu mai bạn sửa HTML hay Java, hãy làm theo thứ tự này.

Giai đoạn A: Tại máy tính của bạn (Local)
Mở Terminal tại thư mục service bạn vừa sửa (ví dụ frontend_service).

# B1: Đóng gói file .jar
.\mvnw clean install -DskipTests

# B2: Đóng gói Docker Image (Ghi đè bản cũ)
# Thay tên service tương ứng: bus-frontend, bus-auth, bus-gateway...
docker build -t tuannguyen276/bus-frontend:v1 .

# B3: Đẩy lên mạng
docker push tuannguyen276/bus-frontend:v1

Giai đoạn B: Tại Server AWS (SSH)
Sau khi đẩy xong, vào server gõ lệnh để cập nhật.

# B1: Xóa container cũ đang chạy
# Thay tên service tương ứng: frontend-service, auth-service...
sudo docker rm -f frontend-service

# B2: Xóa image cũ để bắt buộc tải bản mới
sudo docker rmi tuannguyen276/bus-frontend:v1

# B3: Khởi chạy lại (Nó sẽ tự tải bản mới và chạy)
sudo docker-compose up -d


4. CÁC LỆNH "CỨU THƯƠNG" KHẨN CẤP
Dùng khi hệ thống gặp lỗi lạ hoặc bị treo.

# Khởi động lại toàn bộ hệ thống (Soft restart)
sudo docker-compose restart

# Cập nhật file docker-compose.yml (Nếu bạn sửa file cấu hình)
# Lệnh này sẽ tự phát hiện thay đổi và recreate container cần thiết
sudo docker-compose up -d

# "Hủy diệt" và làm lại từ đầu (Khi lỗi quá nặng)
# 1. Xóa sạch sành sanh container
sudo docker rm -f $(sudo docker ps -aq)
# 2. Chạy lại từ đầu
sudo docker-compose up -d