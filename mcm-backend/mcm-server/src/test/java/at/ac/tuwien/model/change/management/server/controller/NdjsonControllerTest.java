package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.core.service.NdjsonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class NdjsonControllerTest {

    @Mock
    private NdjsonService ndjsonService;

    @InjectMocks
    private NdjsonController ndjsonController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(ndjsonController).build();
    }

    @Test
    public void testImportModels_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.ndjson",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"id\":\"1\",\"title\":\"Test Model\"}".getBytes()
        );

        when(ndjsonService.importModels(any(), eq("testConfig"))).thenReturn(new ArrayList<>());

        mockMvc.perform(multipart("/api/ndjson/import")
                .file(file)
                .param("configurationName", "testConfig"))
                .andExpect(status().isOk())
                .andExpect(content().string("Models imported successfully"));
    }

    @Test
    public void testImportModels_EmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.ndjson",
                MediaType.APPLICATION_JSON_VALUE,
                "".getBytes()
        );

        mockMvc.perform(multipart("/api/ndjson/import")
                .file(file)
                .param("configurationName", "testConfig"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please select a file to upload"));
    }

    @Test
    public void testImportModels_InvalidFileExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test".getBytes()
        );

        mockMvc.perform(multipart("/api/ndjson/import")
                .file(file)
                .param("configurationName", "testConfig"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Only NDJSON files are allowed"));
    }

    @Test
    public void testImportModels_ValidationErrors() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.ndjson",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"id\":\"1\",\"title\":\"Test Model\"}".getBytes()
        );

        List<String> errors = new ArrayList<>();
        errors.add("Error parsing line 1: Invalid format");

        when(ndjsonService.importModels(any(), eq("testConfig"))).thenReturn(errors);

        mockMvc.perform(multipart("/api/ndjson/import")
                .file(file)
                .param("configurationName", "testConfig"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0]").value("Error parsing line 1: Invalid format"));
    }

    @Test
    public void testExportModels() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("{\"id\":\"1\",\"title\":\"Test Model\"}".getBytes());

        when(ndjsonService.exportModels("testConfig")).thenReturn(resource);

        mockMvc.perform(get("/api/ndjson/export")
                .param("configurationName", "testConfig"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"testConfig-export.ndjson\""))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testExportAllModels() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("{\"id\":\"1\",\"title\":\"Test Model\"}".getBytes());

        when(ndjsonService.exportAllModels()).thenReturn(resource);

        mockMvc.perform(get("/api/ndjson/export/all"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"all-models-export.ndjson\""))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
