package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.mapper.neo4j.DashboardEntityMapper;
import at.ac.tuwien.model.change.management.core.model.Dashboard;
import at.ac.tuwien.model.change.management.graphdb.dao.DashboardEntityDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class DashboardServiceImpl implements DashboardService {

    private final DashboardEntityDAO dashboardRepository;
    private final DashboardEntityMapper dashboardMapper;

    @Override
    public Dashboard createDashboard(Dashboard dashboard) {
        //TODO!
        return null;
    }

    @Override
    public Dashboard getDashboard(String id) {
        //TODO!
        return null;
    }

    @Override
    public List<Dashboard> getDashboardsForRoles(List<String> roles) {
        //TODO!
        return List.of();
    }

    @Override
    public void deleteDashboard(String id) {
        //TODO!
    }
}
