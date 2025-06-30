package at.ac.tuwien.model.change.management.core.exception;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * All API exceptions carry a machine-stable errorCode and a user-facing message.
 */
public abstract class ApiException extends RuntimeException {
    @JsonProperty("errorCode")
    private final String errorCode;

    @JsonProperty("message")
    private final String message;

    protected ApiException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }

    public String getErrorCode() { return errorCode; }

    @Override public String getMessage() { return message; }
}
