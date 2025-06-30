package at.ac.tuwien.model.change.management.core.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataspaceImportException extends ApiException {
    public DataspaceImportException(String errorCode, String msg) {
        super(errorCode, msg);
    }

    public DataspaceImportException(String errorCode, String msg, Throwable cause) {
        super(errorCode, msg);
        initCause(cause);
        log.error(msg, cause);
    }
}
