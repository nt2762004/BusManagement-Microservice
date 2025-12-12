package bus_management.vehicle_service.dto;

import bus_management.vehicle_service.entity.VehicleStatusType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleStatusUpdateRequest {
    @NotNull
    private VehicleStatusType status;
    private String note;
}
