package bus_management.route_service.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class RouteDto {
    public Long id;
    public Long originId;
    public String originName;
    public Long destinationId;
    public String destinationName;
    public Integer durationMin;
    public BigDecimal distanceKm;
    public Boolean active;
    public LocalDateTime updatedAt;
}
