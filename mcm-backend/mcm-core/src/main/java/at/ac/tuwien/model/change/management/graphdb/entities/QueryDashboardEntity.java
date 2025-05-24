package at.ac.tuwien.model.change.management.graphdb.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.util.List;

@Node("QueryDashboard")
@Getter
@Setter
public class QueryDashboardEntity {
    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    //Neo4j can't store Maps, so use a list of a custom query object (filters) instead!
    @Relationship(type = "HAS_QUERY") //No need to name it, just make sure the annotation exists so Neo4j saves entities correctly.
    private List<FilterEntity> query;
}
