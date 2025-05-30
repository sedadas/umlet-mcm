package at.ac.tuwien.model.change.management.server.mapper.conemo;

import at.ac.tuwien.model.change.management.graphdb.entities.NodeEntity;
import at.ac.tuwien.model.change.management.server.dto.conemo.NodeEntityDTO;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", uses = {RelationEntityDTOMapper.class})
public class NodeEntityDTOMapper {

    @Autowired
    private RelationEntityDTOMapper relationEntityDTOMapper;

    //The list of all nodes.
    //This will be passed to the relationEntityMapper to find nodes by their ids.
    private List<NodeEntityDTO> nodeDtos;

    public NodeEntity toEntity(NodeEntityDTO dto) {
        var entity = new NodeEntity();

        entity.setGeneratedID(dto.id()); //TODO: Set ID or not?

        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setType(dto.type());
        entity.setTags(dto.tags());
        entity.setUmletProperties(dto.umletProperties());

        entity.setRelations(relationEntityDTOMapper.toEntities(dto.relations(), nodeDtos));

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
