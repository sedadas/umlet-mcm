package at.ac.tuwien.model.change.management.core.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class UserRole implements GrantedAuthority {
    private String name;
    private List<String> permissions;

    @Override
    public String getAuthority() {
        return name;
    }
}
