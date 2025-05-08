package at.ac.tuwien.model.change.management.server.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DashboardDTO(
        @Nullable String id,
        @NotNull List<UserRoleDTO> allowedRoles
) {
}
