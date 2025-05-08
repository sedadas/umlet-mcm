package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import at.ac.tuwien.model.change.management.core.model.Dashboard;
import at.ac.tuwien.model.change.management.graphdb.entities.DashboardEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DashboardEntityMapperImpl implements DashboardEntityMapper {

    private final UserRoleEntityMapper userRoleEntityMapper;

    @Override
    public DashboardEntity toEntity(Dashboard dashboard) {
        var entity = new DashboardEntity();

        entity.setId(dashboard.getId());
        entity.setAllowedRoles(userRoleEntityMapper.toEntities(dashboard.getAllowedRoles()));

        return entity;
    }

    @Override
    public Dashboard fromEntity(DashboardEntity dashboardEntity) {
        var dto = new Dashboard();

        dto.setId(dashboardEntity.getId());
        dto.setAllowedRoles(userRoleEntityMapper.fromEntities(dashboardEntity.getAllowedRoles()));

        return dto;
    }

    @Override
    public List<Dashboard> fromEntities(List<DashboardEntity> dashboardEntities) {
        return dashboardEntities.stream().map(this::fromEntity).toList();
    }

    @Override
    public List<DashboardEntity> toEntities(List<Dashboard> dashboardObjects) {
        return dashboardObjects.stream().map(this::toEntity).toList();
    }
}
