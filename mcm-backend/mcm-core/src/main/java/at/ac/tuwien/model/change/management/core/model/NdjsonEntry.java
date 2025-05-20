package at.ac.tuwien.model.change.management.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents an entry in an NDJSON file for validation purposes.
 * Each entry must have a timestamp and a list of datasources.
 */
@Getter
@Setter
public class NdjsonEntry {

    /**
     * The timestamp of the entry in ISO 8601 format.
     * Example: "2025-05-20T14:23:45Z"
     */
    @JsonProperty(required = true)
    private String timestamp;

    /**
     * The list of datasources for this entry.
     */
    @JsonProperty(required = true)
    private List<Datasource> datasources;

    /**
     * Represents a datasource in an NDJSON entry.
     */
    @Getter
    @Setter
    public static class Datasource {
        /**
         * The name of the datasource.
         */
        @JsonProperty(required = true)
        private String name;

        /**
         * The value of the datasource.
         * Can be any type (string, number, etc.)
         */
        @JsonProperty(required = true)
        private Object value;
    }
}
