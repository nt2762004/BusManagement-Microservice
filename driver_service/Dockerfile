# 1. Thay đổi image base
FROM eclipse-temurin:17-jdk-alpine

# 2. Thiết lập thư mục
WORKDIR /app

# 3. Copy file .jar (Dùng dấu * cho tiện)
COPY target/*.jar app.jar

# 4. Mở cổng 8084
EXPOSE 8084

# 5. Chạy app
ENTRYPOINT ["java", "-jar", "app.jar"]