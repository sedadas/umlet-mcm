package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.UserRoleNotFoundException;
import at.ac.tuwien.model.change.management.core.model.UserRole;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface UserRoleService {
    /**
     * Get all user roles in the system
     * @return a list of userRoles
     */
    List<UserRole> getAll();

    /**
     * Get a user object from the database by the specified name
     * @param name to query for
     * @return the user if found
     * @throws UserRoleNotFoundException if no such user has been found
     */
    UserRole getUserRole(@NotNull String name) throws UserRoleNotFoundException;

    /**
     * Create a new userRole
     * @param newUserRole to create in the database
     * @return the newly created userRole
     */
    UserRole createUserRole(@NotNull UserRole newUserRole);

    /**
     * Update a userRole
     * @param newUserRole user object to modify
     * @return the update userRole
     * @throws UserRoleNotFoundException if no such user has been found
     */
    UserRole updateUserRole(@NotNull UserRole newUserRole) throws UserRoleNotFoundException;

    /**
     * Deletes a userRole
     * @param name of the userRole to delete
     * @throws UserRoleNotFoundException if no such userRole has been found
     */
    void deleteUserRole(@NotNull String name) throws UserRoleNotFoundException;
}
