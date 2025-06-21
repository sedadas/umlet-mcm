package at.ac.tuwien.model.change.management.graphdb.exceptions;

public class DataspaceUpdateException extends RuntimeException {
    public DataspaceUpdateException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
