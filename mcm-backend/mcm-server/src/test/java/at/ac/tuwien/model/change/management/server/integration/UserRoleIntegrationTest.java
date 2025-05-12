package at.ac.tuwien.model.change.management.server.integration;

import at.ac.tuwien.model.change.management.graphdb.dao.UserRoleEntityDAO;
import at.ac.tuwien.model.change.management.server.dto.UserRoleDTO;
import com.fasterxml.jackson.databind.MappingIterator;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

import static at.ac.tuwien.model.change.management.server.Initialize.ADMIN_ROLE;
import static at.ac.tuwien.model.change.management.server.controller.Constants.USER_ROLE_ENDPOINT;
import static at.ac.tuwien.model.change.management.server.integration.data.TestData.validNonExistingPermissions;
import static at.ac.tuwien.model.change.management.server.integration.data.TestData.validNonExistingRole;
import static org.junit.jupiter.api.Assertions.*;

class UserRoleIntegrationTest extends Neo4jIntegrationTest {

    @Autowired
    private UserRoleEntityDAO userRoleRepository;

    @Test
    @DisplayName("Creating a new user role persists it in the database with correct data.")
    void testCreateUserRole_givenValidUserRole_persistsNewUserRole() throws Exception {
        final var role = validNonExistingRole();

        var rolesCount = userRoleRepository.count();
        try (var iterator = createUserRole(role)) {
            final var created = iterator.next();
            assertNotNull(created, "Create endpoint did not save the user role!");
            assertEquals(rolesCount + 1, userRoleRepository.count(), "Create endpoint did not persist the user role in the repository!");
            assertEquals(role.name(), created.name(), "Create endpoint did not return a user role with correct name!");
            assertArrayEquals(
                    role.permissions().toArray(),
                    created.permissions().toArray(),
                    "Create endpoint did not return a user role with correct permissions!"
            );
        }
    }

    @Test
    @DisplayName("Creating a user role that already exists returns 400 Bad Request.")
    void testCreateUserRole_givenExistingUserRole_throwsBadRequestException() throws Exception {
        final var role = validNonExistingRole();
        try (var iterator = createUserRole(role)) { /* Empty */ }

        var response = createUserRoleResponse(role);
        assertEquals(
                Response.Status.BAD_REQUEST.getStatusCode(),
                response.getStatus(),
                "Attempting to create an already existing user role should return 400 Bad Request!"
        );
    }

    @Test
    @DisplayName("Get all user roles returns correct roles.")
    void testGetAllUserRoles_givenExistingRoles_returnsRoles() throws Exception {
        final var role1 = validNonExistingRole();
        final var role2 = validNonExistingRole();

        try (var iterator = createUserRole(role1)) { /* Empty */ }
        try (var iterator = createUserRole(role2)) { /* Empty */ }

        try (var iterator = getAllUserRoles()) {
            var roles = iterator.readAll();
            assertNotNull(roles, "Get all user roles did not return correct roles!");
            //Account for admin role!
            assertEquals(2 + 1, roles.size(), "Get all user roles did not return correct roles!");
            roles.removeIf(role -> role.name().equals(ADMIN_ROLE)); //Remove admin role from the list.
            assertEquals(2, roles.size(), "Get all user roles did not return admin role!");
            assertEquals(role1.name(), roles.get(0).name(), "Get all user roles did not return correct roles!");
            assertEquals(role2.name(), roles.get(1).name(), "Get all user roles did not return correct roles!");
        }
    }

    @Test
    @DisplayName("Get all user roles when no roles have been created returns the admin role.")
    void testGetAllUserRoles_givenNoExistingRoles_returnsAdminRole() throws Exception {
        try (var iterator = getAllUserRoles()) {
            final var roles = iterator.readAll();
            assertNotNull(roles, "Get all user roles did not return admin role!");
            assertEquals(1, roles.size(), "Get all user roles did not return admin role!");
            assertEquals(ADMIN_ROLE, roles.getFirst().name(), "Get all user roles did not return correct roles!");
        }
    }

    @Test
    @DisplayName("Get user role by existing ID returns correct role.")
    void testGetUserRole_givenExistingId_returnsRole() throws Exception {
        final var role1 = validNonExistingRole();
        final var role2 = validNonExistingRole();

        try (var iterator = createUserRole(role1)) { /* Empty */ }
        try (var iterator = createUserRole(role2)) { /* Empty */ }

        try (var iterator = getUserRole(role1.name())) {
            final var role = iterator.next();
            assertNotNull(role, "Get user role did not return correct role!");
            assertEquals(role1.name(), role.name(), "Get user role did not return correct role!");
            assertFalse(iterator.hasNext(), "Get user role should return only 1 role!");
        }
    }

    @Test
    @DisplayName("Get user role by non-existing ID returns 404 Not Found.")
    void testGetUserRole_givenNonExistingId_returns404NotFound() throws Exception {
        var response = getUserRoleResponse(null);
        assertEquals(
                Response.Status.NOT_FOUND.getStatusCode(),
                response.getStatus(),
                "Attempting to get a non-existing user role should return 404 Not Found!"
        );

        response = getUserRoleResponse("");
        assertEquals(
                Response.Status.NOT_FOUND.getStatusCode(),
                response.getStatus(),
                "Attempting to get a non-existing user role should return 404 Not Found!"
        );

        response = getUserRoleResponse("aaa");
        assertEquals(
                Response.Status.NOT_FOUND.getStatusCode(),
                response.getStatus(),
                "Attempting to get a non-existing user role should return 404 Not Found!"
        );
    }

    @Test
    @DisplayName("Update user role persists new changes.")
    void testUpdateUserRole_givenExistingRole_updatesRole() throws Exception {
        final var role1 = validNonExistingRole();
        try (var iterator = createUserRole(role1)) { /* Empty */ }

        var role1UpdatedPermissions = new UserRoleDTO(role1.name(), validNonExistingPermissions());
        var count = userRoleRepository.count();
        try (var iterator = updateUserRole(role1UpdatedPermissions)) {
            final var updated = iterator.next();
            assertNotNull(updated, "Update user role did not return correct role!");
            assertEquals(role1UpdatedPermissions.name(), updated.name(), "Updating a user role did not persist changes!");
            assertArrayEquals(role1UpdatedPermissions.permissions().toArray(), updated.permissions().toArray(), "Updating a user role did not persist changes!");
            assertEquals(count, userRoleRepository.count(), "Updating a user role should not create new elements in the database!");
            assertFalse(iterator.hasNext(), "Update user role should return only 1 role!");
        }
    }

    @Test
    @DisplayName("Update non-existing user role returns 404 Not Found.")
    void testUpdateUserRole_givenNonExistingRole_returns404NotFound() throws Exception {
        var response = updateUserRoleResponse(validNonExistingRole());
        assertEquals(
                Response.Status.NOT_FOUND.getStatusCode(),
                response.getStatus(),
                "Attempting to update a non-existing user role should return 404 Not Found!"
        );
    }

    @Test
    @DisplayName("Delete user role by existing ID deletes role.")
    void testDeleteUserRole_givenExistingId_deletesRole() throws Exception {
        final var role = validNonExistingRole();
        try (var iterator = createUserRole(role)) { /* Empty */ }

        var count = userRoleRepository.count();
        var response = deleteUserRole(role.name());
        assertEquals(
                Response.Status.NO_CONTENT.getStatusCode(),
                response.getStatus(),
                "Deleting a user role should return 204 No Content!"
        );
        assertFalse(userRoleRepository.existsById(role.name()), "Deleting a user role by name did not remove it from the database!");
        assertEquals(count - 1, userRoleRepository.count(), "Deleting a user role by name did not remove it from the database!");
    }

    @Test
    @DisplayName("Delete user role by non-existing ID returns 404 Not Found.")
    void testDeleteUserRole_givenNonExistingId_returns404NotFound() throws Exception {
        var count = userRoleRepository.count();

        var response = deleteUserRole(null);
        assertEquals(
                Response.Status.NOT_FOUND.getStatusCode(),
                response.getStatus(),
                "Attempting to delete a non-existing user role should return 404 Not Found!"
        );
        assertEquals(count, userRoleRepository.count(), "Attempting to delete a non-existing user role should not delete anything from the database!");

        response = deleteUserRole("");
        assertEquals(
                Response.Status.NOT_FOUND.getStatusCode(),
                response.getStatus(),
                "Attempting to delete a non-existing user role should return 404 Not Found!"
        );
        assertEquals(count, userRoleRepository.count(), "Attempting to delete a non-existing user role should not delete anything from the database!");

        response = deleteUserRole("aaa");
        assertEquals(
                Response.Status.NOT_FOUND.getStatusCode(),
                response.getStatus(),
                "Attempting to delete a non-existing user role should return 404 Not Found!"
        );
        assertEquals(count, userRoleRepository.count(), "Attempting to delete a non-existing user role should not delete anything from the database!");
    }

    private MappingIterator<UserRoleDTO> createUserRole(UserRoleDTO userRoleDto) throws Exception {
        return mvc.postRequest(userRoleDto, USER_ROLE_ENDPOINT, UserRoleDTO.class);
    }

    private MockHttpServletResponse createUserRoleResponse(UserRoleDTO userRoleDto) throws Exception {
        return mvc.postRequestResponse(userRoleDto, USER_ROLE_ENDPOINT);
    }

    private MappingIterator<UserRoleDTO> updateUserRole(UserRoleDTO userRoleDto) throws Exception {
        return mvc.putRequest(userRoleDto, USER_ROLE_ENDPOINT, UserRoleDTO.class);
    }

    private MockHttpServletResponse updateUserRoleResponse(UserRoleDTO userRoleDto) throws Exception {
        return mvc.putRequestResponse(userRoleDto, USER_ROLE_ENDPOINT);
    }

    private MappingIterator<UserRoleDTO> getUserRole(String name) throws Exception {
        return mvc.getRequest(USER_ROLE_ENDPOINT + "/" + name, UserRoleDTO.class);
    }

    private MockHttpServletResponse getUserRoleResponse(String name) throws Exception {
        return mvc.getRequestResponse(USER_ROLE_ENDPOINT + "/" + name);
    }

    private MappingIterator<UserRoleDTO> getAllUserRoles() throws Exception {
        return mvc.getRequest(USER_ROLE_ENDPOINT, UserRoleDTO.class);
    }

    private MockHttpServletResponse deleteUserRole(String name) throws Exception {
        return mvc.deleteRequest(USER_ROLE_ENDPOINT + "/" + name);
    }
}
