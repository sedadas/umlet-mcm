package at.ac.tuwien.model.change.management.server.integration.data;

import at.ac.tuwien.model.change.management.server.dto.DashboardDTO;
import at.ac.tuwien.model.change.management.server.dto.UserDTO;
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

    public static DashboardDTO invalidDashboardWithNullRoles() {
        return new DashboardDTO(null, null);
    }

    public static DashboardDTO invalidDashboardWithEmptyRoles() {
        return new DashboardDTO(null, List.of());
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

    public static UserDTO validNonExistingUser() {
        return new UserDTO(
            validNonExistingUsername(),
            validPassword(),
            validNonExistingRoles()
        );
    }

    public static UserDTO invalidUserWithInvalidName() {
        return new UserDTO(
                invalidUsername(),
                validPassword(),
                validNonExistingRoles()
        );
    }

    public static UserDTO invalidUserWithInvalidPassword() {
        return new UserDTO(
                validNonExistingUsername(),
                invalidPassword(),
                validNonExistingRoles()
        );
    }

    public static String validNonExistingUsername() {
        Faker faker = new Faker();
        return faker.internet().emailAddress();
    }

    public static String invalidUsername() {
        return "invalidemail.com";
    }

    public static String validPassword() {
        Faker faker = new Faker();
        return faker.internet()
                .password(8, 20, true, false, true)
                .replaceAll("\\s","") //No whitespace characters!
                + "_"; //Add a supported special character.
    }

    public static String invalidPassword() {
        return "pass";
    }

    private TestData() { /* Empty */ }
}
