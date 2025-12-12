package bus_management.trip_service.repository;

import bus_management.trip_service.entity.Trip;
import bus_management.trip_service.entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByStatus(TripStatus status);
    List<Trip> findByRouteId(Long routeId);
    List<Trip> findByPlannedStartTimeBetween(Instant from, Instant to);
}
