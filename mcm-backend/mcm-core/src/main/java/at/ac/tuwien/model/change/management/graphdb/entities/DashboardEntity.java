package at.ac.tuwien.model.change.management.graphdb.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node("Dashboard")
@Getter
@Setter
public class DashboardEntity {
    @Id
    private String id;

    @Relationship(value = "VISIBLE_FOR")
    private List<UserRoleEntity> allowedRoles;
}
