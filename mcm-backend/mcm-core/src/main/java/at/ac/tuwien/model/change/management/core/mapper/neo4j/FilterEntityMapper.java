package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import at.ac.tuwien.model.change.management.core.model.Filter;
import at.ac.tuwien.model.change.management.graphdb.entities.FilterEntity;

import java.util.List;

public interface FilterEntityMapper {
    FilterEntity toEntity(Filter filter);
    Filter fromEntity(FilterEntity filterEntity);
    List<Filter> fromEntities(List<FilterEntity> filterEntities);
    List<FilterEntity> toEntities(List<Filter> filters);
}
