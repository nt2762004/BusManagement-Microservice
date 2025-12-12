package bus_management.trip_service.service;

import bus_management.trip_service.dto.*;
import bus_management.trip_service.entity.*;
import bus_management.trip_service.repository.TripRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TripService {
    private static final Logger log = LoggerFactory.getLogger(TripService.class);
    private static final ZoneId APP_ZONE = ZoneId.systemDefault();
    private final TripRepository tripRepository;
    private final TripNotificationService notificationService;

    /**
     * Create a new trip.
     */
    @Transactional
    @SuppressWarnings({"null"})
    public TripResponse create(TripCreateRequest req){
        Objects.requireNonNull(req, "request must not be null");
        Instant plannedStart = parse(req.getPlannedStart());
        Instant plannedEnd = parse(req.getPlannedEnd());
        validateTimeOrder(plannedStart, plannedEnd, "plannedStart", "plannedEnd");
        
        Trip trip = Trip.builder()
            .routeId(req.getRouteId())
            .vehicleId(req.getVehicleId())
            .driverId(req.getDriverId())
            .status(TripStatus.NOT_STARTED)
            .plannedStartTime(plannedStart)
            .plannedEndTime(plannedEnd)
            .note(req.getNote())
            .build();
            
        Trip saved = tripRepository.save(trip);
        log.debug("Created trip id={}", saved.getId());
        
        // --- ĐOẠN ĐÃ SỬA ---
        try {
            String message = "đã tạo chuyến mới";
            // Sửa null thành req.getUserId() để lưu người tạo
            notificationService.createNotification(saved.getId(), req.getUserId(), message);
        } catch (Exception e) {
            log.warn("Failed to create notification for trip {}", saved.getId(), e);
        }
        // -------------------
        
        return toResponse(saved);
    }

    /**
     * List all trips.
     */
    @Transactional(readOnly = true)
    public List<TripResponse> list(){
        return tripRepository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Get trip by id.
     */
    @Transactional(readOnly = true)
    public TripResponse get(Long id){
        Objects.requireNonNull(id, "id must not be null");
        Trip trip = tripRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Không tìm thấy chuyến"));
        return toResponse(trip);
    }

    /**
     * Update trip fields.
     */
    @Transactional
    @SuppressWarnings({"null"})
    public TripResponse update(Long id, TripUpdateRequest req){
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(req, "request must not be null");
        Trip trip = tripRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Không tìm thấy chuyến"));

        // Store old values for comparison
        Instant oldPlannedStart = trip.getPlannedStartTime();
        Instant oldPlannedEnd = trip.getPlannedEndTime();
        Instant oldActualStart = trip.getActualStartTime();
        Instant oldActualEnd = trip.getActualEndTime();
        Long oldVehicleId = trip.getVehicleId();
        Long oldDriverId = trip.getDriverId();
        TripStatus oldStatus = trip.getStatus();
        
        // Parse times first (do not mutate yet)
        Instant newPlannedStart = req.getPlannedStart()!=null ? parse(req.getPlannedStart()) : trip.getPlannedStartTime();
        Instant newPlannedEnd = req.getPlannedEnd()!=null ? parse(req.getPlannedEnd()) : trip.getPlannedEndTime();
        validateTimeOrder(newPlannedStart, newPlannedEnd, "plannedStart", "plannedEnd");

        Instant newActualStart = req.getActualStart()!=null ? parse(req.getActualStart()) : trip.getActualStartTime();
        Instant newActualEnd = req.getActualEnd()!=null ? parse(req.getActualEnd()) : trip.getActualEndTime();
        validateTimeOrder(newActualStart, newActualEnd, "actualStart", "actualEnd");

        if(req.getRouteId()!=null) trip.setRouteId(req.getRouteId());
        if(req.getVehicleId()!=null) trip.setVehicleId(req.getVehicleId());
        if(req.getDriverId()!=null) trip.setDriverId(req.getDriverId());
        if(req.getPlannedStart()!=null) trip.setPlannedStartTime(newPlannedStart);
        if(req.getPlannedEnd()!=null) trip.setPlannedEndTime(newPlannedEnd);
        if(req.getActualStart()!=null) trip.setActualStartTime(newActualStart);
        if(req.getActualEnd()!=null) trip.setActualEndTime(newActualEnd);
        if(req.getStatus()!=null) trip.setStatus(req.getStatus());
        if(req.getNote()!=null) trip.setNote(req.getNote());

        Trip saved = tripRepository.save(trip);
        log.debug("Updated trip id={}", saved.getId());
        
        // Tạo thông báo cập nhật với userId - chỉ khi có thay đổi thực sự
        try {
            StringBuilder changes = new StringBuilder("đã cập nhật chuyến");
            boolean hasChanges = false;
            
            if(req.getStatus() != null && !req.getStatus().equals(oldStatus)) {
                changes.append("\nTrạng thái: ").append(formatStatus(req.getStatus()));
                hasChanges = true;
            }
            
            boolean actualTimeChanged = false;
            if(req.getActualStart() != null && !newActualStart.equals(oldActualStart)) {
                actualTimeChanged = true;
            }
            if(req.getActualEnd() != null && !newActualEnd.equals(oldActualEnd)) {
                actualTimeChanged = true;
            }
            if(actualTimeChanged) {
                changes.append("\nThời gian thực tế đã thay đổi");
                hasChanges = true;
            }
            
            boolean assignmentChanged = false;
            if(req.getVehicleId() != null && !req.getVehicleId().equals(oldVehicleId)) {
                assignmentChanged = true;
            }
            if(req.getDriverId() != null && !req.getDriverId().equals(oldDriverId)) {
                assignmentChanged = true;
            }
            if(assignmentChanged) {
                changes.append("\nPhân công đã thay đổi");
                hasChanges = true;
            }
            
            boolean plannedTimeChanged = false;
            if(req.getPlannedStart() != null && !newPlannedStart.equals(oldPlannedStart)) {
                plannedTimeChanged = true;
            }
            if(req.getPlannedEnd() != null && !newPlannedEnd.equals(oldPlannedEnd)) {
                plannedTimeChanged = true;
            }
            if(plannedTimeChanged) {
                changes.append("\nThời gian dự kiến đã thay đổi");
                hasChanges = true;
            }
            
            // Only create notification if there are actual changes
            if(hasChanges) {
                notificationService.createNotification(saved.getId(), req.getUserId(), changes.toString());
            }
        } catch (Exception e) {
            log.warn("Failed to create notification for trip {}", saved.getId(), e);
        }
        
        return toResponse(saved);
    }

    /**
     * Delete trip by id.
     */
    @Transactional
    public void delete(Long id){
        Objects.requireNonNull(id, "id must not be null");
        if(!tripRepository.existsById(id)) throw new EntityNotFoundException("Không tìm thấy chuyến");
        tripRepository.deleteById(id);
        log.debug("Deleted trip id={}", id);
    }

    private static String toIso(Instant i){
        return i==null? null : i.toString();
    }
    private static Instant parse(String s){
        if(s==null || s.isBlank()) return null;
        String t = s.trim();
        try {
            return Instant.parse(t);
        } catch (DateTimeParseException ex) {
            LocalDateTime ldt = LocalDateTime.parse(t, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return ldt.atZone(APP_ZONE).toInstant();
        }
    }

    private static void validateTimeOrder(Instant start, Instant end, String startLabel, String endLabel){
        if(start!=null && end!=null && start.isAfter(end)){
            throw new IllegalArgumentException(startLabel + " must be before " + endLabel);
        }
    }
    
    private static String formatStatus(TripStatus status) {
        if(status == null) return "";
        switch(status) {
            case NOT_STARTED: return "Chưa chạy";
            case IN_PROGRESS: return "Đang chạy";
            case COMPLETED: return "Hoàn thành";
            case CANCELLED: return "Hủy";
            default: return status.name();
        }
    }

    private TripResponse toResponse(Trip trip){
        return TripResponse.builder()
            .id(trip.getId())
            .routeId(trip.getRouteId())
            .vehicleId(trip.getVehicleId())
            .driverId(trip.getDriverId())
            .status(trip.getStatus()!=null? trip.getStatus().name(): null)
            .plannedStart(toIso(trip.getPlannedStartTime()))
            .plannedEnd(toIso(trip.getPlannedEndTime()))
            .actualStart(toIso(trip.getActualStartTime()))
            .actualEnd(toIso(trip.getActualEndTime()))
            .note(trip.getNote())
            .updatedAt(trip.getUpdatedAt()==null? null : trip.getUpdatedAt().toInstant().toString())
            .build();
    }
}