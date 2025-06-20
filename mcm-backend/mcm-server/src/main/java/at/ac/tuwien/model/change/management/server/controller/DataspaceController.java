// File: mcm-server/src/main/java/at/ac/tuwien/model/change/management/server/controller/DataspaceController.java

package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.core.exception.DataspaceImportException;
import at.ac.tuwien.model.change.management.core.exception.dataspace_import_exceptions.*;
import at.ac.tuwien.model.change.management.core.service.GraphDBService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.IOException;
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

        // 3) Parse that line as JSON
        this.validateWholeFileSchema(file);

        // 5) Extract datasources with timestamp and values
        List<Map.Entry<String, String>> datasources = this.groupDatasourcesByName(file);

        // 6) upsert each datasource value
        datasources.forEach(entry -> {
            String name         = entry.getKey();
            String jsonArrayStr = entry.getValue();           // already a JSON string
            graphDBService.upsertDatasourceValue(name, jsonArrayStr);
        });

        // 7) Log success and return response
        log.info("Successfully imported {} datasources from file with UUID: {}", datasources.size(), uuid);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("uuid", uuid.toString(), "datasources", datasources.size()));
    }

    /**
     * Validates the uploaded NDJSON file.
     * Checks if the file is not null, not empty, has a valid extension,
     * and is in the correct format.
     *
     * @param file the uploaded NDJSON file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            // DSI_0001
            throw new InvalidFileException();
        }
        validateFileExtension(file);
        validateFileFormat(file);
    }

    /**
     * Validates that the file has a valid NDJSON extension.
     * Throws InvalidFileException if the file extension is not .ndjson.
     *
     * @param file the uploaded NDJSON file
     */
    private void validateFileExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".ndjson")) {
            // DSI_0001
            throw new InvalidFileException();
        }
    }

    /**
     * Validates that the file is not empty.
     * Throws InvalidFileException if the file is empty.
     *
     * @param file the uploaded NDJSON file
     */
    private void validateFileFormat(MultipartFile file) {
        if (file.isEmpty()) {
            // DSI_0001
            throw new InvalidFileException();
        }
    }

    /**
     * Reads the uploaded NDJSON file, groups data sources by their names,
     * and returns a list of entries with name and corresponding JSON array.
     *
     * @param file the uploaded NDJSON file
     * @return List of Map.Entry with datasource name and JSON array string
     */
    private List<Map.Entry<String, String>> groupDatasourcesByName(
            @NotNull MultipartFile file) {

        Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isBlank()) continue;

                Map<String, Object> parsed = this.parseJson(line);
                this.validateSchema(parsed);

                String timestamp = (String) parsed.get("timestamp");
                List<Object> datasources = this.extractDataspaces(parsed);

                for (Object obj : datasources) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> ds = (Map<String, Object>) obj;

                    String name  = (String) ds.get("name");
                    Object value = ds.get("value");

                    grouped
                            .computeIfAbsent(name, k -> new ArrayList<>())
                            .add(Map.of("timestamp", timestamp, "value", value));
                }
            }
        } catch (DataspaceImportException die) {
            throw die;
        } catch (Exception e) {
            log.error("Failed to aggregate NDJSON file", e);
            throw new DataspaceImportException(
                    "DSI_0000",
                    "Failed to aggregate NDJSON file: " + e.getMessage(),
                    e
            );
        }

        List<Map.Entry<String, String>> result = new ArrayList<>(grouped.size());
        grouped.forEach((name, list) -> {
            try {
                String jsonArray = objectMapper.writeValueAsString(list);
                result.add(new AbstractMap.SimpleEntry<>(name, jsonArray));
            } catch (Exception e) {
                log.error("JSON serialisation failed for datasource '{}'", name, e);
                throw new DataspaceImportException(
                        "DSI_0000",
                        "Serialisation error for datasource '" + name + "'",
                        e
                );
            }
        });

        return result;
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
     * Reads the uploaded *.ndjson* line-by-line and validates the schema
     * of every record.
     *
     * It re-uses the existing helpers:
     *   • parseJson(line)     – generic JSON → Map
     *   • validateSchema(map) – structural checks / DSI_…. exceptions
     *
     * If any line is malformed, the corresponding DSI_… exception is
     * propagated unchanged, so the rest of your controller keeps its
     * current error handling.
     *
     * @param file the uploaded NDJSON file
     * @throws DataspaceImportException on I/O problems
     * @throws JsonParseException, MissingTimestampException, … on bad data
     */
    private void validateWholeFileSchema(@NotNull MultipartFile file) throws DataspaceImportException {

        // basic checks you already have (extension, empty file, …)
        this.validateFile(file);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isBlank()) continue;

                Map<String, Object> parsed = this.parseJson(line);
                this.validateSchema(parsed);
            }

        } catch (DataspaceImportException die) {
            throw die;
        } catch (Exception e) {
            log.error("Failed while validating entire NDJSON file", e);
            // DSI_0000: any unexpected I/O or processing error
            throw new DataspaceImportException(
                    "DSI_0000",
                    "Failed while validating NDJSON file: " + e.getMessage(),
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
}
