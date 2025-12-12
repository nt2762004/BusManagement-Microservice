package bus_management.vehicle_service.repository;

import bus_management.vehicle_service.entity.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleStatusRepository extends JpaRepository<VehicleStatus, Long> {
    List<VehicleStatus> findTop5ByVehicleIdOrderByUpdatedAtDesc(Long vehicleId);
    VehicleStatus findFirstByVehicleIdOrderByUpdatedAtDesc(Long vehicleId);
}
