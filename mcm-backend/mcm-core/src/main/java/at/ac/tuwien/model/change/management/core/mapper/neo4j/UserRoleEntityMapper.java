package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import at.ac.tuwien.model.change.management.core.model.UserRole;
import at.ac.tuwien.model.change.management.graphdb.entities.UserRoleEntity;

import java.util.List;

public interface UserRoleEntityMapper {
    /**
     * Converts a user role object to a neo4j entity
     * @param userRole user role object to convert
     * @return the converted user role entity
     */
    UserRoleEntity toEntity(UserRole userRole);

    /**
     * Converts a neo4j user role entity to a user role object
     * @param userRoleEntity user role entity to convert
     * @return the converted user role object
     */
    UserRole fromEntity(UserRoleEntity userRoleEntity);

    /**
     * Converts a role entity list to a list of role objects
     * @param userRoleList list of role neo4j entities
     * @return list of role objects
     */
    List<UserRole> fromEntities(List<UserRoleEntity> userRoleList);

    /**
     * Converts a role object list to a list of role entities
     * @param userRoleList list of role objects
     * @return list of neo4j entities
     */
    List<UserRoleEntity> toEntities(List<UserRole> userRoleList);
}
