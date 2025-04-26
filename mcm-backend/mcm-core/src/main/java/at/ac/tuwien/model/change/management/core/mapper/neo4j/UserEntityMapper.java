package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import at.ac.tuwien.model.change.management.core.model.User;
import at.ac.tuwien.model.change.management.graphdb.entities.UserEntity;

import java.util.List;

public interface UserEntityMapper {
    /**
     * Converts a user object to a neo4j entity
     * @param user user object to convert
     * @return the converted user entity
     */
    UserEntity toEntity(User user);

    /**
     * Converts a neo4j user entity to a user object
     * @param userEntity user entity to convert
     * @return the converted user object
     */
    User fromEntity(UserEntity userEntity);

    /**
     * Converts a user entity list to a list of user objects
     * @param userEntities list of user neo4j entities
     * @return list of user objects
     */
    List<User> fromEntities(List<UserEntity> userEntities);
}
