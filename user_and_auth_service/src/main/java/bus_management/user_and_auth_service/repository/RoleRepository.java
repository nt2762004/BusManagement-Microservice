package bus_management.user_and_auth_service.repository;

import bus_management.user_and_auth_service.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
