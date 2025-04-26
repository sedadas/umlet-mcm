package at.ac.tuwien.model.change.management.graphdb.dao;

import at.ac.tuwien.model.change.management.graphdb.entities.UserEntity;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEntityDAO extends Neo4jRepository<UserEntity, String> {
}
