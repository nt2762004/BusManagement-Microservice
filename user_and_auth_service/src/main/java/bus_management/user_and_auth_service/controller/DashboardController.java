package bus_management.user_and_auth_service.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import bus_management.user_and_auth_service.repository.UserRepository;
import bus_management.user_and_auth_service.entity.User;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    // Hàm helper lấy user từ Security Context thay vì Session
    private Map<String,Object> userInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String username = auth.getName();
            // Tìm trong DB để lấy role đầy đủ
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                Map<String,Object> map = new HashMap<>();
                map.put("fullName", user.getFullName());
                map.put("username", user.getUsername());
                map.put("role", user.getRole().getName());
                return map;
            }
        }
        return null;
    }

    private Map<String,Object> systemStatus() {
        String systemStatus = "Hoạt động bình thường";
        String dbStatus = "Kết nối thành công";
        String dbColor = "#28a745";
        try {
            userRepository.count();
            dbStatus = "Kết nối thành công";
            dbColor = "#28a745";
        } catch (Exception e) {
            dbStatus = "Kết nối thất bại";
            dbColor = "#dc3545";
            systemStatus = "Có lỗi hệ thống";
        }
        Map<String,Object> map = new HashMap<>();
        map.put("systemStatus", systemStatus);
        map.put("dbStatus", dbStatus);
        map.put("dbColor", dbColor);
        return map;
    }

    @GetMapping("/admin")
    public Object dashboardAdmin() {
        var user = userInfo(); // Gọi hàm mới không cần session
        if (user == null) return Map.of("success", false, "error", "Chưa đăng nhập");
        
        var status = systemStatus();
        long userCount = userRepository.count();
        
        Map<String,Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("user", user);
        resp.put("system", status);
        resp.put("userCount", userCount);
        return resp;
    }

    @GetMapping("/chu-nha-xe")
    public Object dashboardChuNhaXe() {
        var user = userInfo();
        if (user == null) return Map.of("success", false, "error", "Chưa đăng nhập");
        return Map.of("success", true, "user", user, "system", systemStatus());
    }

    @GetMapping("/dieu-hanh")
    public Object dashboardDieuHanh() {
        var user = userInfo();
        if (user == null) return Map.of("success", false, "error", "Chưa đăng nhập");
        return Map.of("success", true, "user", user, "system", systemStatus());
    }

    @GetMapping("/ky-thuat")
    public Object dashboardKyThuat() {
        var user = userInfo();
        if (user == null) return Map.of("success", false, "error", "Chưa đăng nhập");
        return Map.of("success", true, "user", user, "system", systemStatus());
    }
}