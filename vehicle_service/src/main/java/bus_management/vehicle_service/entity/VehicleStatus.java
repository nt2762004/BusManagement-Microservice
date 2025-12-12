package bus_management.vehicle_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vehicle_status")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class VehicleStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatusType status; // running, idle, maintenance

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private java.sql.Timestamp updatedAt;
}
