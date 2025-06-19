package at.ac.tuwien.model.change.management.core.exception.dataspace_import_exceptions;


import at.ac.tuwien.model.change.management.core.exception.DataspaceImportException;

/**
 * Exception thrown when the uploaded file is empty or invalid.
 */
public class InvalidFileException extends DataspaceImportException {
    public static final String CODE = "DSI_0001_INVALID_FILE";
    public InvalidFileException() {
        super(CODE, "Uploaded file is empty or invalid.");
    }
}
