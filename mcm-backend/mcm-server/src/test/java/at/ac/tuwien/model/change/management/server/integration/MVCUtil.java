package at.ac.tuwien.model.change.management.server.integration;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@Component
public final class MVCUtil {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    public <T, S> MappingIterator<T> getRequest(S dto, final String ENDPOINT, Class<T> clazz) throws Exception {
        final byte[] body = mockMvc.perform(MockMvcRequestBuilders
                        .get(ENDPOINT)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsByteArray();
        return objectMapper.readerFor(clazz).readValues(body);
    }

    public <T> MappingIterator<T> getRequest(final String ENDPOINT, Class<T> clazz) throws Exception {
        final byte[] body = mockMvc.perform(MockMvcRequestBuilders
                        .get(ENDPOINT)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsByteArray();
        return objectMapper.readerFor(clazz).readValues(body);
    }

    public <T, S> MappingIterator<T> postRequest(S dto, final String ENDPOINT, Class<T> clazz) throws Exception {
        final byte[] body = mockMvc.perform(MockMvcRequestBuilders
                        .post(ENDPOINT)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsByteArray();
        return objectMapper.readerFor(clazz).readValues(body);
    }

    public <T, S> MappingIterator<T> putRequest(S dto, final String ENDPOINT, Class<T> clazz) throws Exception {
        final byte[] body = mockMvc.perform(MockMvcRequestBuilders
                        .put(ENDPOINT)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsByteArray();
        return objectMapper.readerFor(clazz).readValues(body);
    }

    public <T> MockHttpServletResponse postRequestResponse(T dto, final String ENDPOINT) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                        .post(ENDPOINT)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }

    public MockHttpServletResponse deleteRequest(final String ENDPOINT) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
                        .delete(ENDPOINT)
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
    }
}
