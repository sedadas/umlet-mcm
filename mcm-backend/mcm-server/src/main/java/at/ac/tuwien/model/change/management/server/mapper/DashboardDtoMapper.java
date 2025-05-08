package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.Dashboard;
import at.ac.tuwien.model.change.management.server.dto.DashboardDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserRoleDtoMapper.class})
public interface DashboardDtoMapper {
    DashboardDTO toDto(Dashboard dashboard);
    Dashboard fromDto(DashboardDTO dashboardDTO);
    List<DashboardDTO> toDTOs(List<Dashboard> dashboards);
}
