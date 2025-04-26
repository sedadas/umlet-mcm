package at.ac.tuwien.model.change.management.core.exception;

public class UserRoleAlreadyExistsException extends RuntimeException {
    public UserRoleAlreadyExistsException(String message) {
        super(message);
    }
}
