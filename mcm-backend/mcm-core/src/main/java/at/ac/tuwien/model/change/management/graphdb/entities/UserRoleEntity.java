package at.ac.tuwien.model.change.management.graphdb.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.List;

@Node("UserRole")
@Getter
@Setter
public class UserRoleEntity {
    @Id
    private String name;

    private List<String> permissions;
}
