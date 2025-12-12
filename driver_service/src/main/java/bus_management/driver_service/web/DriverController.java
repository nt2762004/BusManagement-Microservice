package bus_management.driver_service.web;

import bus_management.driver_service.domain.Driver;
import bus_management.driver_service.service.DriverService;
import bus_management.driver_service.web.dto.DriverRequest;
import bus_management.driver_service.web.dto.StatusUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = {"http://localhost:8000","http://127.0.0.1:8000"}, allowCredentials = "true")
public class DriverController {

    private final DriverService service;

    public DriverController(DriverService service) {
        this.service = service;
    }

    @GetMapping
    public List<Driver> all(@RequestParam(required = false) String q,
                            @RequestParam(required = false) Driver.DriverStatus status,
                            @RequestParam(required = false) Driver.LicenseType licenseType) {
        if ((q != null && !q.isBlank()) || status != null || licenseType != null) {
            return service.search(q, status, licenseType);
        }
        return service.findAll();
    }

    @GetMapping("/available")
    public List<Driver> available() {
        return service.search(null, Driver.DriverStatus.available, null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Driver> getById(@PathVariable Integer id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Driver> create(@Valid @RequestBody DriverRequest req) {
        Driver d = new Driver();
        d.setFullName(req.getFullName());
        d.setLicenseType(req.getLicenseType());
        d.setLicenseExpiry(req.getLicenseExpiry());
        d.setPhone(req.getPhone());
        if (req.getStatus() != null) d.setStatus(req.getStatus());
        Driver saved = service.create(d);
        return ResponseEntity.created(URI.create("/api/drivers/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Driver> update(@PathVariable Integer id, @Valid @RequestBody DriverRequest req) {
        Driver d = new Driver();
        d.setFullName(req.getFullName());
        d.setLicenseType(req.getLicenseType());
        d.setLicenseExpiry(req.getLicenseExpiry());
        d.setPhone(req.getPhone());
        d.setStatus(req.getStatus());
        Driver saved = service.update(id, d);
        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Driver> updateStatus(@PathVariable Integer id, @Valid @RequestBody StatusUpdateRequest req) {
        Driver updated = service.updateStatus(id, req.getStatus());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Service-to-service endpoint: unlink driver auth user and deactivate driver
    @PostMapping("/unlink-auth")
    public Map<String,Object> unlinkAuth(@RequestBody Map<String,Object> body) {
        Long authUserId = null;
        String phone = null;
        if (body.get("authUserId") != null) {
            try { authUserId = Long.valueOf(body.get("authUserId").toString()); } catch (Exception ignored) {}
        }
        if (body.get("phone") != null) {
            phone = body.get("phone").toString();
        }
        boolean ok = service.unlinkAuthAndDeactivate(authUserId, phone);
        return Map.of("success", ok);
    }

    // Service-to-service endpoint: HARD DELETE driver by auth user link (or phone)
    @PostMapping("/delete-by-auth")
    public Map<String, Object> deleteByAuth(@RequestBody Map<String, Object> body) {
        System.out.println(">> DriverController: Nhận lệnh xóa. Body: " + body);
        
        Long authUserId = null;
        String phone = null;

        // 1. Xử lý an toàn authUserId (Chấp nhận cả Integer và Long từ JSON)
        Object idObj = body.get("authUserId");
        if (idObj instanceof Number) {
            authUserId = ((Number) idObj).longValue();
        } else if (idObj instanceof String) {
            try {
                authUserId = Long.parseLong((String) idObj);
            } catch (NumberFormatException e) { /* ignore */ }
        }

        // 2. Xử lý phone
        if (body.get("phone") != null) {
            phone = body.get("phone").toString();
        }

        System.out.println(">> Parsed ID: " + authUserId + ", Phone: " + phone);
        
        boolean ok = service.deleteByAuth(authUserId, phone);
        return Map.of("success", ok);
    }
}
