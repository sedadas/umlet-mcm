package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.model.Dashboard;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface DashboardService {
    Dashboard createDashboard(@NotNull Dashboard dashboard);
    Dashboard getDashboard(@NotNull String id);
    List<Dashboard> getDashboardsForRoles(@NotNull List<String> roles);
    void deleteDashboard(@NotNull String id);
}
