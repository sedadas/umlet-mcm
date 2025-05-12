package at.ac.tuwien.model.change.management.server.integration;

import at.ac.tuwien.model.change.management.graphdb.dao.UserEntityDAO;
import at.ac.tuwien.model.change.management.server.dto.UserDTO;
import com.fasterxml.jackson.databind.MappingIterator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

import static at.ac.tuwien.model.change.management.server.controller.Constants.USER_ENDPOINT;

class UserIntegrationTest extends Neo4jIntegrationTest {

    @Autowired
    private UserEntityDAO userRepository;

    @Test
    @DisplayName("Get all users returns correct users.")
    void testGetAllUsers_givenExistingUsers_returnsCorrectUsers() {

    }

    @Test
    @DisplayName("Get all users returns admin user when no users exist.")
    void testGetAllUsers_givenNoUsers_returnsAdminUser() {

    }

    @Test
    @DisplayName("Get user by name returns correct user.")
    void testGetUserByName_givenExistingUser_returnsCorrectUser() {

    }

    @Test
    @DisplayName("Get non-existing user returns 404 Not Found.")
    void testGetUserByName_givenNonExistingUser_returns404() {

    }

    @Test
    @DisplayName("Create user persists and returns new user.")
    void testCreateUser_givenValidUser_returnsNewUser() {

    }

    @Test
    @DisplayName("Create existing user returns 400 Bad Request.")
    void testCreateUser_givenExistingUser_returns400BadRequest() {

    }

    @Test
    @DisplayName("Create user with invalid data returns 400 Bad Request.")
    void testCreateUser_givenInvalidData_returns400BadRequest() {

    }

    @Test
    @DisplayName("Update user persists and returns updated user.")
    void testUpdateUser_givenValidUser_returnsUpdatedUser() {

    }

    @Test
    @DisplayName("Update non-existing user returns 404 Not Found.")
    void testUpdateUser_givenNonExistingUser_returns404() {

    }

    @Test
    @DisplayName("Update existing user with invalid data returns 400 Bad Request.")
    void testUpdateUser_givenInvalidData_returns400BadRequest() {

    }

    @Test
    @DisplayName("Delete user removes it from the database.")
    void testDeleteUser_givenExistingUser_deletesUser() {

    }

    @Test
    @DisplayName("Delete non-existing user returns 404 Not Found.")
    void testDeleteUser_givenNonExistingUser_returns404() {

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
