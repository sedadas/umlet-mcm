package at.ac.tuwien.model.change.management.server.integration;

import at.ac.tuwien.model.change.management.graphdb.dao.UserEntityDAO;
import at.ac.tuwien.model.change.management.server.dto.UserDTO;
import com.fasterxml.jackson.databind.MappingIterator;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static at.ac.tuwien.model.change.management.server.Initialize.*;
import static at.ac.tuwien.model.change.management.server.controller.Constants.USER_ENDPOINT;
import static at.ac.tuwien.model.change.management.server.integration.data.TestData.*;
import static org.junit.jupiter.api.Assertions.*;

class UserIntegrationTest extends Neo4jIntegrationTest {

    @Autowired
    private UserEntityDAO userRepository;

    private final BCryptPasswordEncoder passEncoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("Get all users returns correct users.")
    void testGetAllUsers_givenExistingUsers_returnsCorrectUsers() throws Exception {
        var user1 = validNonExistingUser();
        var user2 = validNonExistingUser();

        try (var iterator = createUser(user1)) { /* Empty */ }
        try (var iterator = createUser(user2)) { /* Empty */ }

        try (var iterator = getAllUsers()) {
            var users = iterator.readAll();
            assertNotNull(users);

            //Account for admin user!
            assertEquals(3, users.size());
            users.removeIf(user -> user.username().equals(ADMIN_USERNAME));

            assertEquals(user1.username(), users.getFirst().username());
            assertTrue(passEncoder.matches(user1.password(), users.getFirst().password()));
            assertArrayEquals(user1.roles().toArray(), users.get(0).roles().toArray());
            assertArrayEquals(user1.privateDashboards().toArray(), users.get(0).privateDashboards().toArray());

            assertEquals(user2.username(), users.get(1).username());
            assertTrue(passEncoder.matches(user2.password(), users.get(1).password()));
            assertArrayEquals(user2.roles().toArray(), users.get(1).roles().toArray());
            assertArrayEquals(user2.privateDashboards().toArray(), users.get(1).privateDashboards().toArray());
        }
    }

    @Test
    @DisplayName("Get all users returns admin user when no users exist.")
    void testGetAllUsers_givenNoUsers_returnsAdminUser() throws Exception {
        try (var iterator = getAllUsers()) {
            var users = iterator.readAll();

            assertNotNull(users);
            assertEquals(1, users.size());
            var admin = users.getFirst();

            assertEquals(ADMIN_USERNAME, admin.username());
            assertTrue(passEncoder.matches(ADMIN_PASSWORD, admin.password()));
        }
    }

    @Test
    @DisplayName("Get user by name returns correct user.")
    void testGetUserByName_givenExistingUser_returnsCorrectUser() throws Exception {
        var user1 = validNonExistingUser();
        var user2 = validNonExistingUser();

        try (var iterator = createUser(user1)) { /* Empty */}
        try (var iterator = createUser(user2)) { /* Empty */}

        try (var iterator = getUser(user1.username())) {
            var users = iterator.readAll();
            assertNotNull(users);
            assertEquals(1, users.size());

            var user = users.getFirst();
            assertEquals(user1.username(), user.username());
            assertTrue(passEncoder.matches(user1.password(), user.password()));
        }

        try (var iterator = getUser(user2.username())) {
            var users = iterator.readAll();
            assertNotNull(users);
            assertEquals(1, users.size());

            var user = users.getFirst();
            assertEquals(user2.username(), user.username());
            assertTrue(passEncoder.matches(user2.password(), user.password()));
        }
    }

    @Test
    @DisplayName("Get non-existing user returns 404 Not Found.")
    void testGetUserByName_givenNonExistingUser_returns404() throws Exception {
        var response = getUserResponse(null);
        assertEquals(
                Response.Status.NOT_FOUND.getStatusCode(),
                response.getStatus()
        );

        response = getUserResponse("");
        assertEquals(
                Response.Status.NOT_FOUND.getStatusCode(),
                response.getStatus()
        );

        response = getUserResponse(invalidUsername());
        assertEquals(
                Response.Status.NOT_FOUND.getStatusCode(),
                response.getStatus()
        );
    }

    @Test
    @DisplayName("Create user persists and returns new user.")
    void testCreateUser_givenValidUser_returnsNewUser() throws Exception {
        var user = validNonExistingUser();
        try (var iterator = createUser(user)) {
            var users = iterator.readAll();
            assertNotNull(users);
            assertEquals(1, users.size());

            var created = users.getFirst();
            assertEquals(user.username(), created.username());
            assertTrue(passEncoder.matches(user.password(), created.password()));
            assertArrayEquals(user.roles().toArray(), created.roles().toArray());
            assertArrayEquals(user.privateDashboards().toArray(), created.privateDashboards().toArray());
        }
    }

    @Test
    @DisplayName("Create existing user returns 400 Bad Request.")
    void testCreateUser_givenExistingUser_returns400BadRequest() throws Exception {
        var user = validNonExistingUser();
        try (var iterator = createUser(user)) { /* Empty */ }

        var response = createUserResponse(user);
        assertEquals(
                Response.Status.BAD_REQUEST.getStatusCode(),
                response.getStatus()
        );
    }

    @Test
    @DisplayName("Create user with invalid data returns 400 Bad Request.")
    void testCreateUser_givenInvalidData_returns400BadRequest() throws Exception {
        var response = createUserResponse(invalidUserWithInvalidName());
        assertEquals(
                Response.Status.BAD_REQUEST.getStatusCode(),
                response.getStatus()
        );

        response = createUserResponse(invalidUserWithInvalidPassword());
        assertEquals(
                Response.Status.BAD_REQUEST.getStatusCode(),
                response.getStatus()
        );
    }

    @Test
    @DisplayName("Update user persists and returns updated user.")
    void testUpdateUser_givenValidUser_returnsUpdatedUser() throws Exception {
        var user = validNonExistingUser();
        try (var iterator = createUser(user)) { /* Empty */}

        var toUpdate = new UserDTO(
                user.username(),
                validPassword(),
                validNonExistingRoles(),
                validNonExistingQueryDashboards()
        );
        try (var iterator = updateUser(toUpdate)) {
            var updated = iterator.next();

            assertEquals(toUpdate.username(), updated.username());
            assertTrue(passEncoder.matches(toUpdate.password(), updated.password()));
            assertArrayEquals(toUpdate.roles().toArray(), updated.roles().toArray());
            assertArrayEquals(toUpdate.privateDashboards().toArray(), updated.privateDashboards().toArray());

            assertFalse(iterator.hasNext());
        }
    }

    @Test
    @DisplayName("Update non-existing user returns 404 Not Found.")
    void testUpdateUser_givenNonExistingUser_returns404() throws Exception {
        var response = updateUserResponse(validNonExistingUser());
        assertEquals(
                Response.Status.NOT_FOUND.getStatusCode(),
                response.getStatus()
        );
    }

    @Test
    @DisplayName("Update existing user with invalid data returns 400 Bad Request.")
    void testUpdateUser_givenInvalidData_returns400BadRequest() throws Exception {
        var user = validNonExistingUser();
        try (var iterator = createUser(user)) { /* Empty */ }

        var invalidUser = new UserDTO(
                user.username(),
                invalidPassword(),
                validNonExistingRoles(),
                validNonExistingQueryDashboards()
        );
        var response = updateUserResponse(invalidUser);
        assertEquals(
                Response.Status.BAD_REQUEST.getStatusCode(),
                response.getStatus()
        );
    }

    @Test
    @DisplayName("Delete user removes it from the database.")
    void testDeleteUser_givenExistingUser_deletesUser() throws Exception {
        var user = validNonExistingUser();
        try (var iterator = createUser(user)) { /* Empty */}

        var count = userRepository.count();
        var response = deleteUser(user.username());
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        assertEquals(count - 1, userRepository.count());
    }

    @Test
    @DisplayName("Delete non-existing user returns 404 Not Found.")
    void testDeleteUser_givenNonExistingUser_returns404() throws Exception {
        var response = deleteUser(null);
        assertEquals(
                Response.Status.NOT_FOUND.getStatusCode(),
                response.getStatus()
        );

        response = deleteUser("");
        assertEquals(
                Response.Status.NOT_FOUND.getStatusCode(),
                response.getStatus()
        );

        response = deleteUser(invalidUsername());
        assertEquals(
                Response.Status.NOT_FOUND.getStatusCode(),
                response.getStatus()
        );
    }

    @Test
    @DisplayName("Editing a user can persist query dashboards.")
    void testEditUser_givenValidUser_updatesQueryDashboards() throws Exception {
        var user = validNonExistingUserWithoutQueryDashboards();
        try (var iterator = createUser(user)) {
            var created = iterator.next();
            assertNull(created.privateDashboards());
        }

        var toUpdate = new UserDTO(
            user.username(),
            user.password(),
            user.roles(),
            validNonExistingQueryDashboards()
        );
        try (var iterator = updateUser(toUpdate)) {
            var updated = iterator.next();
            assertNotNull(updated.privateDashboards());
            assertArrayEquals(toUpdate.privateDashboards().toArray(), updated.privateDashboards().toArray());
        }
    }

    private MappingIterator<UserDTO> createUser(UserDTO userDTO) throws Exception {
        return mvc.postRequest(userDTO, USER_ENDPOINT, UserDTO.class);
    }

    private MockHttpServletResponse createUserResponse(UserDTO userDTO) throws Exception {
        return mvc.postRequestResponse(userDTO, USER_ENDPOINT);
    }

    private MappingIterator<UserDTO> updateUser(UserDTO userDTO) throws Exception {
        return mvc.putRequest(userDTO, USER_ENDPOINT, UserDTO.class);
    }

    private MockHttpServletResponse updateUserResponse(UserDTO userDTO) throws Exception {
        return mvc.putRequestResponse(userDTO, USER_ENDPOINT);
    }

    private MappingIterator<UserDTO> getUser(String name) throws Exception {
        return mvc.getRequest(USER_ENDPOINT + "/" + name, UserDTO.class);
    }

    private MockHttpServletResponse getUserResponse(String name) throws Exception {
        return mvc.getRequestResponse(USER_ENDPOINT + "/" + name);
    }

    private MappingIterator<UserDTO> getAllUsers() throws Exception {
        return mvc.getRequest(USER_ENDPOINT, UserDTO.class);
    }

    private MockHttpServletResponse deleteUser(String name) throws Exception {
        return mvc.deleteRequest(USER_ENDPOINT + "/" + name);
    }
}
