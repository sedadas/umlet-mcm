// File: mcm-server/src/main/java/at/ac/tuwien/model/change/management/server/controller/DataspaceController.java

package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.core.service.GraphDBService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        // 1) Reject empty uploads
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Uploaded file is empty"));
        }

        // 2) Read only the first non‐blank line
        String firstLine;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            do {
                firstLine = reader.readLine();
                if (firstLine == null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "File contains no JSON objects"));
                }
                firstLine = firstLine.trim();
            } while (firstLine.isEmpty());

        } catch (Exception e) {
            log.error("Failed to read uploaded file", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Server error reading file: " + e.getMessage()));
        }

        // 3) Parse that line as JSON
        Map<String, Object> parsed;
        try {
            // Jackson will convert JSON into a generic Map<String,Object>
            parsed = objectMapper.readValue(firstLine, Map.class);
        } catch (Exception e) {
            log.error("Failed to parse JSON", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "First line is not valid JSON: " + e.getMessage()));
        }

        // 4) Validate presence of "timestamp" (String) and "datasources" (List)
        if (!parsed.containsKey("timestamp") || !(parsed.get("timestamp") instanceof String)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing or invalid 'timestamp' (must be a string)"));
        }
        if (!parsed.containsKey("datasources") || !(parsed.get("datasources") instanceof List)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing or invalid 'datasources' (must be an array)"));
        }

        String timestamp = (String) parsed.get("timestamp");

        @SuppressWarnings("unchecked")
        List<Object> dsRawList = (List<Object>) parsed.get("datasources");
        if (dsRawList.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "'datasources' array must not be empty"));
        }

        // 5) Build a map from name→value
        Map<String, Object> valuesByName = new HashMap<>();
        for (int i = 0; i < dsRawList.size(); i++) {
            Object element = dsRawList.get(i);
            if (!(element instanceof Map)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Element at datasources[" + i + "] is not an object"));
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> dsEntry = (Map<String, Object>) element;

            if (!dsEntry.containsKey("name") || !(dsEntry.get("name") instanceof String)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing or invalid 'name' in datasources[" + i + "]"));
            }
            if (!dsEntry.containsKey("value")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing 'value' in datasources[" + i + "]"));
            }

            String dsName = (String) dsEntry.get("name");
            Object dsValue = dsEntry.get("value");
            // Overwrite if duplicate names; last one wins
            valuesByName.put(dsName, dsValue);
        }

        // 6) Delegate to GraphDBService, which writes into Neo4j
        try {
            graphDBService.upsertDataspaceProperties(timestamp, valuesByName);
        } catch (Exception e) {
            log.error("GraphDBService.upsertDataspaceProperties failed", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to update Neo4j: " + e.getMessage()));
        }

        // 7) All done
        return ResponseEntity.ok(Map.of(
                "message", "Dataspace properties updated",
                "count", valuesByName.size()
        ));
    }
}
