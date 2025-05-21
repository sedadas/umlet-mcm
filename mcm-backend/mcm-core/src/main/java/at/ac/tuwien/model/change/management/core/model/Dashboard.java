package at.ac.tuwien.model.change.management.core.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class Dashboard {
    private String id;
    private List<UserRole> allowedRoles;
    private List<String> nodeIds;
}
