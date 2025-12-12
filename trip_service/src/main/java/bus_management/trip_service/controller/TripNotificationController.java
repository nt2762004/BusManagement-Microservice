package bus_management.trip_service.controller;

import bus_management.trip_service.dto.ApiResponse;
import bus_management.trip_service.entity.TripNotification;
import bus_management.trip_service.service.TripNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8000","http://localhost:8080","http://localhost:8081","http://localhost:8085"}, allowCredentials = "true")
public class TripNotificationController {
    private final TripNotificationService notificationService;

    /**
     * Lấy tất cả thông báo (giới hạn 100 gần nhất)
     */
    @GetMapping
    public ApiResponse<List<TripNotification>> getAllNotifications() {
        return ApiResponse.ok(notificationService.getAllNotifications());
    }

    /**
     * Lấy thông báo theo tripId
     */
    @GetMapping("/trip/{tripId}")
    public ApiResponse<List<TripNotification>> getNotificationsByTrip(@PathVariable Long tripId) {
        return ApiResponse.ok(notificationService.getNotificationsByTripId(tripId));
    }

    /**
     * Tạo thông báo mới (internal use)
     */
    @PostMapping
    public ApiResponse<TripNotification> createNotification(@RequestBody Map<String, Object> payload) {
        Long tripId = payload.get("tripId") != null ? Long.valueOf(payload.get("tripId").toString()) : null;
        Long userId = payload.get("userId") != null ? Long.valueOf(payload.get("userId").toString()) : null;
        String message = payload.get("message") != null ? payload.get("message").toString() : "";
        
        TripNotification notification = notificationService.createNotification(tripId, userId, message);
        return ApiResponse.ok(notification);
    }

    /**
     * Đánh dấu notification là đã đọc
     */
    @PutMapping("/{id}/read")
    public ApiResponse<TripNotification> markAsRead(@PathVariable Long id) {
        TripNotification notification = notificationService.markAsRead(id);
        return ApiResponse.ok(notification);
    }

    /**
     * Xóa thông báo
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ApiResponse.ok(null);
    }

    /**
     * Xóa tất cả notification đã đọc
     */
    @DeleteMapping("/read")
    public ApiResponse<Map<String, Object>> deleteReadNotifications() {
        int count = notificationService.deleteReadNotifications();
        return ApiResponse.ok(Map.of("deleted", count));
    }

    /**
     * Refresh updatedAt của tất cả thông báo (for testing)
     */
    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refreshAllUpdatedAt() {
        int count = notificationService.refreshAllUpdatedAt();
        return ApiResponse.ok(Map.of("refreshed", count, "timestamp", System.currentTimeMillis()));
    }
}
