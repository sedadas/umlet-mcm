package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.*;
import at.ac.tuwien.model.change.management.core.mapper.neo4j.UserRoleEntityMapper;
import at.ac.tuwien.model.change.management.core.model.UserRole;
import at.ac.tuwien.model.change.management.graphdb.dao.UserRoleEntityDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserRoleServiceImpl implements UserRoleService {
    private final UserRoleEntityDAO userRoleRepository;
    private final UserRoleEntityMapper userRoleEntityMapper;

    @Override
    public List<UserRole> getAll() {
        var userRoleEntities = userRoleRepository.findAll();
        return userRoleEntityMapper.fromEntities(userRoleEntities);
    }

    /**
     * Get a user object from the database by the specified name
     *
     * @param name to query for
     * @return the user if found
     * @throws UserRoleNotFoundException if no such user has been found
     */
    @Override
    public UserRole getUserRole(String name) throws UserRoleNotFoundException {
        var userRole = userRoleRepository.findById(name);
        if(userRole.isEmpty()) {
            throw new UserRoleNotFoundException("Role %s not found".formatted(name));
        }
        return userRoleEntityMapper.fromEntity(userRole.get());
    }

    /**
     * Create a new userRole
     *
     * @param newUserRole to create in the database
     * @return the newly created userRole
     */
    @Override
    public UserRole createUserRole(UserRole newUserRole) {
        if(userRoleRepository.existsById(newUserRole.getName())) {
            throw new UserRoleAlreadyExistsException("Role %s already exists".formatted(newUserRole.getName()));
        }
        var userRoleEntity = userRoleEntityMapper.toEntity(newUserRole);
        return userRoleEntityMapper.fromEntity(userRoleRepository.save(userRoleEntity));
    }

    /**
     * Update a userRole
     *
     * @param newUserRole user object to modify
     * @return the update userRole
     * @throws UserRoleNotFoundException if no such user has been found
     */
    @Override
    public UserRole updateUserRole(UserRole newUserRole) throws UserRoleNotFoundException {
        if(!userRoleRepository.existsById(newUserRole.getName())) {
            throw new UserRoleNotFoundException("Role %s not found".formatted(newUserRole.getName()));
        }
        var userRoleEntity = userRoleEntityMapper.toEntity(newUserRole);
        return userRoleEntityMapper.fromEntity(userRoleRepository.save(userRoleEntity));
    }

    /**
     * Deletes a userRole
     *
     * @param name of the userRole to delete
     * @throws UserRoleNotFoundException if no such userRole has been found
     */
    @Override
    public void deleteUserRole(String name) throws UserRoleNotFoundException {
        if(!userRoleRepository.existsById(name)) {
            throw new UserRoleNotFoundException("Role %s not found".formatted(name));
        }
        if(name.equals("admin")) {
            throw new IllegalArgumentException("Cannot delete admin role");
        }
        userRoleRepository.deleteById(name);
    }
}
