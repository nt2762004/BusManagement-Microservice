package bus_management.user_and_auth_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/")
    public java.util.Map<String,Object> home() {
        return java.util.Map.of("service", "user-and-auth", "status", "ok");
    }
}
