package at.ac.tuwien.model.change.management;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class QueryDashboard {
    private String id;
    private List<String> nodeIds;
}
