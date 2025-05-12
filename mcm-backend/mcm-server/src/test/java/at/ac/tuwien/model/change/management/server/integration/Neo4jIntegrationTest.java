package at.ac.tuwien.model.change.management.server.integration;

import at.ac.tuwien.model.change.management.git.util.PathUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;

import static at.ac.tuwien.model.change.management.server.Initialize.*;

/**
 * Base class for integration tests using a mock MVC to call endpoints.<br/>
 * In this project, for reasons beyond my mortal comprehension, annotating multiple classes with @SpringBootTest causes<br/>
 * a FileLockException on the Neo4j DB lock file.<br/>
 * To fix this, we now have a single @SpringBootTest-annotated abstract base class, which is then extended by the test subclasses.<br/>
 * Extend this class from your custom integration tests - do not add @SpringBootTest in subclasses!
 */

@SpringBootTest
@AutoConfigureMockMvc

//Prevent FileLockException on the DB lock file.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)

//Allows not setting an auth header on HTTP requests with mockMVC.
//Keep in mind that using roles = {} instead of authorities = {} will prefix roles with "ROLE_"!
@WithMockUser(username = ADMIN_USERNAME, password = ADMIN_PASSWORD, authorities = { ADMIN_ROLE })

//Applies @Transactional to each @Test method in every child class.
//@Transactional methods are rolled back, so there are no changes to the DB after tests run.
@Transactional
public abstract class Neo4jIntegrationTest {

    protected static final Path INTEGRATION_TEST_TEMP_FOLDER = Path.of("integration_test_temp");

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected MVCUtil mvc;

    @AfterAll
    static void afterAll() {
        try {
            PathUtils.deleteFilesRecursively(INTEGRATION_TEST_TEMP_FOLDER, false);
        } catch (IOException e) { /* Empty */ }
    }
}
