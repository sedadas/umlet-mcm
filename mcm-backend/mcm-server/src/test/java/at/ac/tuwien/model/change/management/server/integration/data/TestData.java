package at.ac.tuwien.model.change.management.server.integration.data;

import at.ac.tuwien.model.change.management.server.dto.QueryDashboardDTO;
import at.ac.tuwien.model.change.management.server.dto.UserDTO;
import at.ac.tuwien.model.change.management.server.dto.UserRoleDTO;
import net.datafaker.Faker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestData {

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

    public static Map<String, String> validNonExistingQueryMap() {
        Faker faker = new Faker();
        var queryMap = new HashMap<String, String>();

        queryMap.put(faker.regexify("^[a-zA-Z]{5,20}$"), faker.regexify("^[a-zA-Z]{5,20}$"));
        queryMap.put(faker.regexify("^[a-zA-Z]{5,20}$"), faker.regexify("^[a-zA-Z]{5,20}$"));
        queryMap.put(faker.regexify("^[a-zA-Z]{5,20}$"), faker.regexify("^[a-zA-Z]{5,20}$"));

        return queryMap;
    }

    public static String validNonExistingId() {
        Faker faker = new Faker();
        return faker.regexify("^ID_[a-zA-Z]{5,20}$");
    }

    public static List<String> validNonExistingPermissions() {
        Faker faker = new Faker();
        var permissions = new ArrayList<String>();

        permissions.add(faker.regexify("^[a-zA-Z]{5,20}$"));
        permissions.add(faker.regexify("^[a-zA-Z]{5,20}$"));
        permissions.add(faker.regexify("^[a-zA-Z]{5,20}$"));

        return permissions;
    }

    public static List<QueryDashboardDTO> validNonExistingQueryDashboards() {
        List<QueryDashboardDTO> result = new ArrayList<>();

        result.add(validNonExistingQueryDashboard());
        result.add(validNonExistingQueryDashboard());
        result.add(validNonExistingQueryDashboard());

        return result;
    }

    public static QueryDashboardDTO validNonExistingQueryDashboard() {
        return new QueryDashboardDTO(
            validNonExistingId(),
            validNonExistingQueryMap()
        );
    }

    public static UserDTO validNonExistingUser() {
        return new UserDTO(
            validNonExistingUsername(),
            validPassword(),
            validNonExistingRoles(),
            validNonExistingQueryDashboards()
        );
    }

    public static UserDTO validNonExistingUserWithoutQueryDashboards() {
        return new UserDTO(
                validNonExistingUsername(),
                validPassword(),
                validNonExistingRoles(),
                null
        );
    }

    public static UserDTO invalidUserWithInvalidName() {
        return new UserDTO(
                invalidUsername(),
                validPassword(),
                validNonExistingRoles(),
                validNonExistingQueryDashboards()
        );
    }

    public static UserDTO invalidUserWithInvalidPassword() {
        return new UserDTO(
                validNonExistingUsername(),
                invalidPassword(),
                validNonExistingRoles(),
                validNonExistingQueryDashboards()
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
