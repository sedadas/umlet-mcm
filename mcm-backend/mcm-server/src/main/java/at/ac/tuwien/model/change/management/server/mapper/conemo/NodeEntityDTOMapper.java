package at.ac.tuwien.model.change.management.server.mapper.conemo;

import at.ac.tuwien.model.change.management.graphdb.entities.NodeEntity;
import at.ac.tuwien.model.change.management.graphdb.entities.RelationEntity;
import at.ac.tuwien.model.change.management.server.dto.conemo.NodeEntityDTO;
import org.mapstruct.Mapper;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.value.StringValue;

import java.util.*;

@Mapper(componentModel = "spring")
public class NodeEntityDTOMapper {

    //The list of all nodes.
    //This will be passed to the relationEntityMapper to find nodes by their ids.
    private List<NodeEntityDTO> nodeDtos;

    public NodeEntity toEntity(NodeEntityDTO dto) {

        var entity = toEntityNoRelations(dto);

        Set<RelationEntity> relationEntities = new HashSet<>();
        for (var dtoRelation : dto.relations()) {
            var relation = new RelationEntity();
            var targetDto = nodeDtos.stream().filter(
        node -> node.id().equals(dtoRelation.targetId())
            ).findFirst();

            if (targetDto.isEmpty())
                continue;

            relation.setTarget(toEntityNoRelations(targetDto.get()));

            //CONEMO requires an empty properties name to identify this as an Asset/Condition Relationship object.
            relation.setProperties(Map.of("name", new StringValue("")));
            relationEntities.add(relation);
        }
        entity.setRelations(relationEntities);

        return entity;
    }

    private NodeEntity toEntityNoRelations(NodeEntityDTO dto) {
        //To avoid endless recursion, only convert the static data.
        var entity = new NodeEntity();

        entity.setGeneratedID(dto.id()); //TODO: Set ID or not?
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setType(dto.type());
        entity.setTags(dto.tags());
        entity.setUmletProperties(dto.umletProperties());

        return entity;
    }

    public List<NodeEntity> toEntities(List<NodeEntityDTO> dtos) {
        nodeDtos = dtos;
        var entities = new ArrayList<NodeEntity>();
        for (var dto : dtos) {
            entities.add(toEntity(dto));
        }
        return entities;
    }
}
