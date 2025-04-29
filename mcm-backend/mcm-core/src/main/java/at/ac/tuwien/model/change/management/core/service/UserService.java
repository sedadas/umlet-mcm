package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.UserNotFoundException;
import at.ac.tuwien.model.change.management.core.model.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    /**
     * Get all users in the system
     * @return a list of users
     */
    List<User> getAll();

    /**
     * Get a user object from the database by the specified username
     * @param username to query for
     * @return the user if found
     * @throws UserNotFoundException if no such user has been found
     */
    User getUser(@NotNull String username) throws UserNotFoundException;

    /**
     * Create a new user
     * @param newUser to create in the database
     * @return the newly created user
     */
    User createUser(@NotNull User newUser);

    /**
     * Update a user
     * @param newUser user object to modify
     * @return the update user
     * @throws UserNotFoundException if no such user has been found
     */
    User updateUser(@NotNull User newUser) throws UserNotFoundException;

    /**
     * Deletes a user
     * @param username of the username to delete
     * @throws UserNotFoundException if no such user has been found
     */
    void deleteUser(@NotNull String username) throws UserNotFoundException;
}
