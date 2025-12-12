package bus_management.route_service.repo;

import bus_management.route_service.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, Long> {
    boolean existsByOrigin_IdAndDestination_Id(Long originId, Long destinationId);
}
