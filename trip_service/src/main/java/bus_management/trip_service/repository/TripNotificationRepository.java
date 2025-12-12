package bus_management.trip_service.repository;

import bus_management.trip_service.entity.TripNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripNotificationRepository extends JpaRepository<TripNotification, Long> {
    List<TripNotification> findTop10ByTripIdOrderByCreatedAtDesc(Long tripId);
    List<TripNotification> findByStatus(String status);
    void deleteByStatus(String status);
}
