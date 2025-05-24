package at.ac.tuwien.model.change.management.graphdb.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("FilterEntity")
@Getter
@Setter
public class FilterEntity {
    @Id
    private String key;

    private String value;
}
