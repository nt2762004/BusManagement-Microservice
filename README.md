# Bus Management System

The bus management project is built on Microservices architecture. It helps manage all bus operations like vehicles, drivers, routes, ticket booking, and notifications.

**Note: The project is deployed on AWS with a Free Tier account, so performance may be unstable.**

Deployed Link: **Inactive** (AWS Free Tier expired). For details, please refer to the **report.pdf** in GitHub repository.

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
