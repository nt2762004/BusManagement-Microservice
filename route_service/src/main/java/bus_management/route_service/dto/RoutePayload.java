package bus_management.route_service.dto;

import java.math.BigDecimal;

public class RoutePayload {
    public Long originId;
    public Long destinationId;
    public Integer durationMin; // maps to eta_minutes
    public BigDecimal distanceKm;
    public Boolean active;
}
