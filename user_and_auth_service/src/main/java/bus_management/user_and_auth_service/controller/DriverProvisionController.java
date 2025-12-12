package bus_management.user_and_auth_service.controller;

import bus_management.user_and_auth_service.entity.User;
import bus_management.user_and_auth_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class DriverProvisionController {
    @Autowired
    private UserService userService;

    /**
     * Server-to-server endpoint to create or get a driver user by phone.
     * Body: { "fullName": "...", "phone": "..." }
     * Response: { success: true, userId: <id> } or { success: false, error: "..." }
     */
    @PostMapping("/driver-provision")
    public Map<String,Object> createOrGetDriver(@RequestBody Map<String,String> body) {
        try {
            String fullName = body.getOrDefault("fullName", "");
            String phone = body.get("phone");
            User user = userService.createOrGetDriverUser(fullName, phone);
            return Map.of("success", true, "userId", user.getId());
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }
}
