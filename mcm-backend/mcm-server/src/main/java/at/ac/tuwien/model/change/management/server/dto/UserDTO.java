package at.ac.tuwien.model.change.management.server.dto;

import java.util.List;

public record UserDTO(
        String username,
        String password,
        List<UserRoleDTO> roles
) {
}
