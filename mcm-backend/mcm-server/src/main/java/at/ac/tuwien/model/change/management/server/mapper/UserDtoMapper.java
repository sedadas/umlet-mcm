package at.ac.tuwien.model.change.management.server.mapper;


import at.ac.tuwien.model.change.management.core.model.User;
import at.ac.tuwien.model.change.management.server.dto.UserDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserRoleDtoMapper.class, QueryDashboardDtoMapperImpl.class})
public interface UserDtoMapper {
    UserDTO toDto(User user);

    User fromDto(UserDTO userDTO);

    List<UserDTO> toDTOs(List<User> userList);
}
