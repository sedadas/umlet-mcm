// File: mcm-server/src/main/java/at/ac/tuwien/model/change/management/server/controller/DataspaceController.java

package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.core.exception.DataspaceImportException;
import at.ac.tuwien.model.change.management.core.exception.dataspace_import_exceptions.*;
import at.ac.tuwien.model.change.management.core.service.GraphDBService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * REST controller to handle dataspace NDJSON uploads.
 *
 * Endpoint: POST /api/v1/dataspace/import
 *
 * Reads only the first non‐blank line of the uploaded file.
 * That line must be valid JSON with exactly:
 *   • "timestamp"   : String (ISO‐8601)
 *   • "datasources" : Array of { "name": String, "value": Any primitive }
 *
 * For each element in "datasources", we pass name→value to GraphDBService.upsertDataspaceProperties().
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/v1/dataspace")
@RequiredArgsConstructor
public class DataspaceController {

    private final GraphDBService graphDBService;
    private final ObjectMapper     objectMapper;

    @PostMapping(
            path = "/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = "application/json"
    )
    public ResponseEntity<?> importDataspace(
            @RequestPart("file") @NotNull MultipartFile file
    ) {
        // 1) Generate a UUID for this upload
        UUID uuid = UUID.randomUUID();
        log.info("Uploading dataspace file with UUID: {}", uuid);

        // 2) Validate the file
        this.validateFile(file);

        String firstLine = this.readLineAtIndex(file, 0);
        if (firstLine == null) {
            // DSI_0001: file is empty or contains no valid JSON
            throw new InvalidFileException();
        }

        // 3) Parse that line as JSON
        Map<String, Object> parsed = this.parseJson(firstLine);
        // 4) Validate the schema
        this.validateSchema(parsed);

        String timestamp = (String) parsed.get("timestamp");
        @SuppressWarnings("unchecked")
        List<Object> datasources = this.extractDataspaces(parsed);

        // 5) Build a map from name→value
        Map<String, Object> valuesByName = this.buildValuesByName(datasources);

        // 6) Delegate to GraphDBService, which writes into Neo4j
        ResponseEntity<List<Map<String, String>>> response =
                this.upsertDataspaceData(timestamp, valuesByName, uuid);

        if (response.getStatusCode().isError()) {
            log.error("Failed to update Neo4j: {}", response.getBody());
            // DSI_0009: error during Neo4j update
            throw new DataspaceImportException(
                    "DSI_0009",
                    "Failed to update Neo4j: " + response.getBody()
            );
        }

        // 7) Log success and return response
        log.info("Successfully updated Neo4j with {} dataspaces", valuesByName.size());
        return ResponseEntity.ok(Map.of(
                "message", "Dataspace properties updated",
                "count", valuesByName.size()
        ));
    }


    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            // DSI_0001
            throw new InvalidFileException();
        }
        validateFileExtension(file);
        validateFileFormat(file);
    }

    private void validateFileExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".ndjson")) {
            // DSI_0001
            throw new InvalidFileException();
        }
    }

    private void validateFileFormat(MultipartFile file) {
        if (file.isEmpty()) {
            // DSI_0001
            throw new InvalidFileException();
        }
    }


    /**
     * Reads the first non-blank line at the given index from the NDJSON file.
     * Returns null if the index is out of bounds or if EOF is reached.
     *
     * @param file  The NDJSON file to read
     * @param index The line index to read (0-based)
     * @return The line content or null if not found
     */
    private String readLineAtIndex(MultipartFile file, int index) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            for (int i = 0; i <= index; i++) {
                String line = reader.readLine();
                if (line == null) return null; // EOF
                if (i == index) return line.trim(); // Return the requested line
            }
        } catch (Exception e) {
            log.error("Failed to read line at index {}: {}", index, e.getMessage(), e);
            // Wrap any I/O or reader error in our generic dataspace‐import exception
            throw new DataspaceImportException(
                    "DSI_0000",
                    "Failed to read line at index " + index + ": " + e.getMessage(),
                    e
            );
        }
        return null;
    }


    /**
     * Parses the given JSON string into a Map.
     * Throws JsonParseException if parsing fails.
     *
     * @param jsonString The JSON string to parse
     * @return The parsed JSON as a Map
     */
    private Map<String, Object> parseJson(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, Map.class);
        } catch (Exception e) {
            log.error("Failed to parse JSON: {}", e.getMessage());
            // Use our standardized JSON‐parse exception (DSI_0002)
            throw new JsonParseException(
                    0,
                    e.getMessage(),
                    e
            );
        }
    }

    /**
     * Validates the schema of the parsed JSON object.
     * Throws specific exceptions for missing or invalid fields.
     *
     * @param parsed The parsed JSON object as a Map
     */
    private void validateSchema(Map<String, Object> parsed) {
        if (!parsed.containsKey("timestamp") || !(parsed.get("timestamp") instanceof String)) {
            // DSI_0003
            throw new MissingTimestampException(0);
        }
        if (!(parsed.get("datasources") instanceof List)) {
            // DSI_0004
            throw new MissingDataspacesException(0);
        }

        @SuppressWarnings("unchecked")
        List<Object> dsRawList = (List<Object>) parsed.get("datasources");
        if (dsRawList.isEmpty()) {
            // DSI_0004
            throw new MissingDataspacesException(0);
        }

        for (int i = 0; i < dsRawList.size(); i++) {
            Object element = dsRawList.get(i);
            if (!(element instanceof Map)) {
                // DSI_0002
                throw new JsonParseException(
                        i + 1,
                        "Element at dataspaces[" + i + "] is not an object",
                        null
                );
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> dsEntry = (Map<String, Object>) element;

            if (!dsEntry.containsKey("name") || !(dsEntry.get("name") instanceof String)) {
                // DSI_0005
                throw new MissingNameValueException(
                        i + 1,
                        ""  // no key available
                );
            }
            if (!dsEntry.containsKey("value")) {
                // DSI_0005
                String key = (String) dsEntry.get("name");
                throw new MissingNameValueException(
                        i + 1,
                        key
                );
            }
        }
    }


    // extract dataspaces from json object
    private List<Object> extractDataspaces(Map<String, Object> parsed) {
        if (!parsed.containsKey("datasources")) {
            // DSI_0004: missing 'dataspaces'
            throw new MissingDataspacesException(0);
        }

        System.out.println("DataspaceController → parsed = %s".formatted(parsed));
        List<Object> dsRawList = (List<Object>) parsed.get("datasources");
        System.out.println("DataspaceController <UNK> parsed = %s".formatted(dsRawList));
        if (dsRawList.isEmpty()) {
            throw new MissingDataspacesException(0);
        }
        return dsRawList;
    }


    /**
     * Builds a map from data source names to their values.
     * If multiple entries have the same name, the last one wins.
     *
     * @param dsRawList The list of raw data source entries
     * @return A map with names as keys and values as values
     */
    private Map<String, Object> buildValuesByName(List<Object> dsRawList) {
        Map<String, Object> valuesByName = new HashMap<>();
        for (int i = 0; i < dsRawList.size(); i++) {
            Object element = dsRawList.get(i);
            if (!(element instanceof Map)) {
                // DSI_0002: invalid JSON structure for dataspace entry
                throw new JsonParseException(
                        i + 1,
                        "Element at dataspaces[" + i + "] is not an object",
                        null
                );
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> dsEntry = (Map<String, Object>) element;

            if (!dsEntry.containsKey("name") || !(dsEntry.get("name") instanceof String)) {
                // DSI_0005: missing name or value
                throw new JsonParseException(
                        i + 1,
                        "Missing or invalid 'name' in dataspaces[" + i + "]",
                        null
                );
            }
            if (!dsEntry.containsKey("value")) {
                // DSI_0005: missing name or value
                throw new JsonParseException(
                        i + 1,
                        "Missing 'value' in dataspaces[" + i + "]",
                        null
                );
            }

            String dsName = (String) dsEntry.get("name");
            Object dsValue = dsEntry.get("value");
            // Overwrite if duplicate names; last one wins
            valuesByName.put(dsName, dsValue);
        }
        return valuesByName;
    }

    /**
     * Upserts the dataspace data into the Neo4j database.
     * Handles exceptions and returns appropriate HTTP responses.
     *
     * @param timestamp    The ISO-8601 timestamp
     * @param valuesByName Map of data source names to their values
     * @param uuid         The UUID of the upload
     * @return ResponseEntity with the result of the upsert operation
     */
    private ResponseEntity<List<Map<String, String>>> upsertDataspaceData(
            String timestamp,
            Map<String, Object> valuesByName,
            UUID uuid) {

        try {
            graphDBService.upsertDataspaceProperties(timestamp, valuesByName, uuid);
        }
        catch (MissingTimestampException e) {
            log.error("upsertDataspaceData failed – missing timestamp", e);
            return ResponseEntity
                    .badRequest()
                    .body(Collections.singletonList(
                            Map.of(
                                    "errorCode",   e.getErrorCode(),
                                    "message",     e.getMessage()
                            )
                    ));
        }
        catch (OverwriteFailedException e) {
            log.error("upsertDataspaceData failed – Neo4j overwrite error", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(Collections.singletonList(
                            Map.of(
                                    "errorCode",   e.getErrorCode(),
                                    "message",     e.getMessage()
                            )
                    ));
        }
        catch (Exception e) {
            log.error("GraphDBService.upsertDataspaceProperties failed", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList(
                            Map.of(
                                    "error",   "Failed to update Neo4j: " + e.getMessage()
                            )
                    ));
        }

        return ResponseEntity.ok(Collections.singletonList(
                Map.of(
                        "message", "Dataspace properties updated",
                        "count",   String.valueOf(valuesByName.size())
                )
        ));
    }

}
