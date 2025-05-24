package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import at.ac.tuwien.model.change.management.core.model.Filter;
import at.ac.tuwien.model.change.management.graphdb.entities.FilterEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class FilterEntityMapperImpl implements FilterEntityMapper {

    @Override
    public FilterEntity toEntity(Filter filter) {
        var entity = new FilterEntity();

        entity.setKey(filter.getKey());
        entity.setValue(filter.getValue());

        return entity;
    }

    @Override
    public Filter fromEntity(FilterEntity filterEntity) {
        var dto = new Filter();

        dto.setKey(filterEntity.getKey());
        dto.setValue(filterEntity.getValue());

        return dto;
    }

    @Override
    public List<Filter> fromEntities(List<FilterEntity> filterEntities) {
        return filterEntities.stream().map(this::fromEntity).toList();
    }

    @Override
    public List<FilterEntity> toEntities(List<Filter> filters) {
        return filters.stream().map(this::toEntity).toList();
    }
}
