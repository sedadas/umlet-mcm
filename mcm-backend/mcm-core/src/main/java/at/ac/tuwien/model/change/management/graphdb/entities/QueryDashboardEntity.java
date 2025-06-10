package at.ac.tuwien.model.change.management.graphdb.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.*;

import java.util.Map;

@Node("QueryDashboard")
@Getter
@Setter
public class QueryDashboardEntity {
    @Id
    private String name;

    @CompositeProperty
    private Map<String, String> query;
}
