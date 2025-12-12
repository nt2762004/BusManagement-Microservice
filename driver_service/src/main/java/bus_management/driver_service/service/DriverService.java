package bus_management.driver_service.service;

import bus_management.driver_service.domain.Driver;
import bus_management.driver_service.repo.DriverRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DriverService {
    private final DriverRepository repo;

    private final RestTemplate restTemplate;
    private final String authBaseUrl;
    private static final Logger log = LoggerFactory.getLogger(DriverService.class);

    public DriverService(DriverRepository repo,
                         RestTemplate restTemplate,
                         @Value("${auth.service.base-url:http://localhost:8080}") String authBaseUrl) {
        this.repo = repo;
        this.restTemplate = restTemplate;
        this.authBaseUrl = authBaseUrl;
    }

    public List<Driver> findAll() { return repo.findAll(); }

    public Optional<Driver> findById(Integer id) { return repo.findById(id); }

    public Driver create(Driver d) {
        Driver saved = repo.save(d);
        // Attempt provisioning of auth user
        try {
            var payload = new java.util.HashMap<String,String>();
            payload.put("fullName", saved.getFullName());
            payload.put("phone", saved.getPhone());
            String url = authBaseUrl + "/api/auth/driver-provision";
            @SuppressWarnings("unchecked")
            var resp = restTemplate.postForObject(url, payload, java.util.Map.class);
            if (resp != null && Boolean.TRUE.equals(resp.get("success"))) {
                Object uid = resp.get("userId");
                if (uid != null) {
                    try {
                        Long userId = Long.valueOf(uid.toString());
                        saved.setAuthUserId(userId);
                        saved = repo.save(saved);
                        log.info("[DriverService] Provisioned auth user {} for driver {}", userId, saved.getId());
                    } catch (NumberFormatException ex) {
                        log.warn("[DriverService] Invalid userId format returned: {}", uid);
                    }
                }
            } else {
                log.warn("[DriverService] Auth provisioning failed for driver {}: {}", saved.getId(), resp != null ? resp.get("error") : "null response");
            }
        } catch (Exception ex) {
            log.error("[DriverService] Exception provisioning auth user for driver {}: {}", saved.getId(), ex.getMessage());
        }
        return saved;
    }

    public Driver update(Integer id, Driver update) {
        Driver d = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Driver not found"));
        d.setFullName(update.getFullName());
        d.setLicenseType(update.getLicenseType());
        d.setLicenseExpiry(update.getLicenseExpiry());
        d.setPhone(update.getPhone());
        if (update.getStatus() != null) d.setStatus(update.getStatus());
        return repo.save(d);
    }

    public void delete(Integer id) { repo.deleteById(id); }

    public Driver updateStatus(Integer id, Driver.DriverStatus status) {
        Driver d = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Driver not found"));
        d.setStatus(status);
        return repo.save(d);
    }

    public List<Driver> search(String q, Driver.DriverStatus status, Driver.LicenseType licenseType) {
    Specification<Driver> spec = (root, cq, cb) -> cb.conjunction();
        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((root, cq, cb) -> cb.or(
                    cb.like(cb.lower(root.get("fullName")), like),
                    cb.like(cb.lower(root.get("phone")), like)
            ));
        }
        if (status != null) {
            Driver.DriverStatus s = status;
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("status"), s));
        }
        if (licenseType != null) {
            Driver.LicenseType lt = licenseType;
            spec = spec.and((root, cq, cb) -> cb.equal(root.get("licenseType"), lt));
        }
        return repo.findAll(spec);
    }

    public boolean unlinkAuthAndDeactivate(Long authUserId, String phone) {
        Driver d = null;
        if (authUserId != null) {
            d = repo.findByAuthUserId(authUserId);
        }
        if (d == null && phone != null && !phone.isBlank()) {
            d = repo.findByPhone(phone);
        }
        if (d == null) return false;
        d.setAuthUserId(null);
        d.setStatus(Driver.DriverStatus.inactive);
        repo.save(d);
        return true;
    }

    public boolean deleteByAuth(Long authUserId, String phone) {
        Driver d = null;
        if (authUserId != null) {
            d = repo.findByAuthUserId(authUserId);
        }
        if (d == null && phone != null && !phone.isBlank()) {
            d = repo.findByPhone(phone);
        }
        if (d == null) return false;
        repo.deleteById(d.getId());
        return true;
    }
}
