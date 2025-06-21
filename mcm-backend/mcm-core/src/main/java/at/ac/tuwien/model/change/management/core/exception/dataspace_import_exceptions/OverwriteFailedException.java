package at.ac.tuwien.model.change.management.core.exception.dataspace_import_exceptions;

import at.ac.tuwien.model.change.management.core.exception.DataspaceImportException;

/**
 * Exception thrown when an overwrite operation fails.
 */
public class OverwriteFailedException extends DataspaceImportException {
    public static final String CODE = "DSI_0007_OVERWRITE_FAILED";
    public OverwriteFailedException(String detail, Throwable cause) {
        super(CODE, "Overwrite failed: " + detail, cause);
    }
}
