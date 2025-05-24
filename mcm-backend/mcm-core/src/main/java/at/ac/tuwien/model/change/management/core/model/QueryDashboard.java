package at.ac.tuwien.model.change.management.core.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class QueryDashboard {
    private String id;
    private List<Filter> query;
}
