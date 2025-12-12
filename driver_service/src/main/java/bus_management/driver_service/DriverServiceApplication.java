package bus_management.driver_service;

import bus_management.driver_service.repo.DriverRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DriverServiceApplication {
	private static final Logger log = LoggerFactory.getLogger(DriverServiceApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DriverServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner verifyDb(DriverRepository driverRepository) {
		return args -> {
			try {
				long count = driverRepository.count();
				log.info("DriverService started. drivers.count={} ", count);
			} catch (Exception ex) {
				log.error("[STARTUP ERROR] Cannot access database: {}", ex.getMessage(), ex);
				// Re-throw to make the app exit with meaningful error
				throw ex;
			}
		};
	}
}
