package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.mapper.neo4j.DashboardEntityMapper;
import at.ac.tuwien.model.change.management.core.model.Dashboard;
import at.ac.tuwien.model.change.management.core.model.UserRole;
import at.ac.tuwien.model.change.management.graphdb.dao.DashboardEntityDAO;
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

    @Override
    public Dashboard createDashboard(Dashboard dashboard) {
        return mapper.fromEntity(repository.save(mapper.toEntity(dashboard)));
    }

    @Override
    public Dashboard getDashboard(String id) {
        return mapper.fromEntity(repository.getById(id));
    }

    @Override
    public List<Dashboard> getDashboardsForRoles(List<UserRole> roles) {

        //Query all dashboards that contain at least one of the user roles.

        List<Dashboard> result = new ArrayList<>();

        for (var role : roles) {
            var dashboards = repository.findByAllowedRole(role.getName());
            result.addAll(mapper.fromEntities(dashboards));
        }

        return result;
    }

    @Override
    public void deleteDashboard(String id) {
        repository.deleteById(id);
    }
}
