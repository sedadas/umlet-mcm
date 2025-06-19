package at.ac.tuwien.model.change.management.core.exception.dataspace_import_exceptions;


import at.ac.tuwien.model.change.management.core.exception.DataspaceImportException;

/**
 * Exception thrown when the 'timestamp' field is missing in the uploaded file.
 */
public class MissingTimestampException extends DataspaceImportException {
    public static final String CODE = "DSI_0003_MISSING_TIMESTAMP";
    public MissingTimestampException(int line) {
        super(CODE, String.format("Line %d: missing mandatory 'timestamp'.", line));
    }
}
