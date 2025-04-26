package at.ac.tuwien.model.change.management.core.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class User {
    private String username;
    private String password;
    private List<UserRole> roles;
}
