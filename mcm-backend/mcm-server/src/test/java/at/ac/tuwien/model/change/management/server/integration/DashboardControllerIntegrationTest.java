package at.ac.tuwien.model.change.management.server.integration;

import at.ac.tuwien.model.change.management.graphdb.dao.DashboardEntityDAO;
import at.ac.tuwien.model.change.management.server.dto.DashboardDTO;
import at.ac.tuwien.model.change.management.server.dto.UserRoleDTO;
import com.fasterxml.jackson.databind.MappingIterator;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Base64;
import java.util.List;

import static at.ac.tuwien.model.change.management.server.Initialize.ADMIN_PASSWORD;
import static at.ac.tuwien.model.change.management.server.Initialize.ADMIN_USERNAME;
import static at.ac.tuwien.model.change.management.server.integration.data.TestData.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardControllerIntegrationTest extends Neo4jIntegrationTest {

    private static final String DASHBOARD_ENDPOINT = "/api/v1/dashboard";

    @Autowired
    private DashboardEntityDAO dashboardRepository;

    @Test
    @DisplayName("Creating a new dashboard persists it in the database with correct data.")
    void testCreateDashboard_givenValidDashboard_persistsNewDashboard() throws Exception {
        final var dashboardDto = validDashboardWithoutIdDTO();

        try (MappingIterator<DashboardDTO> iterator = createDashboard(dashboardDto)) {
            final DashboardDTO dashboard = iterator.next();
            assertNotNull(dashboard, "Create endpoint did not save the dashboard!");
            assertNotNull(dashboard.id(), "Create endpoint did not return a dashboard with an id!");
            assertArrayEquals(
                    dashboardDto.allowedRoles().toArray(),
                    dashboard.allowedRoles().toArray(),
                    "Create endpoint did not return a dashboard with correct allowed roles!"
            );
        }
    }

    @Test
    @DisplayName("Attempting to create a dashboard with invalid arguments returns 400 Bad Request.")
    void testCreateDashboard_givenInvalidDashboard_returnsBadRequest() throws Exception {
        assertEquals(
            Response.Status.BAD_REQUEST.getStatusCode(),
            createDashboardResponse(null).getStatus(),
            "Attempting to create a null dashboard should return 400 Bad Request!"
        );
        assertEquals(
                Response.Status.BAD_REQUEST.getStatusCode(),
                createDashboardResponse(invalidDashboardWithNullRoles()).getStatus(),
                "Attempting to create a null dashboard should return 400 Bad Request!"
        );
        assertEquals(
                Response.Status.BAD_REQUEST.getStatusCode(),
                createDashboardResponse(invalidDashboardWithEmptyRoles()).getStatus(),
                "Attempting to create a null dashboard should return 400 Bad Request!"
        );
    }

    @Test
    @DisplayName("Deleting a dashboard by id removes it from the database.")
    void testDeleteDashboardById_givenValidId_deletesDashboard() throws Exception {
        //Create a dashboard.
        try (var iterator = createDashboard(validDashboardWithoutIdDTO())) {
            final var id = iterator.next().id();
            assert id != null;

            //Now delete it.
            mockMvc.perform(MockMvcRequestBuilders
                            .delete(DASHBOARD_ENDPOINT + "/" + id)
                            .header("Authorization", "Basic " + authToken()))
                    .andExpect(status().isNoContent())
                    .andReturn();

            assertFalse(dashboardRepository.existsById(id), "Deleting a dashboard by id did not remove it from the database!");
            assertFalse(dashboardRepository.findAll().iterator().hasNext(), "Database should be empty at this point!");
        }
    }

    @Test
    @DisplayName("Attempting to delete a non-existing dashboard returns 404 Not Found.")
    void testDeleteDashboardById_givenNonExistingDashboard_returnsNotFound() throws Exception {
        var response = mockMvc.perform(MockMvcRequestBuilders
                        .delete(DASHBOARD_ENDPOINT + "/" + "aaa")
                        .header("Authorization", "Basic " + authToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(
                Response.Status.NOT_FOUND.getStatusCode(),
                response.getStatus(),
                "Attempting to delete a non-existing dashboard should return 404 Not Found!"
        );
    }

    @Test
    @DisplayName("Getting a dashboard by id returns the correct dashboard.")
    void testGetDashboardById_givenExistingDashboard_ReturnsDashboard() throws Exception {

        //1. Create a few dashboards.

        final var dashboard1 = validDashboardWithoutIdDTO();
        final var dashboard2 = validDashboardWithoutIdDTO();

        String id1, id2;
        try (var iterator = createDashboard(dashboard1)) {
            id1 = iterator.next().id();
        }
        try (var iterator = createDashboard(dashboard2)) {
            id2 = iterator.next().id();
        }

        //2. Assert correct dashboards are returned.

        try (var iterator = getDashboard(id1)) {
            final var result = iterator.next();
            assertEquals(id1, result.id(), "Getting a dashboard by id did not return the correct dashboard!");
            assertArrayEquals(
                    dashboard1.allowedRoles().toArray(),
                    result.allowedRoles().toArray(),
                    "Getting a dashboard by id did not return the correct dashboard!"
            );
            assertFalse(iterator.hasNext(), "Only 1 dashboard should be returned!");
        }
        try (var iterator = getDashboard(id2)) {
            final var result = iterator.next();
            assertEquals(id2, result.id(), "Getting a dashboard by id did not return the correct dashboard!");
            assertArrayEquals(
                    dashboard2.allowedRoles().toArray(),
                    result.allowedRoles().toArray(),
                    "Getting a dashboard by id did not return the correct dashboard!"
            );
            assertFalse(iterator.hasNext(), "Only 1 dashboard should be returned!");
        }
    }

    @Test
    @DisplayName("Attempting to get a non-existing dashboard returns 404 Not Found.")
    void testGetDashboardById_givenNonExistingDashboard_returnsNotFound() throws Exception {
        var response = mockMvc.perform(MockMvcRequestBuilders
                        .get(DASHBOARD_ENDPOINT + "/" + "aaa")
                        .header("Authorization", "Basic " + authToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(
                Response.Status.NOT_FOUND.getStatusCode(),
                response.getStatus(),
                "Attempting to get a non-existing dashboard should return 404 Not Found!"
        );
    }

    @Test
    @DisplayName("Getting dashboards by roles returns correct dashboards.")
    void testGetDashboardsByRoles_givenExistingDashboards_returnsCorrectDashboards() throws Exception {

        final var role1 = validNonExistingRole();
        final var role2 = validNonExistingRole();
        final var role3 = validNonExistingRole();
        final var role4 = validNonExistingRole(); //No dashboard with this role.

        final var dashboard1 = new DashboardDTO(null, List.of(role1));
        final var dashboard2 = new DashboardDTO(null, List.of(role2));
        final var dashboard3 = new DashboardDTO(null, List.of(role2, role3));

        //1. Create the dashboards.
        String id1, id2, id3;
        try (var iterator = createDashboard(dashboard1)) {
            id1 = iterator.next().id();
        }
        try (var iterator = createDashboard(dashboard2)) {
            id2 = iterator.next().id();
        }
        try (var iterator = createDashboard(dashboard3)) {
            id3 = iterator.next().id();
        }

        //2. Try to get the correct dashboards according to the roles defined above.
        try (var iterator = getDashboards(List.of(role4))) {
            final var dashboards = iterator.readAll();
            assertNotNull(dashboards, "Get dashboards by role endpoint should not return null!");
            assertTrue(dashboards.isEmpty(), "Get dashboards by role endpoint did not return correct dashboards!");
        }

        try (var iterator = getDashboards(List.of(role1))) {
            final var dashboards = iterator.readAll();
            assertNotNull(dashboards, "Get dashboards by role endpoint should not return null!");
            assertEquals(1, dashboards.size(), "Get dashboards by role endpoint did not return correct dashboards!");
            assertEquals(dashboards.getFirst().id(), id1, "Get dashboards by role endpoint did not return correct dashboards!");
        }

        try (var iterator = getDashboards(List.of(role3))) {
            final var dashboards = iterator.readAll();
            assertNotNull(dashboards, "Get dashboards by role endpoint should not return null!");
            assertEquals(1, dashboards.size(), "Get dashboards by role endpoint did not return correct dashboards!");
            assertEquals(dashboards.getFirst().id(), id3, "Get dashboards by role endpoint did not return correct dashboards!");
        }

        try (var iterator = getDashboards(List.of(role2))) {
            final var dashboards = iterator.readAll();
            assertNotNull(dashboards, "Get dashboards by role endpoint should not return null!");
            assertEquals(2, dashboards.size(), "Get dashboards by role endpoint did not return correct dashboards!");
            assertEquals(dashboards.getFirst().id(), id2, "Get dashboards by role endpoint did not return correct dashboards!");
            assertEquals(dashboards.getLast().id(), id3, "Get dashboards by role endpoint did not return correct dashboards!");
        }
    }

    private static String authToken() {
        return Base64.getEncoder().encodeToString((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes());
    }

    private MockHttpServletResponse createDashboardResponse(DashboardDTO dashboardDto) throws Exception {
        //Helper method that creates a simple dashboard and returns the HTTP response of the endpoint.
        return mockMvc.perform(MockMvcRequestBuilders
                        .post(DASHBOARD_ENDPOINT + "/new")
                        .content(objectMapper.writeValueAsString(dashboardDto))
                        .header("Authorization", "Basic " + authToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    private MappingIterator<DashboardDTO> createDashboard(DashboardDTO dashboardDto) throws Exception {
        //Helper method that creates a simple dashboard and returns the response of the endpoint.
        final byte[] body = mockMvc.perform(MockMvcRequestBuilders
                        .post(DASHBOARD_ENDPOINT + "/new")
                        .content(objectMapper.writeValueAsString(dashboardDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + authToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsByteArray();
        return objectMapper.readerFor(DashboardDTO.class).readValues(body);
    }

    private MappingIterator<DashboardDTO> getDashboards(List<UserRoleDTO> roles) throws Exception {
        //Helper method that calls the endpoint to get all dashboards for a list of roles.
        final byte[] body = mockMvc.perform(MockMvcRequestBuilders
                        .get(DASHBOARD_ENDPOINT)
                        .content(objectMapper.writeValueAsString(roles))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Basic " + authToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsByteArray();
        return objectMapper.readerFor(DashboardDTO.class).readValues(body);
    }

    private MappingIterator<DashboardDTO> getDashboard(String id) throws Exception {
        //Helper method that calls the dashboard endpoint to get a dashboard by its id.
        final byte[] body = mockMvc.perform(MockMvcRequestBuilders
                        .get(DASHBOARD_ENDPOINT + "/" + id)
                        .header("Authorization", "Basic " + authToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsByteArray();
        return objectMapper.readerFor(DashboardDTO.class).readValues(body);
    }

}
