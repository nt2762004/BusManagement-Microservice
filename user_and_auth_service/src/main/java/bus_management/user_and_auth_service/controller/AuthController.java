package bus_management.user_and_auth_service.controller;

import bus_management.user_and_auth_service.entity.User;
import bus_management.user_and_auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String username = payload.get("username");
        String password = payload.get("password");
        Map<String, Object> result = new HashMap<>();

        log.info("[LOGIN] incoming username='{}'", username);

        if (username == null || password == null) {
            return errorResponse(result, "Thiếu thông tin đăng nhập");
        }

        // 1. Kiểm tra DB (Logic cũ)
        User user = userRepository.findByUsernameAndPassword(username, password);
        if (user == null) {
            log.warn("[LOGIN] invalid credentials for username='{}'", username);
            return errorResponse(result, "Sai tài khoản hoặc mật khẩu");
        }

        if (user.getActive() != null && !user.getActive()) {
            log.warn("[LOGIN] locked account username='{}'", username);
            return errorResponse(result, "Tài khoản của bạn đã bị khóa.");
        }

        // 2. --- LOGIC MỚI QUAN TRỌNG: TÍCH HỢP SPRING SECURITY ---
        
        // a. Tạo danh sách quyền (Role)
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() != null) {
            // Spring Security thường thích có prefix ROLE_, nhưng ta cứ add tên role gốc vào
            authorities.add(new SimpleGrantedAuthority(user.getRole().getName()));
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
        }

        // b. Tạo đối tượng xác thực (Authentication)
        UsernamePasswordAuthenticationToken authReq = 
            new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);

        // c. Lưu vào Security Context
        SecurityContext sc = SecurityContextHolder.createEmptyContext();
        sc.setAuthentication(authReq);
        SecurityContextHolder.setContext(sc);

        // d. --- QUAN TRỌNG NHẤT ---: Lưu Context này vào Session
        // Để các request sau (Dashboard) không bị quên
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

        // ----------------------------------------------------------

        log.info("[LOGIN] success username='{}' role='{}'", username, user.getRole() != null ? user.getRole().getName() : "null");
        
        result.put("success", true);
        result.put("role", user.getRole() != null ? user.getRole().getName() : "");
        result.put("fullName", user.getFullName());
        return result;
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Map.of("success", true);
    }

    @GetMapping("/me")
    public Map<String, Object> me() {
        // Lấy user từ SecurityContext thay vì Session thô
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Map.of("success", false, "error", "Chưa đăng nhập");
        }

        String currentUsername = auth.getName();
        User user = userRepository.findByUsername(currentUsername).orElse(null);

        if (user == null) {
            return Map.of("success", false, "error", "Không tìm thấy thông tin user");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("fullName", user.getFullName());
        data.put("phone", user.getPhone());
        if (user.getRole() != null) {
            data.put("role", user.getRole().getName());
        }
        data.put("active", user.getActive());
        return Map.of("success", true, "user", data);
    }

    @GetMapping("/user/{id}")
    public Map<String, Object> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return Map.of("success", false, "error", "Không tìm thấy người dùng");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("fullName", user.getFullName());
        if (user.getRole() != null) {
            data.put("role", user.getRole().getName());
        }
        return Map.of("success", true, "user", data);
    }

    // Helper
    private Map<String, Object> errorResponse(Map<String, Object> map, String msg) {
        map.put("success", false);
        map.put("error", msg);
        return map;
    }
}