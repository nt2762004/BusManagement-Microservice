package bus_management.user_and_auth_service.repository;

import bus_management.user_and_auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional; // <--- Nhớ import dòng này

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Hàm cũ của bạn
    User findByUsernameAndPassword(String username, String password);
    
    User findByPhone(String phone);

    // --- THÊM DÒNG NÀY ---
    // Hàm này giúp tìm user chỉ bằng username (dùng cho SecurityContext)
    Optional<User> findByUsername(String username);
}