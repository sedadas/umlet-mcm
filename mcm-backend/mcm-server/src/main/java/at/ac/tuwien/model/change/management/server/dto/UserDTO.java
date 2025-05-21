package at.ac.tuwien.model.change.management.server.dto;

import jakarta.annotation.Nullable;

import java.util.List;

public record UserDTO(
        String username,
        String password,
        List<UserRoleDTO> roles,
        @Nullable List<QueryDashboardDTO> privateDashboards
) {
}
