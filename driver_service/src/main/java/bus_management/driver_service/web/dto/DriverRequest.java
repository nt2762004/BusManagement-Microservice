package bus_management.driver_service.web.dto;

import bus_management.driver_service.domain.Driver;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class DriverRequest {
    @NotBlank
    @Size(max = 100)
    private String fullName;

    @NotNull
    private Driver.LicenseType licenseType;

    @NotNull
    private LocalDate licenseExpiry;

    @NotBlank
    @Size(max = 20)
    private String phone;

    private Driver.DriverStatus status; // optional on create

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public Driver.LicenseType getLicenseType() { return licenseType; }
    public void setLicenseType(Driver.LicenseType licenseType) { this.licenseType = licenseType; }
    public LocalDate getLicenseExpiry() { return licenseExpiry; }
    public void setLicenseExpiry(LocalDate licenseExpiry) { this.licenseExpiry = licenseExpiry; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Driver.DriverStatus getStatus() { return status; }
    public void setStatus(Driver.DriverStatus status) { this.status = status; }
}
