package bus_management.driver_service.repo;

import bus_management.driver_service.domain.DriverAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverAssignmentRepository extends JpaRepository<DriverAssignment, Integer> {
    List<DriverAssignment> findByDriver_Id(Integer driverId);
    boolean existsByDriver_IdAndTripId(Integer driverId, Integer tripId);
}
