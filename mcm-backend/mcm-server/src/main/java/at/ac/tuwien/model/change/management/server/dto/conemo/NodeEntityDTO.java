package at.ac.tuwien.model.change.management.server.dto.conemo;

import java.util.Map;
import java.util.Set;

//Sent from CONEMO for stress tests.
public record NodeEntityDTO(
        String id,
        String name,
        String description,
        String type,
        Set<RelationEntityDTO> relations,
        Map<String,String> umletProperties,
        Set<String> tags
) {
}
