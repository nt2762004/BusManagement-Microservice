package bus_management.trip_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TripResponse {
    private Long id;
    private Long routeId;
    private Long vehicleId;
    private Long driverId;
    private String status;
    private String plannedStart;
    private String plannedEnd;
    private String actualStart;
    private String actualEnd;
    private String note;
    private String updatedAt;
}
