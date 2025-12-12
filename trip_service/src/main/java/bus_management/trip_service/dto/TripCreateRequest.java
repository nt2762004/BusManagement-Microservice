package bus_management.trip_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TripCreateRequest {
    @NotNull
    private Long routeId;
    @NotNull
    private Long vehicleId;
    @NotNull
    private Long driverId;
    @NotBlank
    private String plannedStart; // ISO string from frontend
    @NotBlank
    private String plannedEnd;   // ISO string from frontend
    private String note;
    private Long userId;
}
