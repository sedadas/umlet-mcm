package at.ac.tuwien.model.change.management.server.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record DashboardDTO(
        @Nullable String id,
        @NotEmpty List<UserRoleDTO> allowedRoles
) {
}
