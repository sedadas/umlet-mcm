package at.ac.tuwien.model.change.management.server;

import at.ac.tuwien.model.change.management.core.exception.UserNotFoundException;
import at.ac.tuwien.model.change.management.core.exception.UserRoleNotFoundException;
import at.ac.tuwien.model.change.management.core.model.User;
import at.ac.tuwien.model.change.management.core.model.UserRole;
import at.ac.tuwien.model.change.management.core.service.UserRoleService;
import at.ac.tuwien.model.change.management.core.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class InitializeUsers implements ApplicationRunner {

    public static final String ADMIN_USERNAME = "admin@example.com";
    public static final String ADMIN_PASSWORD = "VerySecurePassword123!";

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleService userRoleService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String bootstrapEnv = System.getenv("CONEMO_BOOTSTRAP");
        boolean bootstrap = bootstrapEnv == null || bootstrapEnv.isBlank() || (!bootstrapEnv.isBlank() && Boolean.parseBoolean(bootstrapEnv));

        if(bootstrap) {
            log.info("Adding default admin user if not present...");

            UserRole adminRole;
            try {
                adminRole = userRoleService.getUserRole("admin");
                log.info("Admin role found, skipping creation...");
            } catch (UserRoleNotFoundException e) {
                log.warn("Admin role not found, creating...");
                adminRole = userRoleService.createUserRole(configureAdminRole());
            }

            try {
                userService.getUser(ADMIN_USERNAME);
                log.info("Admin user found, skipping creation...");
            } catch (UserNotFoundException e) {
                log.warn("Admin user not found, creating...");
                userService.createUser(configureDefaultUser(adminRole));
            }
        }
        else {
            log.info("User bootstrap disabled. Skipping...");
        }
    }

    private UserRole configureAdminRole() {
        UserRole adminRole = new UserRole();
        adminRole.setName("admin");
        return adminRole;
    }

    private User configureDefaultUser(UserRole adminRole) {
        User adminUser = new User();
        adminUser.setUsername(ADMIN_USERNAME);
        adminUser.setPassword(ADMIN_PASSWORD);
        adminUser.setRoles(List.of(adminRole));
        return adminUser;
    }
}
