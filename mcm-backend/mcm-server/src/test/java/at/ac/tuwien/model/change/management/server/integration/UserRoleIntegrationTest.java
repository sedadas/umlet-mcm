package at.ac.tuwien.model.change.management.server.integration;

import at.ac.tuwien.model.change.management.graphdb.dao.UserRoleEntityDAO;
import at.ac.tuwien.model.change.management.server.dto.UserRoleDTO;
import com.fasterxml.jackson.databind.MappingIterator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

import static at.ac.tuwien.model.change.management.server.controller.Constants.USER_ROLE_ENDPOINT;
import static at.ac.tuwien.model.change.management.server.integration.data.TestData.validNonExistingRole;
import static org.junit.jupiter.api.Assertions.*;

class UserRoleIntegrationTest extends Neo4jIntegrationTest {

    @Autowired
    private UserRoleEntityDAO userRoleRepository;

    @Test
    @DisplayName("Creating a new user role persists it in the database with correct data.")
    void testCreateUserRole_givenValidUserRole_persistsNewUserRole() throws Exception {
        final var role = validNonExistingRole();

        try (MappingIterator<UserRoleDTO> iterator = createUserRole(role)) {
            final var created = iterator.next();
            assertNotNull(created, "Create endpoint did not save the user role!");
            assertEquals(role.name(), created.name(), "Create endpoint did not return a user role with correct name!");
            assertArrayEquals(
                    role.permissions().toArray(),
                    created.permissions().toArray(),
                    "Create endpoint did not return a user role with correct permissions!"
            );
        }
    }

    private MappingIterator<UserRoleDTO> createUserRole(UserRoleDTO userRoleDto) throws Exception {
        return mvc.postRequest(userRoleDto, USER_ROLE_ENDPOINT, UserRoleDTO.class);
    }

    private MappingIterator<UserRoleDTO> updateUserRole(UserRoleDTO userRoleDto) throws Exception {
        return mvc.putRequest(userRoleDto, USER_ROLE_ENDPOINT, UserRoleDTO.class);
    }

    private MappingIterator<UserRoleDTO> getUserRole(String name) throws Exception {
        return mvc.getRequest(USER_ROLE_ENDPOINT + "/" + name, UserRoleDTO.class);
    }

    private MappingIterator<UserRoleDTO> getAllUserRoles() throws Exception {
        return mvc.getRequest(USER_ROLE_ENDPOINT, UserRoleDTO.class);
    }

    private MockHttpServletResponse deleteUserRole(String name) throws Exception {
        return mvc.deleteRequest(USER_ROLE_ENDPOINT + "/" + name);
    }
}
