package at.ac.tuwien.model.change.management.core.exception.dataspace_import_exceptions;


import at.ac.tuwien.model.change.management.core.exception.DataspaceImportException;

/**
 * Exception thrown when no existing resource is found to overwrite during a dataspace import.
 */
public class NoTargetFoundException extends DataspaceImportException {
    public static final String CODE = "DSI_0008_NO_TARGET_FOUND";
    public NoTargetFoundException() {
        super(CODE, "No existing resource found to overwrite.");
    }
}
