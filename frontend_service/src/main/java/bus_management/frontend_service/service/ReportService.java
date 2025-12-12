package bus_management.frontend_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReportService {

    @Value("${gateway.url:http://localhost:8080}")
    private String gatewayUrl;

    private final RestTemplate restTemplate;

    public ReportService() {
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Object> getSystemOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        try {
            // Lấy tổng số xe
            Integer vehicleCount = getVehicleCount();
            overview.put("totalVehicles", vehicleCount != null ? vehicleCount : 0);
            
            // Lấy tổng số tài xế
            Integer driverCount = getDriverCount();
            overview.put("totalDrivers", driverCount != null ? driverCount : 0);
            
            // Lấy tổng số tuyến đường
            Integer routeCount = getRouteCount();
            overview.put("totalRoutes", routeCount != null ? routeCount : 0);
            
            // Lấy tổng số chuyến đi
            Integer tripCount = getTripCount();
            overview.put("totalTrips", tripCount != null ? tripCount : 0);
            
            // Lấy số liệu thực tế từ statistics
            Map<String, Integer> vehicleStats = getVehicleStatistics();
            int activeVehicles = vehicleStats.getOrDefault("Đang chạy", 0);
            overview.put("activeVehicles", activeVehicles);
            
            Map<String, Integer> driverStats = getDriverStatistics();
            int activeDrivers = driverStats.getOrDefault("Đang làm việc", 0);
            overview.put("activeDrivers", activeDrivers);
            
            Map<String, Integer> tripStats = getTripStatistics();
            int completedTrips = tripStats.getOrDefault("Hoàn thành", 0);
            int ongoingTrips = tripStats.getOrDefault("Đang chạy", 0);
            overview.put("completedTrips", completedTrips);
            overview.put("ongoingTrips", ongoingTrips);
            
        } catch (Exception e) {
            System.err.println("Error getting system overview: " + e.getMessage());
        }
        
        return overview;
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // Lấy danh sách xe để thống kê theo trạng thái
            Map<String, Integer> vehicleStats = getVehicleStatistics();
            statistics.put("vehiclesByStatus", vehicleStats);
            
            // Thống kê chuyến đi theo trạng thái
            Map<String, Integer> tripStats = getTripStatistics();
            statistics.put("tripsByStatus", tripStats);
            
            // Thống kê tài xế theo trạng thái
            Map<String, Integer> driverStats = getDriverStatistics();
            statistics.put("driversByStatus", driverStats);
            
        } catch (Exception e) {
            System.err.println("Error getting statistics: " + e.getMessage());
        }
        
        return statistics;
    }

    public Map<String, Object> getTrends(int days) {
        Map<String, Object> trends = new HashMap<>();
        
        try {
            // Tạo dữ liệu trend cho n ngày gần đây
            List<String> dates = getLastNDays(days);
            trends.put("dates", dates);
            
            // Lấy dữ liệu thực từ các service
            List<Integer> tripTrend = getTripTrendData(days);
            trends.put("tripTrend", tripTrend);
            
            List<Integer> vehicleTrend = getVehicleTrendData(days);
            trends.put("vehicleTrend", vehicleTrend);
            
            List<Integer> driverTrend = getDriverTrendData(days);
            trends.put("driverTrend", driverTrend);
            
        } catch (Exception e) {
            System.err.println("Error getting trends: " + e.getMessage());
        }
        
        return trends;
    }

    // Helper methods
    private Integer getVehicleCount() {
        try {
            String url = gatewayUrl + "/api/vehicles";
            Object response = restTemplate.getForObject(url, Object.class);
            List<?> list = extractListFromResponse(response);
            return list != null ? list.size() : 0;
        } catch (Exception e) {
            System.err.println("Error getting vehicle count: " + e.getMessage());
            return 0;
        }
    }

    private Integer getDriverCount() {
        try {
            String url = gatewayUrl + "/api/drivers";
            Object response = restTemplate.getForObject(url, Object.class);
            List<?> list = extractListFromResponse(response);
            return list != null ? list.size() : 0;
        } catch (Exception e) {
            System.err.println("Error getting driver count: " + e.getMessage());
            return 0;
        }
    }

    private Integer getRouteCount() {
        try {
            String url = gatewayUrl + "/api/routes";
            Object response = restTemplate.getForObject(url, Object.class);
            List<?> list = extractListFromResponse(response);
            return list != null ? list.size() : 0;
        } catch (Exception e) {
            System.err.println("Error getting route count: " + e.getMessage());
            return 0;
        }
    }

    private Integer getTripCount() {
        try {
            String url = gatewayUrl + "/api/trips";
            Object response = restTemplate.getForObject(url, Object.class);
            List<?> list = extractListFromResponse(response);
            return list != null ? list.size() : 0;
        } catch (Exception e) {
            System.err.println("Error getting trip count: " + e.getMessage());
            return 0;
        }
    }

    private Map<String, Integer> getVehicleStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("Đang chạy", 0);
        stats.put("Nghỉ", 0);
        stats.put("Bảo dưỡng", 0);
        
        try {
            String url = gatewayUrl + "/api/vehicles";
            Object response = restTemplate.getForObject(url, Object.class);
            List<?> vehicles = extractListFromResponse(response);
            
            if (vehicles != null) {
                for (Object obj : vehicles) {
                    if (obj instanceof java.util.LinkedHashMap) {
                        @SuppressWarnings("unchecked")
                        java.util.LinkedHashMap<String, Object> vehicle = (java.util.LinkedHashMap<String, Object>) obj;
                        
                        // --- SỬA ĐOẠN NÀY: Lấy thêm trường active ---
                        Object activeObj = vehicle.get("active");
                        boolean isActive = false;
                        if (activeObj instanceof Boolean) isActive = (Boolean) activeObj;
                        else if (activeObj != null) isActive = "true".equalsIgnoreCase(activeObj.toString()) || "1".equals(activeObj.toString());
                        // --------------------------------------------

                        String status = (String) vehicle.get("currentStatus");
                        
                        if (status != null) {
                            switch (status.toLowerCase()) {
                                case "running":
                                    // Chỉ tính là đang chạy nếu xe còn hoạt động
                                    if (isActive) {
                                        stats.put("Đang chạy", stats.get("Đang chạy") + 1);
                                    }
                                    break;
                                case "idle":
                                    stats.put("Nghỉ", stats.get("Nghỉ") + 1);
                                    break;
                                case "maintenance":
                                    stats.put("Bảo dưỡng", stats.get("Bảo dưỡng") + 1);
                                    break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting vehicle statistics: " + e.getMessage());
        }
        return stats;
    }

    private Map<String, Integer> getTripStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("Hoàn thành", 0);
        stats.put("Đang chạy", 0);
        stats.put("Chưa chạy", 0);
        stats.put("Đã hủy", 0);
        
        try {
            String url = gatewayUrl + "/api/trips";
            // Get as String first to see the actual response
            String responseStr = restTemplate.getForObject(url, String.class);
            System.out.println("Trip API Response: " + responseStr);
            
            // Try to parse as Object to handle both array and wrapper object
            Object response = restTemplate.getForObject(url, Object.class);
            List<?> trips = null;
            
            if (response instanceof List) {
                trips = (List<?>) response;
            } else if (response instanceof java.util.LinkedHashMap) {
                // If wrapped in object, try to extract list from common keys
                @SuppressWarnings("unchecked")
                java.util.LinkedHashMap<String, Object> map = (java.util.LinkedHashMap<String, Object>) response;
                Object data = map.get("data");
                if (data == null) data = map.get("trips");
                if (data == null) data = map.get("content");
                if (data instanceof List) {
                    trips = (List<?>) data;
                }
            }
            
            if (trips != null) {
                for (Object obj : trips) {
                    if (obj instanceof java.util.LinkedHashMap) {
                        @SuppressWarnings("unchecked")
                        java.util.LinkedHashMap<String, Object> trip = (java.util.LinkedHashMap<String, Object>) obj;
                        String status = (String) trip.get("status");
                        if (status != null) {
                            switch (status) {
                                case "COMPLETED":
                                    stats.put("Hoàn thành", stats.get("Hoàn thành") + 1);
                                    break;
                                case "IN_PROGRESS":
                                    stats.put("Đang chạy", stats.get("Đang chạy") + 1);
                                    break;
                                case "NOT_STARTED":
                                    stats.put("Chưa chạy", stats.get("Chưa chạy") + 1);
                                    break;
                                case "CANCELLED":
                                    stats.put("Đã hủy", stats.get("Đã hủy") + 1);
                                    break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting trip statistics: " + e.getMessage());
            e.printStackTrace();
        }
        return stats;
    }

    private Map<String, Integer> getDriverStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("Đang làm việc", 0);
        stats.put("Nghỉ ngơi", 0);
        
        try {
            String url = gatewayUrl + "/api/drivers";
            
            // Try to parse as Object to handle both array and wrapper object
            Object response = restTemplate.getForObject(url, Object.class);
            List<?> drivers = null;
            
            if (response instanceof List) {
                drivers = (List<?>) response;
            } else if (response instanceof java.util.LinkedHashMap) {
                @SuppressWarnings("unchecked")
                java.util.LinkedHashMap<String, Object> map = (java.util.LinkedHashMap<String, Object>) response;
                Object data = map.get("data");
                if (data == null) data = map.get("drivers");
                if (data == null) data = map.get("content");
                if (data instanceof List) {
                    drivers = (List<?>) data;
                }
            }
            
            if (drivers != null) {
                for (Object obj : drivers) {
                    if (obj instanceof java.util.LinkedHashMap) {
                        @SuppressWarnings("unchecked")
                        java.util.LinkedHashMap<String, Object> driver = (java.util.LinkedHashMap<String, Object>) obj;
                        
                        // Check status field: 'available' or 'inactive'
                        String status = (String) driver.get("status");
                        
                        if (status != null && status.equalsIgnoreCase("available")) {
                            stats.put("Đang làm việc", stats.get("Đang làm việc") + 1);
                        } else {
                            stats.put("Nghỉ ngơi", stats.get("Nghỉ ngơi") + 1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting driver statistics: " + e.getMessage());
            e.printStackTrace();
        }
        return stats;
    }

    // Helper method to extract list from API response (handles wrapper objects)
    private List<?> extractListFromResponse(Object response) {
        if (response instanceof List) {
            return (List<?>) response;
        } else if (response instanceof java.util.LinkedHashMap) {
            @SuppressWarnings("unchecked")
            java.util.LinkedHashMap<String, Object> map = (java.util.LinkedHashMap<String, Object>) response;
            Object data = map.get("data");
            if (data == null) data = map.get("content");
            if (data == null) data = map.get("items");
            if (data instanceof List) {
                return (List<?>) data;
            }
        }
        return null;
    }

    private List<String> getLastNDays(int days) {
        List<String> dates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            dates.add(date.format(formatter));
        }
        
        return dates;
    }

    // Trip trend (logic đếm theo ngày)
    @SuppressWarnings("unchecked")
    private List<Integer> getTripTrendData(int days) {
        List<Integer> trend = new ArrayList<>();
        try {
            String url = gatewayUrl + "/api/trips";
            Object response = restTemplate.getForObject(url, Object.class);
            List<?> trips = extractListFromResponse(response);
            
            if (trips != null) {
                for (int i = days - 1; i >= 0; i--) {
                    LocalDate targetDate = LocalDate.now().minusDays(i);
                    int count = 0;
                    for (Object obj : trips) {
                        if (obj instanceof java.util.LinkedHashMap) {
                            var t = (java.util.LinkedHashMap<String, Object>) obj;
                            // Dùng plannedStart để đếm số lượng chuyến theo kế hoạch
                            String dateStr = (String) t.get("plannedStart"); 
                            if (dateStr != null && dateStr.startsWith(targetDate.toString())) {
                                count++;
                            }
                        }
                    }
                    trend.add(count);
                }
            } else {
                 for (int i = 0; i < days; i++) trend.add(0);
            }
        } catch (Exception e) {
             for (int i = 0; i < days; i++) trend.add(0);
        }
        return trend;
    }

    // Vehicle trend (Chỉ đếm xe thực sự lăn bánh)
    @SuppressWarnings("unchecked")
    private List<Integer> getVehicleTrendData(int days) {
        List<Integer> trend = new ArrayList<>();
        try {
            String url = gatewayUrl + "/api/trips";
            Object response = restTemplate.getForObject(url, Object.class);
            List<?> trips = extractListFromResponse(response);
            
            if (trips != null) {
                for (int i = days - 1; i >= 0; i--) {
                    LocalDate targetDate = LocalDate.now().minusDays(i);
                    Set<Object> usedVehicleIds = new HashSet<>();
                    
                    for (Object obj : trips) {
                        if (obj instanceof java.util.LinkedHashMap) {
                            var t = (java.util.LinkedHashMap<String, Object>) obj;
                            String dateStr = (String) t.get("plannedStart");
                            
                            if (dateStr != null && dateStr.startsWith(targetDate.toString())) {
                                String status = (String) t.get("status");
                                // LỌC: Chỉ tính xe nếu chuyến đang chạy hoặc đã xong
                                if ("IN_PROGRESS".equals(status) || "COMPLETED".equals(status)) {
                                    Object vId = t.get("vehicleId");
                                    if(vId != null) usedVehicleIds.add(vId);
                                }
                            }
                        }
                    }
                    trend.add(usedVehicleIds.size());
                }
            } else {
                for (int i = 0; i < days; i++) trend.add(0);
            }
        } catch (Exception e) {
             for (int i = 0; i < days; i++) trend.add(0);
        }
        return trend;
    }

    // Driver trend (Đếm tài xế được phân công, trừ chuyến hủy)
    @SuppressWarnings("unchecked")
    private List<Integer> getDriverTrendData(int days) {
        List<Integer> trend = new ArrayList<>();
        try {
            String url = gatewayUrl + "/api/trips";
            Object response = restTemplate.getForObject(url, Object.class);
            List<?> trips = extractListFromResponse(response);
            
            if (trips != null) {
                for (int i = days - 1; i >= 0; i--) {
                    LocalDate targetDate = LocalDate.now().minusDays(i);
                    Set<Object> usedDriverIds = new HashSet<>();
                    
                    for (Object obj : trips) {
                        if (obj instanceof java.util.LinkedHashMap) {
                            var t = (java.util.LinkedHashMap<String, Object>) obj;
                            String dateStr = (String) t.get("plannedStart");
                            
                            if (dateStr != null && dateStr.startsWith(targetDate.toString())) {
                                String status = (String) t.get("status");
                                // LỌC: Không đếm chuyến đã hủy
                                if (!"CANCELLED".equals(status)) {
                                    Object dId = t.get("driverId");
                                    if(dId != null) usedDriverIds.add(dId);
                                }
                            }
                        }
                    }
                    trend.add(usedDriverIds.size());
                }
            } else {
                for (int i = 0; i < days; i++) trend.add(0);
            }
        } catch (Exception e) {
             for (int i = 0; i < days; i++) trend.add(0);
        }
        return trend;
    }
}
