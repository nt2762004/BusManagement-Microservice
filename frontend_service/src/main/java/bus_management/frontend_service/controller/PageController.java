package bus_management.frontend_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Nhớ import cái này
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class PageController {

    // 1. Lấy giá trị từ application.properties
    // Local thì nó lấy localhost, lên Server nó lấy giá trị từ Docker
    @Value("${gateway.base-url:http://localhost:8080}")
    private String gatewayUrl;

    // 2. Hàm này sẽ TỰ ĐỘNG chạy trước mọi @GetMapping
    // Nó nhét biến "gatewayUrl" vào tất cả các file HTML
    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("gatewayUrl", gatewayUrl);
    }

    // --- Các hàm cũ giữ nguyên ---
    @GetMapping({"/", "/login"})
    public String login() { return "login"; }

    @GetMapping({"/dashboard"})
    public String adminDashboard() { return "dashboard"; }

    @GetMapping({"/user_mana"})
    public String userManage() { return "user_mana"; }

    @GetMapping({"/settings"})
    public String settingsAdmin() { return "settings"; }

    @GetMapping("/comingsoon")
    public String comingSoon() { return "comingsoon"; }

    @GetMapping("/buses")
    public String vehicleAdmin() { return "vehicle"; }

    @GetMapping("/drivers")
    public String driverAdmin() { return "driver"; }

    @GetMapping("/routes")
    public String routesPage() { return "route"; }

    @GetMapping("/trips")
    public String tripsPage() { return "trip"; }

    @GetMapping("/schedules")
    public String schedulesPage() { return "schedules"; }

    @GetMapping("/reports")
    public String reportsPage() { return "report"; }

    @GetMapping("/logout")
    public String logoutRedirect() {
        return "redirect:/login";
    }
}