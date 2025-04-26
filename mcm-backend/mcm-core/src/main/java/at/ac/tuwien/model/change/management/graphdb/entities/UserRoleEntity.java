package at.ac.tuwien.model.change.management.graphdb.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node("UserRole")
@Getter
@Setter
public class UserRoleEntity {
    @Id
    private String name;

    @Relationship(type = "HAS_PERMISSION")
    private List<UserRoleEntity> permissions;
}
