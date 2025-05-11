package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.model.Dashboard;
import at.ac.tuwien.model.change.management.core.model.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface DashboardService {
    Dashboard createDashboard(@NotNull Dashboard dashboard);
    Dashboard getDashboard(@NotBlank String id);
    List<Dashboard> getDashboardsForRoles(@NotEmpty List<UserRole> roles);
    void deleteDashboard(@NotBlank String id);
}
