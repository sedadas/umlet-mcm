package at.ac.tuwien.model.change.management.graphdb.dao;

import at.ac.tuwien.model.change.management.graphdb.entities.UserEntity;
import at.ac.tuwien.model.change.management.graphdb.entities.UserRoleEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleEntityDAO extends Neo4jRepository<UserRoleEntity, String> {
}
