package at.ac.tuwien.model.change.management.graphdb.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node("User")
@Getter
@Setter
public class UserEntity {
    @Id
    private String username;
    private String password;

    @Relationship(type = "HAS_ROLE")
    private List<UserRoleEntity> roles;
}
