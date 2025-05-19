package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.core.service.NdjsonService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for importing and exporting models in NDJSON format.
 */
@RestController
@RequestMapping("/api/ndjson")
@RequiredArgsConstructor
public class NdjsonController {

    private final NdjsonService ndjsonService;

    /**
     * Imports models from an NDJSON file into a configuration.
     *
     * @param file The NDJSON file to import
     * @param configurationName The name of the configuration to import into
     * @return A response entity with validation errors or success message
     */
    @PostMapping("/import")
    public ResponseEntity<?> importModels(
            @RequestParam("file") MultipartFile file,
            @RequestParam("configurationName") String configurationName) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload");
        }

        if (!file.getOriginalFilename().endsWith(".ndjson")) {
            return ResponseEntity.badRequest().body("Only NDJSON files are allowed");
        }

        List<String> validationErrors = ndjsonService.importModels(file, configurationName);

        if (!validationErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(validationErrors);
        }

        return ResponseEntity.ok("Models imported successfully");
    }

    /**
     * Exports models from a configuration to an NDJSON file.
     *
     * @param configurationName The name of the configuration to export from
     * @return The NDJSON file
     */
    @GetMapping("/export")
    public ResponseEntity<Resource> exportModels(@RequestParam("configurationName") String configurationName) {
        Resource resource = ndjsonService.exportModels(configurationName);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Content-Disposition", "attachment; filename=\"" + configurationName + "-export.ndjson\"")
                .body(resource);
    }

    /**
     * Exports models from all configurations to an NDJSON file.
     *
     * @return The NDJSON file
     */
    @GetMapping("/export/all")
    public ResponseEntity<Resource> exportAllModels() {
        Resource resource = ndjsonService.exportAllModels();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Content-Disposition", "attachment; filename=\"all-models-export.ndjson\"")
                .body(resource);
    }
}
