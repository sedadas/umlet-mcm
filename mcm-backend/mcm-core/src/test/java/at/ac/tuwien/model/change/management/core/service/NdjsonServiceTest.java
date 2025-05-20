package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class NdjsonServiceTest {

    @Mock
    private ConfigurationService configurationService;

    @InjectMocks
    private NdjsonService ndjsonService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        ObjectMapper objectMapper = new ObjectMapper();
        ndjsonService = new NdjsonService(objectMapper, configurationService);
    }

    @Test
    public void testImportModels_ValidNdjson() {
        // Valid NDJSON with correct structure
        String validNdjson =
                "{\"timestamp\":\"2025-05-20T14:23:45Z\",\"datasources\":[{\"name\":\"source1\",\"value\":\"value1\"}]}\n" +
                "{\"timestamp\":\"2025-05-21T10:30:00Z\",\"datasources\":[{\"name\":\"source2\",\"value\":123}]}";

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.ndjson",
                "application/json",
                validNdjson.getBytes(StandardCharsets.UTF_8)
        );

        // Mock configuration service
        Configuration configuration = new Configuration();
        when(configurationService.getConfigurationByName(anyString())).thenReturn(configuration);

        // Test
        List<String> errors = ndjsonService.importModels(file, "testConfig");

        // Verify
        assertTrue(errors.isEmpty(), "Valid NDJSON should not produce validation errors");
    }

    @Test
    public void testImportModels_InvalidJson() {
        // Invalid JSON
        String invalidJson =
                "{\"timestamp\":\"2025-05-20T14:23:45Z\",\"datasources\":[{\"name\":\"source1\",\"value\":\"value1\"}]}\n" +
                "{\"timestamp\":\"2025-05-21T10:30:00Z\",\"datasources\":[{\"name\":\"source2\",\"value\":123"; // Missing closing brackets

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.ndjson",
                "application/json",
                invalidJson.getBytes(StandardCharsets.UTF_8)
        );

        // Test
        List<String> errors = ndjsonService.importModels(file, "testConfig");

        // Verify
        assertFalse(errors.isEmpty(), "Invalid JSON should produce validation errors");
        assertTrue(errors.getFirst().contains("Invalid NDJSON entry format"), "Error should mention invalid format");
    }

    @Test
    public void testImportModels_MissingTimestamp() {
        // Missing timestamp field
        String missingTimestamp =
                "{\"datasources\":[{\"name\":\"source1\",\"value\":\"value1\"}]}";

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.ndjson",
                "application/json",
                missingTimestamp.getBytes(StandardCharsets.UTF_8)
        );

        // Test
        List<String> errors = ndjsonService.importModels(file, "testConfig");

        // Verify
        assertFalse(errors.isEmpty(), "Missing timestamp should produce validation errors");
        assertTrue(errors.getFirst().contains("Invalid NDJSON entry format"), "Error should mention invalid format");
    }

    @Test
    public void testImportModels_InvalidTimestampFormat() {
        // Invalid timestamp format
        String invalidTimestamp =
                "{\"timestamp\":\"2025-05-20\",\"datasources\":[{\"name\":\"source1\",\"value\":\"value1\"}]}";

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.ndjson",
                "application/json",
                invalidTimestamp.getBytes(StandardCharsets.UTF_8)
        );

        // Test
        List<String> errors = ndjsonService.importModels(file, "testConfig");

        // Verify
        assertFalse(errors.isEmpty(), "Invalid timestamp format should produce validation errors");
        assertTrue(errors.getFirst().contains("Invalid timestamp format"), "Error should mention invalid timestamp format");
    }

    @Test
    public void testImportModels_MissingDatasources() {
        // Missing datasources field
        String missingDatasources =
                "{\"timestamp\":\"2025-05-20T14:23:45Z\"}";

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.ndjson",
                "application/json",
                missingDatasources.getBytes(StandardCharsets.UTF_8)
        );

        // Test
        List<String> errors = ndjsonService.importModels(file, "testConfig");

        // Verify
        assertFalse(errors.isEmpty(), "Missing datasources should produce validation errors");
        assertTrue(errors.getFirst().contains("Invalid NDJSON entry format"), "Error should mention invalid format");
    }

    @Test
    public void testImportModels_EmptyDatasources() {
        // Empty datasources array
        String emptyDatasources =
                "{\"timestamp\":\"2025-05-20T14:23:45Z\",\"datasources\":[]}";

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.ndjson",
                "application/json",
                emptyDatasources.getBytes(StandardCharsets.UTF_8)
        );

        // Test
        List<String> errors = ndjsonService.importModels(file, "testConfig");

        // Verify
        assertFalse(errors.isEmpty(), "Empty datasources array should produce validation errors");
        assertTrue(errors.getFirst().contains("Datasources array is empty"), "Error should mention empty datasources");
    }

    @Test
    public void testImportModels_MissingDatasourceName() {
        // Datasource missing name
        String missingName =
                "{\"timestamp\":\"2025-05-20T14:23:45Z\",\"datasources\":[{\"value\":\"value1\"}]}";

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.ndjson",
                "application/json",
                missingName.getBytes(StandardCharsets.UTF_8)
        );

        // Test
        List<String> errors = ndjsonService.importModels(file, "testConfig");

        // Verify
        assertFalse(errors.isEmpty(), "Missing datasource name should produce validation errors");
        assertTrue(errors.getFirst().contains("Invalid NDJSON entry format"), "Error should mention invalid format");
    }

    @Test
    public void testImportModels_EmptyDatasourceName() {
        // Datasource with empty name
        String emptyName =
                "{\"timestamp\":\"2025-05-20T14:23:45Z\",\"datasources\":[{\"name\":\"\",\"value\":\"value1\"}]}";

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.ndjson",
                "application/json",
                emptyName.getBytes(StandardCharsets.UTF_8)
        );

        // Test
        List<String> errors = ndjsonService.importModels(file, "testConfig");

        // Verify
        assertFalse(errors.isEmpty(), "Empty datasource name should produce validation errors");
        assertTrue(errors.getFirst().contains("missing or empty name"), "Error should mention empty name");
    }

    @Test
    public void testImportModels_MultipleValidEntries() {
        // Multiple valid entries
        String multipleEntries =
                """
                        {"timestamp":"2025-05-20T14:23:45Z","datasources":[{"name":"source1","value":"value1"}]}
                        {"timestamp":"2025-05-21T10:30:00Z","datasources":[{"name":"source2","value":123}]}
                        {"timestamp":"2025-05-22T08:15:30Z","datasources":[{"name":"source3","value":true}]}""";

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.ndjson",
                "application/json",
                multipleEntries.getBytes(StandardCharsets.UTF_8)
        );

        // Mock configuration service
        Configuration configuration = new Configuration();
        when(configurationService.getConfigurationByName(anyString())).thenReturn(configuration);

        // Test
        List<String> errors = ndjsonService.importModels(file, "testConfig");

        // Verify
        assertTrue(errors.isEmpty(), "Multiple valid entries should not produce validation errors");
    }

    @Test
    public void testImportModels_MixedValidAndInvalidEntries() {
        // Mix of valid and invalid entries
        String mixedEntries =
                """
                        {"timestamp":"2025-05-20T14:23:45Z","datasources":[{"name":"source1","value":"value1"}]}
                        {"timestamp":"invalid-date","datasources":[{"name":"source2","value":123}]}
                        {"timestamp":"2025-05-22T08:15:30Z","datasources":[{"name":"source3","value":true}]}""";

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.ndjson",
                "application/json",
                mixedEntries.getBytes(StandardCharsets.UTF_8)
        );

        // Test
        List<String> errors = ndjsonService.importModels(file, "testConfig");

        // Verify
        assertFalse(errors.isEmpty(), "Mixed valid and invalid entries should produce validation errors");
        assertEquals(1, errors.size(), "Should have one error for the invalid entry");
        assertTrue(errors.getFirst().contains("Invalid timestamp format"), "Error should mention invalid timestamp format");
    }
}
