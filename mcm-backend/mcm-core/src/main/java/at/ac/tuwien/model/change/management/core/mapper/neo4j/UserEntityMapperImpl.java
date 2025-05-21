package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import at.ac.tuwien.model.change.management.core.model.User;
import at.ac.tuwien.model.change.management.graphdb.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserEntityMapperImpl implements UserEntityMapper {

    private final UserRoleEntityMapper userRoleEntityMapper;
    private final QueryDashboardEntityMapper queryDashboardEntityMapper;

    @Override
    public UserEntity toEntity(User user) {
        UserEntity userEntity = new UserEntity();

        userEntity.setUsername(user.getUsername());
        userEntity.setPassword(user.getPassword());

        userEntity.setRoles(new ArrayList<>());
        for(var role : user.getRoles()) {
            userEntity.getRoles().add(userRoleEntityMapper.toEntity(role));
        }

        userEntity.setPrivateDashboards(queryDashboardEntityMapper.toEntities(user.getPrivateDashboards()));

        return userEntity;
    }

    @Override
    public User fromEntity(UserEntity userEntity) {
        User user = new User();

        user.setUsername(userEntity.getUsername());
        user.setPassword(userEntity.getPassword());

        user.setRoles(new ArrayList<>());
        for(var role : userEntity.getRoles()) {
            user.getRoles().add(userRoleEntityMapper.fromEntity(role));
        }

        user.setPrivateDashboards(queryDashboardEntityMapper.fromEntities(userEntity.getPrivateDashboards()));

        return user;
    }

    @Override
    public List<User> fromEntities(List<UserEntity> userEntities) {
        List<User> userList = new ArrayList<>();
        for (var userEntity : userEntities) {
            userList.add(fromEntity(userEntity));
        }
        return userList;
    }
}
