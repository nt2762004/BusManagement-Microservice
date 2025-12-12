package bus_management.vehicle_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class VehicleResponse {
    private Long id;
    private String plateNumber;
    private String type;
    private Integer seatCount;
    private Integer year;
    private Boolean active;
    private String currentStatus;
    private String currentStatusNote;
    // Thời điểm cập nhật bản ghi vehicle (cột updated_at của bảng vehicles)
    private Instant updatedAt;
    private Instant currentStatusUpdatedAt;
}
