package bus_management.user_and_auth_service.controller;

import bus_management.user_and_auth_service.entity.User;
import bus_management.user_and_auth_service.repository.RoleRepository;
import bus_management.user_and_auth_service.repository.UserRepository;
import bus_management.user_and_auth_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private RestTemplate restTemplate;
    
    // --- SỬA CHỖ NÀY: Đổi localhost thành driver-service ---
    // Để khi chạy trên Docker nó tự tìm thấy nhau.
    // Nếu chạy Local máy tính thì bạn cần sửa lại thành localhost, 
    // nhưng ưu tiên chạy được trên AWS để nộp bài trước.
    @Value("${driver.service.base-url:http://localhost:8084}")
    private String driverBaseUrl;

    /* -----------------------------------------------------
       Helper methods
       ----------------------------------------------------- */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String username = auth.getName();
            return userRepository.findByUsername(username).orElse(null);
        }
        return null;
    }

    private boolean isAdmin(User u) {
        return u != null && u.getRole() != null && "ADMIN".equalsIgnoreCase(u.getRole().getName());
    }

    private Map<String,Object> denyNotLogged() { return Map.of("success", false, "error", "Chưa đăng nhập"); }
    private Map<String,Object> denyNotAdmin() { return Map.of("success", false, "error", "Không có quyền (ADMIN required)"); }

    // --- CÁC API ---

    @PostMapping("/settings/change-password")
    public Map<String,Object> changePassword(@RequestBody Map<String,String> payload) {
        User admin = getCurrentUser(); // Dùng hàm mới
        if (admin == null) return denyNotLogged();
        // Admin có thể tự đổi pass, hoặc đổi pass cho người khác (tùy logic, ở đây giữ nguyên logic cũ)
        if (!isAdmin(admin)) return denyNotAdmin();

        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");
        
        if (oldPassword == null || newPassword == null || newPassword.trim().isEmpty()) {
            return Map.of("success", false, "error", "Thiếu thông tin!");
        }
        // Lưu ý: Nếu dùng BCrypt thì phải dùng encoder.matches(). Ở đây giả sử dùng plain text như cũ.
        if (!admin.getPassword().equals(oldPassword)) {
            return Map.of("success", false, "error", "Mật khẩu hiện tại không đúng!");
        }
        admin.setPassword(newPassword);
        userRepository.save(admin);
        return Map.of("success", true);
    }

    @PostMapping("/settings/add-role")
    public Map<String,Object> addRole(@RequestBody Map<String,String> payload) {
        User admin = getCurrentUser();
        if (admin == null) return denyNotLogged();
        if (!isAdmin(admin)) return denyNotAdmin();
        
        String name = payload.get("name");
        String desc = payload.get("description");
        if (name == null || name.trim().isEmpty()) {
            return Map.of("success", false, "error", "Tên vai trò không được để trống!");
        }
        boolean exists = roleRepository.findAll().stream().anyMatch(r -> r.getName().equalsIgnoreCase(name.trim()));
        if (exists) {
            return Map.of("success", false, "error", "Tên vai trò đã tồn tại!");
        }
        var role = new bus_management.user_and_auth_service.entity.Role();
        role.setName(name.trim());
        role.setDescription(desc != null ? desc.trim() : "");
        roleRepository.save(role);
        return Map.of("success", true, "role", role);
    }

    @PutMapping("/settings/edit-role/{id}")
    public Map<String,Object> editRole(@PathVariable Long id, @RequestBody Map<String,String> payload) {
        User admin = getCurrentUser();
        if (admin == null) return denyNotLogged();
        if (!isAdmin(admin)) return denyNotAdmin();
        
        String name = payload.get("name");
        String desc = payload.get("description");
        if (name == null || name.trim().isEmpty()) {
            return Map.of("success", false, "error", "Tên vai trò không được để trống!");
        }
        boolean exists = roleRepository.findAll().stream()
            .anyMatch(r -> r.getName().equalsIgnoreCase(name.trim()) && !r.getId().equals(id));
        if (exists) {
            return Map.of("success", false, "error", "Tên vai trò đã tồn tại!");
        }
        var roleOpt = roleRepository.findById(id);
        if (roleOpt.isEmpty()) {
            return Map.of("success", false, "error", "Không tìm thấy vai trò!");
        }
        var role = roleOpt.get();
        role.setName(name.trim());
        role.setDescription(desc != null ? desc.trim() : "");
        roleRepository.save(role);
        return Map.of("success", true);
    }

    @DeleteMapping("/settings/delete-role/{id}")
    public Map<String,Object> deleteRole(@PathVariable Long id) {
        User admin = getCurrentUser();
        if (admin == null) return denyNotLogged();
        if (!isAdmin(admin)) return denyNotAdmin();
        
        long userCount = userRepository.findAll().stream().filter(u -> u.getRole() != null && u.getRole().getId().equals(id)).count();
        if (userCount > 0) {
            return Map.of("success", false, "error", "Không thể xóa vai trò này vì đang có người dùng sử dụng!");
        }
        roleRepository.deleteById(id);
        return Map.of("success", true);
    }

    @PostMapping("/users/toggle-active/{id}")
    public Map<String,Object> toggleActive(@PathVariable Long id) {
        User admin = getCurrentUser();
        if (admin == null) return denyNotLogged();
        if (!isAdmin(admin)) return denyNotAdmin();
        
        boolean result = userService.toggleActive(id);
        return Map.of("success", result);
    }

    @PostMapping("/users/check-duplicate")
    public Map<String,Boolean> checkDuplicate(@RequestBody Map<String,Object> payload) {
        User admin = getCurrentUser();
        if (admin == null || !isAdmin(admin)) return Map.of("duplicate", true);
        
        String username = (String) payload.get("username");
        String email = (String) payload.get("email");
        Long id = null;
        if (payload.get("id") != null) {
            try { id = Long.valueOf(payload.get("id").toString()); } catch (Exception ignored) {}
        }
        boolean duplicate = userService.isDuplicate(username, email, id);
        return Map.of("duplicate", duplicate);
    }

    @GetMapping("/users")
    public Object listUsers() {
        User user = getCurrentUser();
        if (user == null) return denyNotLogged();
        if (!isAdmin(user)) return denyNotAdmin();
        
        List<User> users = userRepository.findAll()
            .stream()
            .filter(u -> !"admin".equalsIgnoreCase(u.getUsername()))
            .toList();
        return Map.of(
            "success", true,
            "currentUser", Map.of(
                "fullName", user.getFullName(),
                "username", user.getUsername(),
                "role", user.getRole().getName()
            ),
            "users", users,
            "roles", roleRepository.findAll()
        );
    }

    @GetMapping("/settings")
    public Object settings() {
        User user = getCurrentUser();
        if (user == null) return denyNotLogged();
        if (!isAdmin(user)) return denyNotAdmin();
        
        return Map.of(
            "success", true,
            "currentUser", Map.of(
                "fullName", user.getFullName(),
                "username", user.getUsername(),
                "role", user.getRole().getName()
            ),
            "roles", roleRepository.findAll()
        );
    }

    @PostMapping("/users/add")
    public Object addUser(@RequestBody User newUser) {
        User admin = getCurrentUser();
        if (admin == null) return denyNotLogged();
        if (!isAdmin(admin)) return denyNotAdmin();
        
        User saved = userService.addUser(newUser);
        if (saved == null) {
            return Map.of("success", false, "error", "Không thêm được người dùng (trùng username/email hoặc role không hợp lệ)");
        }
        return Map.of("success", true, "users", userRepository.findAll());
    }

    @GetMapping("/users/{id}/password")
    public Object getUserPassword(@PathVariable Long id) {
        User admin = getCurrentUser();
        if (admin == null) return denyNotLogged();
        if (!isAdmin(admin)) return denyNotAdmin();
        
        return userRepository.findById(id)
            .<Object>map(u -> Map.of("success", true, "password", u.getPassword()))
            .orElseGet(() -> Map.of("success", false, "error", "Không tìm thấy người dùng"));
    }

    @PutMapping("/users/edit/{id}")
    public Object editUser(@PathVariable Long id, @RequestBody User updated) {
        User admin = getCurrentUser();
        if (admin == null) return denyNotLogged();
        if (!isAdmin(admin)) return denyNotAdmin();
        
        User saved = userService.updateUser(id, updated);
        if (saved == null) {
            return Map.of("success", false, "error", "Không cập nhật được (trùng username/email hoặc không tìm thấy user)");
        }
        return Map.of("success", true, "users", userRepository.findAll());
    }

    @DeleteMapping("/users/delete/{username}")
    public Object deleteUser(@PathVariable String username) {
        User admin = getCurrentUser();
        if (admin == null) return denyNotLogged();
        if (!isAdmin(admin)) return denyNotAdmin();
        
        System.out.println("[DELETE USER] Request xóa user với username: " + username);
        User toDelete = userRepository.findAll().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst().orElse(null);
        
        userService.deleteUserByUsername(username);
        
        try {
            if (toDelete != null && toDelete.getRole() != null &&
                    "TÀI XẾ".equalsIgnoreCase(toDelete.getRole().getName())) {
                var payload = new java.util.HashMap<String,Object>();
                payload.put("authUserId", toDelete.getId());
                payload.put("phone", toDelete.getPhone());
                // Dùng tên Service trong Docker cho chắc ăn
                // Nhưng biến driverBaseUrl đã được config trong properties rồi
                String url = driverBaseUrl + "/api/drivers/delete-by-auth"; 
                var resp = restTemplate.postForObject(url, payload, java.util.Map.class);
                System.out.println("[DELETE USER] Notify driver_service unlink-auth resp=" + resp);
            }
        } catch (Exception ex) {
            System.out.println("[DELETE USER] Failed to notify driver_service: " + ex.getMessage());
        }
        System.out.println("[DELETE USER] Đã thực hiện xóa user với username: " + username);
        return Map.of("success", true, "users", userRepository.findAll());
    }
}