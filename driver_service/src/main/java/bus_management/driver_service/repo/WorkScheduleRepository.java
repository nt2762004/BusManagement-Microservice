package bus_management.driver_service.repo;

import bus_management.driver_service.domain.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Integer> {
    @Query("select ws from WorkSchedule ws where ws.driver.id = :driverId and ws.shiftStart >= :dayStart and ws.shiftStart < :dayEnd order by ws.shiftStart")
    List<WorkSchedule> findForDay(Integer driverId, LocalDateTime dayStart, LocalDateTime dayEnd);

    @Query("select count(ws) > 0 from WorkSchedule ws where ws.driver.id = :driverId and (ws.shiftStart < :proposedEnd) and (ws.shiftEnd > :proposedStart)")
    boolean hasOverlap(Integer driverId, LocalDateTime proposedStart, LocalDateTime proposedEnd);
}
