package at.ac.tuwien.model.change.management.core.exception.dataspace_import_exceptions;


import at.ac.tuwien.model.change.management.core.exception.DataspaceImportException;
import lombok.extern.slf4j.Slf4j;

/**
 * Exception thrown when a dataspace entry is missing a 'name' or 'value'.
 */
@Slf4j
public class MissingNameValueException extends DataspaceImportException {
    public static final String CODE = "DSI_0005_NAME_VALUE";
    public MissingNameValueException(int line, String key) {
        super(CODE, String.format("Line %d: dataspaces['%s'] missing 'name' or 'value'.", line, key));
        log.error(CODE + ": dataspaces['" + key + "'] missing name or 'value'.");
    }
}
