# 1. Thay đổi image base
FROM eclipse-temurin:17-jdk-alpine

# 2. Set thư mục làm việc
WORKDIR /app

# 3. Copy file jar (Dùng *.jar cho tiện, đỡ phải nhớ tên version)
COPY target/*.jar app.jar

# 4. Mở cổng
EXPOSE 8080

# 5. Chạy
ENTRYPOINT ["java", "-jar", "app.jar"]