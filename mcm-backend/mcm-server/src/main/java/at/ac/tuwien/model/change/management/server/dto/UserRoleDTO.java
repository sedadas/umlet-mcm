package at.ac.tuwien.model.change.management.server.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UserRoleDTO(
        @NotBlank String name,
        List<String> permissions
) {
}
