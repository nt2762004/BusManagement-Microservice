package bus_management.frontend_service.config;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ViewErrorConfig implements ErrorController {

    @RequestMapping({"/error"})
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        if (status != null && status.toString().equals("404")) {
            return "login"; // hoặc trang 404 tuỳ chỉnh
        }
        return "comingsoon"; // trang fallback chung
    }
}
