package at.ac.tuwien.model.change.management.core.mapper.neo4j;

import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.graphdb.entities.ModelEntity;
import lombok.AllArgsConstructor;
import lombok.val;
import org.neo4j.driver.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/**
 * The currently used implementation of the ModelEntityMapper
 */
@Component
@AllArgsConstructor
public class ModelEntityMapperImpl implements ModelEntityMapper {

    private NodeEntityMapper nodeMapper;
    @Override
    public ModelEntity toEntity(Model model) {
        if(model == null) {
            return null;
        }

        ModelEntity modelEntity = new ModelEntity();

        // Assign ID
        if(model.getId() != null) {
            modelEntity.setId(model.getId());
        }

        // Assign nodes
        modelEntity.setNodes(model.getNodes().stream().map(node -> nodeMapper.toEntity(node)).collect(Collectors.toSet()));

        // Assign tags
        if(model.getTags() != null)
            modelEntity.setTags( new HashSet<>(model.getTags()));

        // Assign properties
        if(model.getMcmAttributes() != null) {
            val mcmAttributes = new HashMap<String, Value>() {{
                model.getMcmAttributes().forEach((key, value) -> put(key, Neo4jValueConverter.convertObject(value)));
            }};
            modelEntity.setProperties( mcmAttributes );
        }

        // Assign name
        modelEntity.setName(model.getTitle());

        // Assign description
        modelEntity.setDescription(model.getDescription());

        return modelEntity;
    }

    @Override
    public Model fromEntity(ModelEntity modelEntity) {
        if(modelEntity == null) {
            return null;
        }

        Model model = new Model();

        // Assign ID
        if(modelEntity.getId() != null) {
            model.setId( modelEntity.getId() );
        }

        // Assign nodes
        model.setNodes(modelEntity.getNodes().stream().map(nodeEntity -> nodeMapper.fromEntity(nodeEntity)).collect(Collectors.toSet()));

        // Assign tags
        model.setTags( new ArrayList<>(modelEntity.getTags()));

        // Assign properties
        val mcmAttributes  = new LinkedHashMap<String,Object>() {{
            modelEntity.getProperties().forEach((key, value) -> put(key, Neo4jValueConverter.convertValue((Value) value)));
        }};
        model.setMcmAttributes( mcmAttributes );

        // Assign name
        model.setTitle(modelEntity.getName());

        // Assign description
        model.setDescription(modelEntity.getDescription());

        return model;
    }
}
