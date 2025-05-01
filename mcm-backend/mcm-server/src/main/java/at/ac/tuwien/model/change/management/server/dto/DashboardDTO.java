package at.ac.tuwien.model.change.management.server.dto;

import jakarta.annotation.Nullable;

import java.util.List;

public record DashboardDTO(@Nullable String id, List<UserRoleDTO> roles) {
}
