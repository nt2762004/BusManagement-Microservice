package bus_management.driver_service.web.dto;

import bus_management.driver_service.domain.Driver;
import jakarta.validation.constraints.NotNull;

public class StatusUpdateRequest {
    @NotNull
    private Driver.DriverStatus status;

    public Driver.DriverStatus getStatus() { return status; }
    public void setStatus(Driver.DriverStatus status) { this.status = status; }
}
