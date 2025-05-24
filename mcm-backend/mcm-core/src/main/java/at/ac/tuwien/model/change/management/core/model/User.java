package at.ac.tuwien.model.change.management.core.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class User implements UserDetails {
    private String username;
    private String password;
    private List<UserRole> roles;
    private List<QueryDashboard> privateDashboards;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }
}
