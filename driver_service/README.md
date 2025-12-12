# Driver Service

## Run with MySQL (default)
The service uses MySQL by default (see `src/main/resources/application.properties`).

Run:
```powershell
.\mvnw spring-boot:run
```

On startup it will log the driver count or print a clear `[STARTUP ERROR]` with the DB error in the terminal and exit with code 1.

Service port: 8084

Endpoints:
- `GET /api/drivers`
- `GET /api/drivers/available`
- `POST /api/drivers`
