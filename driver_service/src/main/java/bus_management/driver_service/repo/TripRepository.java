package bus_management.driver_service.repo;

import bus_management.driver_service.domain.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, Integer> {}
