package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.QueryDashboard;
import at.ac.tuwien.model.change.management.server.dto.QueryDashboardDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QueryDashboardDtoMapper {
    QueryDashboardDTO toDto(QueryDashboard queryDashboard);
    QueryDashboard fromDto(QueryDashboardDTO queryDashboardDTO);
    List<QueryDashboardDTO> toDTOs(List<QueryDashboard> queryDashboards);
}
