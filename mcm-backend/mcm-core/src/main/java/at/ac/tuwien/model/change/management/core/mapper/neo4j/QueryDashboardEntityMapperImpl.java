package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import at.ac.tuwien.model.change.management.core.model.QueryDashboard;
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
        if (queryDashboard == null)
            return null;

        var entity = new QueryDashboardEntity();

        entity.setName(queryDashboard.getName());
        entity.setQuery(queryDashboard.getQuery());

        return entity;
    }

    @Override
    public QueryDashboard fromEntity(QueryDashboardEntity queryDashboardEntity) {
        if (queryDashboardEntity == null)
            return null;

        var dto = new QueryDashboard();

        dto.setName(queryDashboardEntity.getName());
        dto.setQuery(queryDashboardEntity.getQuery());

        return dto;
    }

    @Override
    public List<QueryDashboard> fromEntities(List<QueryDashboardEntity> queryDashboardEntities) {
        if (queryDashboardEntities == null)
            return null;

        return queryDashboardEntities.stream().map(this::fromEntity).toList();
    }

    @Override
    public List<QueryDashboardEntity> toEntities(List<QueryDashboard> queryDashboardObjects) {
        if (queryDashboardObjects == null)
            return null;

        return queryDashboardObjects.stream().map(this::toEntity).toList();
    }
}
