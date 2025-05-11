package at.ac.tuwien.model.change.management.core.service.validator;

import at.ac.tuwien.model.change.management.core.exception.InvalidDashboardException;
import at.ac.tuwien.model.change.management.core.model.Dashboard;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DashboardValidator {
    public void validateForCreate(@NotNull Dashboard dashboard) {
        if (dashboard.getAllowedRoles() == null)
            throw new InvalidDashboardException("Allowed roles of dashboard cannot be null!");
        if (dashboard.getAllowedRoles().isEmpty())
            throw new InvalidDashboardException("Allowed roles of dashboard cannot be empty!");
    }
}
