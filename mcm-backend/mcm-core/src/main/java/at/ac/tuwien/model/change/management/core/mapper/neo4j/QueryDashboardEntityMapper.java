package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import at.ac.tuwien.model.change.management.core.model.QueryDashboard;
import at.ac.tuwien.model.change.management.graphdb.entities.QueryDashboardEntity;

import java.util.List;

public interface QueryDashboardEntityMapper {
    /**
     * Converts a query dashboard object to a neo4j entity
     * @param queryDashboard query dashboard object to convert
     * @return the converted query dashboard entity
     */
    QueryDashboardEntity toEntity(QueryDashboard queryDashboard);

    /**
     * Converts a neo4j query dashboard entity to a query dashboard object
     * @param queryDashboardEntity query dashboard entity to convert
     * @return the converted query dashboard object
     */
    QueryDashboard fromEntity(QueryDashboardEntity queryDashboardEntity);

    /**
     * Converts a query dashboard entity list to a list of query dashboard objects
     * @param queryDashboardEntities list of query dashboard neo4j entities
     * @return list of query dashboard objects
     */
    List<QueryDashboard> fromEntities(List<QueryDashboardEntity> queryDashboardEntities);

    /**
     * Converts a query dashboard object list to a list of query dashboard entities
     * @param queryDashboardObjects list of query dashboard objects
     * @return list of query dashboard neo4j entities
     */
    List<QueryDashboardEntity> toEntities(List<QueryDashboard> queryDashboardObjects);
}
