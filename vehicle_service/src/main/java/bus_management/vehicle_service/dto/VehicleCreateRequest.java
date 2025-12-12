package bus_management.vehicle_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VehicleCreateRequest {
    @NotBlank
    private String plateNumber;
    @NotBlank
    private String type;
    @NotNull @Min(1)
    private Integer seatCount;
    @NotNull @Min(1990) @Max(2050)
    private Integer year;
    private Boolean active = true;
}
