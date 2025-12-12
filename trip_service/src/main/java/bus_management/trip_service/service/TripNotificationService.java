package bus_management.trip_service.service;

import bus_management.trip_service.entity.TripNotification;
import bus_management.trip_service.repository.TripNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripNotificationService {
    private final TripNotificationRepository notificationRepository;

    /**
     * Lấy danh sách thông báo gần nhất cho một chuyến
     */
    public List<TripNotification> getNotificationsByTripId(Long tripId) {
        return notificationRepository.findTop10ByTripIdOrderByCreatedAtDesc(tripId);
    }

    /**
     * Lấy tất cả thông báo (giới hạn 100 bản ghi gần nhất)
     */
    public List<TripNotification> getAllNotifications() {
        return notificationRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(100)
                .toList();
    }

    /**
     * Tạo thông báo mới
     */
    public TripNotification createNotification(Long tripId, Long userId, String message) {
        if (tripId == null || message == null || message.isBlank()) {
            throw new IllegalArgumentException("tripId and message are required");
        }
        TripNotification notification = TripNotification.builder()
                .tripId(tripId)
                .userId(userId)
                .message(message)
                .build();
        return notificationRepository.save(notification);
    }

    /**
     * Xóa thông báo
     */
    public void deleteNotification(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        notificationRepository.deleteById(id);
    }

    /**
     * Refresh updatedAt của tất cả thông báo (for testing)
     */
    @Transactional
    public int refreshAllUpdatedAt() {
        List<TripNotification> all = notificationRepository.findAll();
        // Use Asia/Ho_Chi_Minh timezone (GMT+7)
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        all.forEach(notif -> {
            // Manually set updatedAt to trigger update
            notif.setUpdatedAt(now);
        });
        notificationRepository.saveAll(all);
        return all.size();
    }

    /**
     * Đánh dấu notification là đã đọc
     */
    @Transactional
    public TripNotification markAsRead(Long id) {
        TripNotification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setStatus("read");
        return notificationRepository.save(notification);
    }

    /**
     * Xóa tất cả notification đã đọc
     */
    @Transactional
    public int deleteReadNotifications() {
        List<TripNotification> readNotifications = notificationRepository.findByStatus("read");
        int count = readNotifications.size();
        notificationRepository.deleteByStatus("read");
        return count;
    }
}
