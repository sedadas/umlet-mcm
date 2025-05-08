package at.ac.tuwien.model.change.management.graphdb.dao;

import at.ac.tuwien.model.change.management.core.model.Dashboard;
import at.ac.tuwien.model.change.management.core.service.DashboardTestConfig;
import at.ac.tuwien.model.change.management.graphdb.entities.DashboardEntity;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataNeo4jTest //Implicitly marks class as @Transactional
@ContextConfiguration(classes = {DashboardTestConfig.class})
@EnableNeo4jRepositories(repositoryBaseClass = DashboardEntityDAO.class)
class DashboardRepositoryTest {

    //TODO: FIX CLASS AND TEST DASHBOARD QUERIES IN THE REPOSITORY!
    @Autowired
    private DashboardEntityDAO dashboardRepository;

    @Test
    void testSaveDashboard() {
        var d = new DashboardEntity();
        d.setAllowedRoles(List.of());

        var saved = dashboardRepository.save(d);
        assertNotNull(saved.getId());
    }
}