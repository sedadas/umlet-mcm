package at.ac.tuwien.model.change.management.core.exception;

public class DataspaceImportException extends ApiException {
    public DataspaceImportException(String errorCode, String msg) {
        super(errorCode, msg);
    }

    public DataspaceImportException(String errorCode, String msg, Throwable cause) {
        super(errorCode, msg);
        initCause(cause);
    }
}
