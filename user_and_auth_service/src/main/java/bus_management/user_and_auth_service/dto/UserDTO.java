package bus_management.user_and_auth_service.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private Boolean active;
    private RoleDTO role;

    @Data
    public static class RoleDTO {
        private Long id;
        private String name;
        private String description;
    }
}
