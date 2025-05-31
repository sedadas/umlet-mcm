package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.graphdb.dao.NodeEntityDAO;
import at.ac.tuwien.model.change.management.graphdb.entities.NodeEntity;
import at.ac.tuwien.model.change.management.server.dto.conemo.NodeEntityDTOList;
import at.ac.tuwien.model.change.management.server.mapper.conemo.NodeEntityDTOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static at.ac.tuwien.model.change.management.server.controller.Constants.STRESS_TEST_ENDPOINT;

@Slf4j
@RestController
@RequestMapping(STRESS_TEST_ENDPOINT)
@RequiredArgsConstructor
public class StressTestController {

    private final NodeEntityDAO nodeEntityDAO;
    private final NodeEntityDTOMapper conemoMapper;

    private List<NodeEntity> saved = new ArrayList<>();

    @PostMapping
    @Transactional
    public ResponseEntity<Void> loadNodes(@RequestBody NodeEntityDTOList dto) {
        //Convert CONEMO nodes to MCM nodes and store them directly using the repository!
        log.info(Arrays.toString(dto.nodes().toArray()));
        saved = nodeEntityDAO.saveAll(conemoMapper.toEntities(dto.nodes()));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> revertChanges() {
        //Revert changes.
        nodeEntityDAO.deleteAll(saved);
        return ResponseEntity.ok().build();
    }
}
