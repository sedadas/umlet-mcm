package at.ac.tuwien.model.change.management.server.controller;


import at.ac.tuwien.model.change.management.core.exception.UserNotFoundException;
import at.ac.tuwien.model.change.management.core.exception.UserRoleAlreadyExistsException;
import at.ac.tuwien.model.change.management.core.exception.UserRoleNotFoundException;
import at.ac.tuwien.model.change.management.core.exception.UserValidationException;
import at.ac.tuwien.model.change.management.core.model.UserRole;
import at.ac.tuwien.model.change.management.core.service.UserRoleService;
import at.ac.tuwien.model.change.management.server.dto.UserRoleDTO;
import at.ac.tuwien.model.change.management.server.mapper.UserRoleDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static at.ac.tuwien.model.change.management.server.controller.Constants.USER_ROLE_ENDPOINT;

@Slf4j
@RestController
@RequestMapping(USER_ROLE_ENDPOINT)
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    private final UserRoleDtoMapper userRoleDtoMapper;

    /**
     * Get all roles in the system
     * @return code 200 and a (potentially empty) list of roles
     */
    @GetMapping
    public ResponseEntity<List<UserRoleDTO>> getAll() {
        List<UserRole> roles = userRoleService.getAll();
        return ResponseEntity.ok(userRoleDtoMapper.toDTOs(roles));
    }

    /**
     * Get the role by its name
     * @param name name to search for
     * @return code 200 and the role if found
     *         code 404 if no such role has been found
     */
    @GetMapping("/{name}")
    public ResponseEntity<UserRoleDTO> getUserRole(@PathVariable String name) {
        try {
            var userRole = userRoleService.getUserRole(name);
            return ResponseEntity.ok(userRoleDtoMapper.toDto(userRole));
        } catch (UserRoleNotFoundException e) {
            log.error("Role not found", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create a new Role
     * @param userRoleDTO role to create
     * @return code 200 and the role if successful
     *         code 400 if the role already exists or validation failed
     */
    @PostMapping
    public ResponseEntity<UserRoleDTO> addUserRole(@RequestBody UserRoleDTO userRoleDTO) {
        try {
            var userRole = userRoleService.createUserRole(userRoleDtoMapper.fromDto(userRoleDTO));
            return ResponseEntity.ok(userRoleDtoMapper.toDto(userRole));
        } catch (UserRoleAlreadyExistsException | UserValidationException e) {
            log.error("role invalid", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update an existing Role
     * @param userRoleDTO Role to update
     * @return code 201 and the role if updated
     *         code 404 if the role does not exist
     */
    @PutMapping
    public ResponseEntity<UserRoleDTO> updateUserRole(@RequestBody UserRoleDTO userRoleDTO) {
        try {
            var role = userRoleService.updateUserRole(userRoleDtoMapper.fromDto(userRoleDTO));
            return ResponseEntity.ok(userRoleDtoMapper.toDto(role));
        } catch (UserValidationException e) {
            log.error("User invalid", e);
            return ResponseEntity.badRequest().build();
        } catch (UserRoleNotFoundException e) {
            log.error("User not found", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete an existing role
     * @param name role to delete
     * @return code 204 if successful
     *         code 404 if not found
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteUserRole(@PathVariable String name) {
        try {
            userRoleService.deleteUserRole(name);
            return ResponseEntity.noContent().build();
        } catch (UserRoleNotFoundException e) {
            log.error("Role not found", e);
            return ResponseEntity.notFound().build();
        }
    }
}
