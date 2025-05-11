package at.ac.tuwien.model.change.management.graphdb.dao;

import at.ac.tuwien.model.change.management.graphdb.entities.DashboardEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DashboardEntityDAO extends Neo4jRepository<DashboardEntity, String> {
    DashboardEntity getById(String id);

    @Query(
            "MATCH (dashboard:Dashboard)-[d:VISIBLE_FOR]->(role:UserRole {name: $userRoleName}) " +
            "RETURN dashboard"
    )
    List<DashboardEntity> findByAllowedRole(@Param("userRoleName") String userRoleName);
}
