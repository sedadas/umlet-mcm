package at.ac.tuwien.model.change.management.core.exception.dataspace_import_exceptions;


import at.ac.tuwien.model.change.management.core.exception.DataspaceImportException;

/**
 * Exception thrown when the 'dataspaces' object is missing or invalid in the uploaded file.
 */
public class MissingDataspacesException extends DataspaceImportException {
    public static final String CODE = "DSI_0004_MISSING_DATASPACES";
    public MissingDataspacesException(int line) {
        super(CODE, String.format("Line %d: missing or invalid 'dataspaces' object.", line));
    }
}
