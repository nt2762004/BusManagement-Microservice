package bus_management.trip_service.dto;

import bus_management.trip_service.entity.TripStatus;
import lombok.Data;

@Data
public class TripUpdateRequest {
    private Long routeId;
    private Long vehicleId;
    private Long driverId;
    private String plannedStart;
    private String plannedEnd;
    private String actualStart;
    private String actualEnd;
    private TripStatus status;
    private String note;
    private Long userId; // User ID who made the update
}
