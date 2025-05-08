package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.DashboardNotFoundException;
import at.ac.tuwien.model.change.management.core.mapper.neo4j.DashboardEntityMapper;
import at.ac.tuwien.model.change.management.core.model.Dashboard;
import at.ac.tuwien.model.change.management.core.model.UserRole;
import at.ac.tuwien.model.change.management.core.service.validator.DashboardValidator;
import at.ac.tuwien.model.change.management.graphdb.dao.DashboardEntityDAO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class DashboardServiceImpl implements DashboardService {

    private final DashboardEntityDAO repository;
    private final DashboardEntityMapper mapper;
    private final DashboardValidator validator;

    @Override
    public Dashboard createDashboard(@NotNull Dashboard dashboard) {
        validator.validateForCreate(dashboard);
        return mapper.fromEntity(repository.save(mapper.toEntity(dashboard)));
    }

    @Override
    public Dashboard getDashboard(@NotBlank String id) {
        var found = repository.getById(id);
        if (found == null)
            throw new DashboardNotFoundException("No dashboard exists with id " + id);
        return mapper.fromEntity(found);
    }

    @Override
    public List<Dashboard> getDashboardsForRoles(@NotEmpty List<UserRole> roles) {

        //Query all dashboards that contain at least one of the user roles.

        List<Dashboard> result = new ArrayList<>();

        for (var role : roles) {
            var dashboards = repository.findByAllowedRole(role.getName());
            result.addAll(mapper.fromEntities(dashboards));
        }

        return result;
    }

    @Override
    public void deleteDashboard(@NotBlank String id) {
        var found = repository.getById(id);
        if (found == null)
            throw new DashboardNotFoundException("No dashboard exists with id " + id);
        repository.deleteById(id);
    }
}
