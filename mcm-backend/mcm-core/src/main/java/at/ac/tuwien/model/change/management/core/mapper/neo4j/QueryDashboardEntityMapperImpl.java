package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import at.ac.tuwien.model.change.management.QueryDashboard;
import at.ac.tuwien.model.change.management.graphdb.entities.QueryDashboardEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class QueryDashboardEntityMapperImpl implements QueryDashboardEntityMapper {
    @Override
    public QueryDashboardEntity toEntity(QueryDashboard queryDashboard) {
        var entity = new QueryDashboardEntity();

        entity.setId(queryDashboard.getId());
        entity.setNodeIds(queryDashboard.getNodeIds());

        return entity;
    }

    @Override
    public QueryDashboard fromEntity(QueryDashboardEntity queryDashboardEntity) {
        var dto = new QueryDashboard();

        dto.setId(queryDashboardEntity.getId());
        dto.setNodeIds(queryDashboardEntity.getNodeIds());

        return dto;
    }

    @Override
    public List<QueryDashboard> fromEntities(List<QueryDashboardEntity> queryDashboardEntities) {
        return queryDashboardEntities.stream().map(this::fromEntity).toList();
    }

    @Override
    public List<QueryDashboardEntity> toEntities(List<QueryDashboard> queryDashboardObjects) {
        return queryDashboardObjects.stream().map(this::toEntity).toList();
    }
}
