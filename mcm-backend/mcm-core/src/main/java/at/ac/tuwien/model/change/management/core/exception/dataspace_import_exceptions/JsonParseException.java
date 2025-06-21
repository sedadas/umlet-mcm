package at.ac.tuwien.model.change.management.core.exception.dataspace_import_exceptions;


import at.ac.tuwien.model.change.management.core.exception.DataspaceImportException;
import lombok.extern.slf4j.Slf4j;

/**
 * Exception thrown when the JSON data is not valid.
 */
@Slf4j
public class JsonParseException extends DataspaceImportException {
    public static final String CODE = "DSI_0002_INVALID_JSON";
    public JsonParseException(int line, String detail, Throwable cause) {
        super(CODE, String.format("Line %d: not valid JSON (%s).", line, detail), cause);
        log.error(CODE + ": Line " + line + ": not valid JSON (" + detail + ").", cause);
    }
}
