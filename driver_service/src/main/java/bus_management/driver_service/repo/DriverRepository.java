package bus_management.driver_service.repo;

import bus_management.driver_service.domain.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DriverRepository extends JpaRepository<Driver, Integer>, JpaSpecificationExecutor<Driver> {
	Driver findByAuthUserId(Long authUserId);
	Driver findByPhone(String phone);
}
