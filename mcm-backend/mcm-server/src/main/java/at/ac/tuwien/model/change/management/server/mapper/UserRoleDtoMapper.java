package at.ac.tuwien.model.change.management.server.mapper;

import at.ac.tuwien.model.change.management.core.model.UserRole;
import at.ac.tuwien.model.change.management.server.dto.UserRoleDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserRoleDtoMapper {
    UserRoleDTO toDto(UserRole userRole);

    UserRole fromDto(UserRoleDTO userRoleDTO);

    List<UserRoleDTO> toDTOs(List<UserRole> roles);

    List<UserRole> fromDTOs(List<UserRoleDTO> roles);
}
