package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.Filter;
import at.ac.tuwien.model.change.management.core.model.QueryDashboard;
import at.ac.tuwien.model.change.management.server.dto.QueryDashboardDTO;
import jakarta.annotation.Nullable;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public class QueryDashboardDtoMapperImpl implements QueryDashboardDtoMapper {

    //Neo4j can't store Map objects, so convert the DTO's query Map to a list of Filter objects!

    @Override
    public QueryDashboardDTO toDto(@Nullable QueryDashboard queryDashboard) {
        if (queryDashboard == null)
            return null;

        return new QueryDashboardDTO(
                queryDashboard.getId(),
                filtersToMap(queryDashboard.getQuery())
        );
    }

    @Override
    public QueryDashboard fromDto(@Nullable QueryDashboardDTO queryDashboardDTO) {
        if (queryDashboardDTO == null)
            return null;

        var result = new QueryDashboard();

        result.setId(queryDashboardDTO.id());
        result.setQuery(mapToFilters(queryDashboardDTO.query()));

        return result;
    }

    @Override
    public List<QueryDashboardDTO> toDTOs(@Nullable List<QueryDashboard> queryDashboards) {
        if (queryDashboards == null)
            return null;

        return queryDashboards.stream().map(this::toDto).toList();
    }

    private static List<Filter> mapToFilters(Map<String, String> map) {
        var filters = new ArrayList<Filter>();
        map.forEach((k,v) -> filters.add(new Filter(k, v)));
        return filters;
    }

    private static Map<String, String> filtersToMap(List<Filter> filters) {
        var map = new HashMap<String, String>();
        filters.forEach(f -> map.put(f.getKey(), f.getValue()));
        return map;
    }
}
