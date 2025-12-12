package bus_management.trip_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "trips")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // References to other services by ID only (no FK constraints across services)
    @Column(name = "route_id", nullable = false)
    private Long routeId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "driver_id", nullable = false)
    private Long driverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TripStatus status;

    @Column(name = "planned_start", nullable = false)
    private Instant plannedStartTime;

    @Column(name = "planned_end", nullable = false)
    private Instant plannedEndTime;

    @Column(name = "actual_start")
    private Instant actualStartTime;

    @Column(name = "actual_end")
    private Instant actualEndTime;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", updatable = false, insertable = false)
    private java.sql.Timestamp createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private java.sql.Timestamp updatedAt;
}
