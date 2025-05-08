package at.ac.tuwien.model.change.management.server.integration.data;

import at.ac.tuwien.model.change.management.server.dto.DashboardDTO;
import at.ac.tuwien.model.change.management.server.dto.UserRoleDTO;
import net.datafaker.Faker;

import java.util.ArrayList;
import java.util.List;

public class TestData {

    public static DashboardDTO validDashboardWithoutIdDTO() {
        return new DashboardDTO(
                null,
                validNonExistingRoles()
        );
    }

    public static List<UserRoleDTO> validNonExistingRoles() {
        var roles = new ArrayList<UserRoleDTO>();

        roles.add(validNonExistingRole());
        roles.add(validNonExistingRole());
        roles.add(validNonExistingRole());

        return roles;
    }

    public static UserRoleDTO validNonExistingRole() {
        Faker faker = new Faker();

        return new UserRoleDTO(
            faker.regexify("^[a-zA-Z]{5,20}$"),
            validNonExistingPermissions()
        );
    }

    public static List<String> validNonExistingPermissions() {
        Faker faker = new Faker();
        var permissions = new ArrayList<String>();

        permissions.add(faker.regexify("^[a-zA-Z]{5,20}$"));
        permissions.add(faker.regexify("^[a-zA-Z]{5,20}$"));
        permissions.add(faker.regexify("^[a-zA-Z]{5,20}$"));

        return permissions;
    }

    private TestData() { /* Empty */ }
}
