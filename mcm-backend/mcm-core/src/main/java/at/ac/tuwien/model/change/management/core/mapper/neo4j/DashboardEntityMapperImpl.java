package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import at.ac.tuwien.model.change.management.core.model.Dashboard;
import at.ac.tuwien.model.change.management.graphdb.entities.DashboardEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DashboardEntityMapperImpl implements DashboardEntityMapper {
    @Override
    public DashboardEntity toEntity(Dashboard dashboard) {
        //TODO!
        return null;
    }

    @Override
    public Dashboard fromEntity(DashboardEntity dashboardEntity) {
        //TODO!
        return null;
    }

    @Override
    public List<Dashboard> fromEntities(List<DashboardEntity> dashboardEntities) {
        //TODO!
        return List.of();
    }
}
