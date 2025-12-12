package bus_management.user_and_auth_service.service;

import bus_management.user_and_auth_service.entity.User;
import bus_management.user_and_auth_service.entity.Role;
import bus_management.user_and_auth_service.repository.UserRepository;
import bus_management.user_and_auth_service.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    // Đổi trạng thái active của user
    public boolean toggleActive(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(!Boolean.TRUE.equals(user.getActive()));
            userRepository.save(user);
            return true;
        }
        return false;
    }
    // Xóa user theo username
    public boolean deleteUserByUsername(String username) {
        User user = userRepository.findAll().stream()
            .filter(u -> u.getUsername().equalsIgnoreCase(username))
            .findFirst().orElse(null);
        if (user != null) {
            userRepository.delete(user);
            // Kiểm tra lại trong DB
            return !userRepository.existsById(user.getId());
        }
        return false;
    }
    // Kiểm tra trùng username/email, loại trừ id hiện tại nếu sửa
    public boolean isDuplicate(String username, String email, Long excludeId) {
        List<User> users = userRepository.findAll();
        for (User u : users) {
            if ((excludeId == null || !u.getId().equals(excludeId)) &&
                (u.getUsername().equalsIgnoreCase(username) || u.getEmail().equalsIgnoreCase(email))) {
                return true;
            }
        }
        return false;
    }
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User addUser(User user) {
    // Đảm bảo role_id hợp lệ
    if (user.getRole() == null || user.getRole().getId() == null) return null;
    // Kiểm tra trùng username/email
    if (isDuplicate(user.getUsername(), user.getEmail(), null)) return null;
    // Xử lý dữ liệu
    user.setActive(user.getActive() != null ? user.getActive() : true);
    user.setPhone(user.getPhone() != null ? user.getPhone() : "");
    return userRepository.save(user);
    }

    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    /**
     * Create or get a driver user with role name 'TÀI XẾ'.
     * Username = phone, default password = last 6 digits (or 123456).
     */
    public User createOrGetDriverUser(String fullName, String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone must not be empty");
        }
        User existing = userRepository.findByPhone(phone);
        if (existing != null) {
            return existing;
        }
        // Find role 'TÀI XẾ'
        Role driverRole = roleRepository.findAll().stream()
                .filter(r -> "TÀI XẾ".equalsIgnoreCase(r.getName()))
                .findFirst()
                .orElse(null);
        if (driverRole == null) {
            throw new IllegalStateException("Role TÀI XẾ not found");
        }
        User user = new User();
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setUsername(phone); // Using phone as username
        String pwd = phone.length() >= 6 ? phone.substring(phone.length() - 6) : "123456";
        user.setPassword(pwd);
        user.setRole(driverRole);
        user.setActive(true);
        return userRepository.save(user);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id).map(user -> {
            // Kiểm tra trùng username/email
            if (isDuplicate(updatedUser.getUsername(), updatedUser.getEmail(), id)) return null;
            user.setUsername(updatedUser.getUsername());
            user.setFullName(updatedUser.getFullName());
            user.setEmail(updatedUser.getEmail());
            user.setPhone(updatedUser.getPhone());
            user.setActive(updatedUser.getActive() != null ? updatedUser.getActive() : true);
            if (updatedUser.getRole() != null && updatedUser.getRole().getId() != null) {
                user.setRole(updatedUser.getRole());
            }
            // Cập nhật password nếu phía client gửi lên (không rỗng)
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
                user.setPassword(updatedUser.getPassword());
            }
            return userRepository.save(user);
        }).orElse(null);
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
