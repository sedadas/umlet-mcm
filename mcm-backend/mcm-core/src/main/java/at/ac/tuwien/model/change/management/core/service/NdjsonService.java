package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for importing and exporting models in NDJSON format.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NdjsonService {

    private final ObjectMapper objectMapper;
    private final ConfigurationService configurationService;

    /**
     * Imports models from an NDJSON file into a configuration.
     *
     * @param file The NDJSON file to import
     * @param configurationName The name of the configuration to import into
     * @return A list of validation errors, empty if the import was successful
     */
    public List<String> importModels(MultipartFile file, String configurationName) {
        List<String> validationErrors = new ArrayList<>();
        Set<Model> models = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }

                try {
                    Model model = objectMapper.readValue(line, Model.class);
                    models.add(model);
                } catch (Exception e) {
                    validationErrors.add("Error parsing line " + lineNumber + ": " + e.getMessage());
                }
            }

            // If there are no validation errors, save the models to the configuration
            if (validationErrors.isEmpty() && !models.isEmpty()) {
                try {
                    // Get the configuration or create a new one if it doesn't exist
                    Configuration configuration;
                    try {
                        configuration = configurationService.getConfigurationByName(configurationName);
                    } catch (Exception e) {
                        // Configuration doesn't exist, create a new one
                        configuration = new Configuration();
                        configuration.setName(configurationName);
                    }

                    // Add the models to the configuration
                    configuration.getModels().addAll(models);

                    // Save the configuration
                    if (configuration.getVersion() == null) {
                        configurationService.createConfiguration(configuration);
                    } else {
                        configurationService.updateConfiguration(configuration);
                    }
                } catch (Exception e) {
                    validationErrors.add("Error saving configuration: " + e.getMessage());
                }
            } else if (models.isEmpty() && validationErrors.isEmpty()) {
                validationErrors.add("No valid models found in the file");
            }

        } catch (IOException e) {
            validationErrors.add("Error reading file: " + e.getMessage());
        }

        return validationErrors;
    }

    /**
     * Exports models from a configuration to an NDJSON file.
     *
     * @param configurationName The name of the configuration to export from
     * @return The NDJSON file as a ByteArrayResource
     */
    public ByteArrayResource exportModels(String configurationName) {
        try {
            Configuration configuration = configurationService.getConfigurationByName(configurationName);
            Set<Model> models = configuration.getModels();

            if (models.isEmpty()) {
                return new ByteArrayResource("".getBytes(StandardCharsets.UTF_8));
            }

            StringBuilder ndjson = new StringBuilder();

            for (Model model : models) {
                String json = objectMapper.writeValueAsString(model);
                ndjson.append(json).append("\n");
            }

            return new ByteArrayResource(ndjson.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Error exporting models to NDJSON", e);
            return new ByteArrayResource("".getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Exports models from all configurations to an NDJSON file.
     *
     * @return The NDJSON file as a ByteArrayResource
     */
    public ByteArrayResource exportAllModels() {
        try {
            List<Configuration> configurations = configurationService.getAllConfigurations();
            Set<Model> allModels = new HashSet<>();

            for (Configuration configuration : configurations) {
                allModels.addAll(configuration.getModels());
            }

            if (allModels.isEmpty()) {
                return new ByteArrayResource("".getBytes(StandardCharsets.UTF_8));
            }

            StringBuilder ndjson = new StringBuilder();

            for (Model model : allModels) {
                String json = objectMapper.writeValueAsString(model);
                ndjson.append(json).append("\n");
            }

            return new ByteArrayResource(ndjson.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Error exporting all models to NDJSON", e);
            return new ByteArrayResource("".getBytes(StandardCharsets.UTF_8));
        }
    }
}
