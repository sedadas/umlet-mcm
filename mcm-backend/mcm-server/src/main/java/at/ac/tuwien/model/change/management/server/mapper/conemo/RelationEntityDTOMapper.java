package at.ac.tuwien.model.change.management.server.mapper.conemo;

import at.ac.tuwien.model.change.management.graphdb.entities.NodeEntity;
import at.ac.tuwien.model.change.management.graphdb.entities.RelationEntity;
import at.ac.tuwien.model.change.management.server.dto.conemo.NodeEntityDTO;
import at.ac.tuwien.model.change.management.server.dto.conemo.RelationEntityDTO;
import org.mapstruct.Mapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public class RelationEntityDTOMapper {

    private List<NodeEntityDTO> nodeDtos;

    public RelationEntity toEntity(RelationEntityDTO dto) {
        var relationEntity = new RelationEntity();

        //To avoid circular dependencies, Node mapper's toEntity() is copied here instead of called.

        var nodeEntity = new NodeEntity();
        NodeEntityDTO nodeDto =
                nodeDtos.stream().filter(
                        node -> node.id().equals(dto.targetId())
                        ).findFirst().get();

        nodeEntity.setGeneratedID(nodeDto.id()); //TODO: Set ID or not?

        nodeEntity.setName(nodeDto.name());
        nodeEntity.setDescription(nodeDto.description());
        nodeEntity.setType(nodeDto.type());
        nodeEntity.setTags(nodeDto.tags());
        nodeEntity.setUmletProperties(nodeDto.umletProperties());

        relationEntity.setTarget(nodeEntity);

        return relationEntity;
    }

    public Set<RelationEntity> toEntities(Set<RelationEntityDTO> dtos, List<NodeEntityDTO> nodes) {
        nodeDtos = nodes;
        var entities = new HashSet<RelationEntity>();
        for (var dto : dtos) {
            entities.add(toEntity(dto));
        }
        return entities;
    }

}
