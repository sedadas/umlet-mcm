package at.ac.tuwien.model.change.management.server.controller;


import at.ac.tuwien.model.change.management.core.exception.UserAlreadyExistsException;
import at.ac.tuwien.model.change.management.core.exception.UserNotFoundException;
import at.ac.tuwien.model.change.management.core.exception.UserValidationException;
import at.ac.tuwien.model.change.management.core.model.User;
import at.ac.tuwien.model.change.management.core.service.UserService;
import at.ac.tuwien.model.change.management.server.dto.UserDTO;
import at.ac.tuwien.model.change.management.server.mapper.UserDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;

import static at.ac.tuwien.model.change.management.server.controller.Constants.USER_ENDPOINT;

@Slf4j
@RestController
@RequestMapping(USER_ENDPOINT)
@RequiredArgsConstructor
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    private final UserDtoMapper userDtoMapper;

    /**
     * Get all users in the system
     * @return code 200 and a (potentially empty) list of users
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAll() {
        List<User> users = userService.getAll();
        return ResponseEntity.ok(userDtoMapper.toDTOs(users));
    }

    /**
     * Get the user by their username
     * @param username username to search for
     * @return code 200 and the user if found
     *         code 404 if no such user has been found
     */
    @GetMapping("/{username}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String username) {
        try {
            var user = userService.getUser(username);
            return ResponseEntity.ok(userDtoMapper.toDto(user));
        } catch (UserNotFoundException e) {
            log.error("User not found", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get myself as a user
     * @param auth Basic auth header
     * @return code 200 and the user if found
     *         code 404 if no such user has been found
     */
    @GetMapping("/self")
    public ResponseEntity<UserDTO> lookupSelf(@RequestHeader(HttpHeaders.AUTHORIZATION) String auth) {
        try {
            String[] headerRes = auth.split("Basic ");
            byte[] decodedBytes = Base64.getDecoder().decode(headerRes[1]);
            String decodedString = new String(decodedBytes);
            String[] credentials = decodedString.split(":");
            var user = userService.getUser(credentials[0]);
            return ResponseEntity.ok(userDtoMapper.toDto(user));
        } catch (UserNotFoundException e) {
            log.error("User not found", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create a new user
     * @param userDTO user to create
     * @return code 200 and the user if successful
     *         code 400 if the user already exists or validation failed
     */
    @PostMapping
    public ResponseEntity<String> addUser(@RequestBody UserDTO userDTO) {
        try {
            var user = userService.createUser(userDtoMapper.fromDto(userDTO));
            return ResponseEntity.ok("");
        } catch (UserAlreadyExistsException | UserValidationException e) {
            log.error("User invalid", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Update an existing user
     * @param userDTO user to update
     * @return code 201 and the user if updated
     *         code 400 if the validation failed
     *         code 404 if the user does not exist
     */
    @PutMapping
    public ResponseEntity<String> updateUser(@RequestBody UserDTO userDTO) {
        try {
            var user = userService.updateUser(userDtoMapper.fromDto(userDTO));
            return ResponseEntity.ok("");
        } catch (UserValidationException e) {
            log.error("User invalid", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (UserNotFoundException e) {
            log.error("User not found", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete an existing user
     * @param username user to delete
     * @return code 204 if successful
     *         code 404 if not found
     */
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        try {
            userService.deleteUser(username);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            log.error("User not found", e);
            return ResponseEntity.notFound().build();
        }
    }
}
