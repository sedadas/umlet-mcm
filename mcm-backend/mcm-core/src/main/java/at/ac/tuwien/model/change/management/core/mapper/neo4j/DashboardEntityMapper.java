package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import at.ac.tuwien.model.change.management.core.model.Dashboard;
import at.ac.tuwien.model.change.management.graphdb.entities.DashboardEntity;

import java.util.List;

public interface DashboardEntityMapper {
    /**
     * Converts a dashboard object to a neo4j entity
     * @param dashboard dashboard object to convert
     * @return the converted dashboard entity
     */
    DashboardEntity toEntity(Dashboard dashboard);

    /**
     * Converts a neo4j dashboard entity to a dashboard object
     * @param dashboardEntity dashboard entity to convert
     * @return the converted dashboard object
     */
    Dashboard fromEntity(DashboardEntity dashboardEntity);

    /**
     * Converts a dashboard entity list to a list of dashboard objects
     * @param dashboardEntities list of dashboard neo4j entities
     * @return list of dashboard objects
     */
    List<Dashboard> fromEntities(List<DashboardEntity> dashboardEntities);

    /**
     * Converts a dashboard object list to a list of dashboard entities
     * @param dashboardObjects list of dashboard objects
     * @return list of dashboard neo4j entities
     */
    List<DashboardEntity> toEntities(List<Dashboard> dashboardObjects);
}
