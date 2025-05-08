package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.core.service.DashboardService;
import at.ac.tuwien.model.change.management.server.dto.DashboardDTO;
import at.ac.tuwien.model.change.management.server.dto.UserRoleDTO;
import at.ac.tuwien.model.change.management.server.mapper.DashboardDtoMapper;
import at.ac.tuwien.model.change.management.server.mapper.UserRoleDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;
    private final DashboardDtoMapper dashboardMapper;
    private final UserRoleDtoMapper userRoleMapper;

    //TODO: Error handling!
    //TODO: Create validators for handling argument validation!

    @PostMapping("/new")
    ResponseEntity<DashboardDTO> createDashboard(@RequestBody DashboardDTO dto) {
        var persistedEntity = service.createDashboard(dashboardMapper.fromDto(dto));
        var persistedDTO = dashboardMapper.toDto(persistedEntity);
        return ResponseEntity.created(URI.create("/api/v1/dashboard/" + persistedDTO.id())).body(persistedDTO);
    }

    @GetMapping("{id}")
    ResponseEntity<DashboardDTO> getDashboard(@PathVariable String id) {
        return ResponseEntity.ok(dashboardMapper.toDto(service.getDashboard(id)));
    }

    @GetMapping()
    ResponseEntity<List<DashboardDTO>> getDashboardsForRoles(@RequestBody List<UserRoleDTO> roles) {
        var dashboards = service.getDashboardsForRoles(userRoleMapper.fromDTOs(roles));
        return ResponseEntity.ok(dashboardMapper.toDTOs(dashboards));
    }

    @DeleteMapping("{id}")
    ResponseEntity<Void> deleteDashboard(@PathVariable String id) {
        service.deleteDashboard(id);
        return ResponseEntity.noContent().build();
    }
}
