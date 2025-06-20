package at.ac.tuwien.model.change.management.core.exception.dataspace_import_exceptions;


import at.ac.tuwien.model.change.management.core.exception.DataspaceImportException;
import lombok.extern.slf4j.Slf4j;

/**
 * Exception thrown when the 'timestamp' field is missing in the uploaded file.
 */
@Slf4j
public class MissingTimestampException extends DataspaceImportException {
    public static final String CODE = "DSI_0003_MISSING_TIMESTAMP";
    public MissingTimestampException(int line) {
        super(CODE, String.format("Line %d: missing mandatory 'timestamp'.", line));
        log.error(CODE + ": Line " + line + ": missing mandatory 'timestamp'.");
    }
}
