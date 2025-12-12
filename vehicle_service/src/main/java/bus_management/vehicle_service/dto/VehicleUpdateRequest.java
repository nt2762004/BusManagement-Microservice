package bus_management.vehicle_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VehicleUpdateRequest {
    @NotBlank
    private String type;
    @NotNull @Min(1)
    private Integer seatCount;
    @NotNull @Min(1990) @Max(2050)
    private Integer year;
    @NotNull
    private Boolean active;
}
