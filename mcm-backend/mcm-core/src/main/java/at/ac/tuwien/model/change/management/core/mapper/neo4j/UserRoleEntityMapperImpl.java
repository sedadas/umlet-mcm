package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import at.ac.tuwien.model.change.management.core.model.UserRole;
import at.ac.tuwien.model.change.management.graphdb.entities.UserRoleEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class UserRoleEntityMapperImpl implements UserRoleEntityMapper {
    @Override
    public UserRoleEntity toEntity(UserRole userRole) {
        UserRoleEntity entity = new UserRoleEntity();
        entity.setName(userRole.getName());
        entity.setPermissions(new ArrayList<>());
        //TODO Add list of permissions
        return entity;
    }

    @Override
    public UserRole fromEntity(UserRoleEntity userRoleEntity) {
        UserRole userRole = new UserRole();
        userRole.setName(userRoleEntity.getName());
        return userRole;
    }

    @Override
    public List<UserRole> fromEntities(List<UserRoleEntity> userRoleList) {
        List<UserRole> userRoles = new ArrayList<>();
        for (var roleEntity : userRoleList) {
            userRoles.add(fromEntity(roleEntity));
        }
        return userRoles;
    }
}
