package at.ac.tuwien.model.change.management.server.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

public record QueryDashboardDTO(
        @NotEmpty String name,
        @NotEmpty Map<String, String> query
) {
}
