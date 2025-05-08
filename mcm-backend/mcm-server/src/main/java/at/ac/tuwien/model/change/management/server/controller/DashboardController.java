package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.core.service.DashboardService;
import at.ac.tuwien.model.change.management.server.dto.DashboardDTO;
import at.ac.tuwien.model.change.management.server.dto.UserRoleDTO;
import at.ac.tuwien.model.change.management.server.mapper.DashboardDtoMapper;
import at.ac.tuwien.model.change.management.server.mapper.UserRoleDtoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    /**
     * Create a new dashboard
     * @param dto dashboard to create
     * @return code 201 Created and the dashboard if successful
     *         code 400 Bad Request if validation failed
     */
    @PostMapping("/new")
    ResponseEntity<DashboardDTO> createDashboard(@RequestBody @NotNull DashboardDTO dto) {
        var persistedEntity = service.createDashboard(dashboardMapper.fromDto(dto));
        var persistedDTO = dashboardMapper.toDto(persistedEntity);
        return ResponseEntity.created(URI.create("/api/v1/dashboard/" + persistedDTO.id())).body(persistedDTO);
    }

    /**
     * Get a dashboard by its id
     * @param id id to search for
     * @return code 200 Ok and the dashboard if found
     *         code 404 Not Found if no such dashboard has been found
     */
    @GetMapping("{id}")
    ResponseEntity<DashboardDTO> getDashboard(@PathVariable @NotBlank String id) {
        return ResponseEntity.ok(dashboardMapper.toDto(service.getDashboard(id)));
    }

    /**
     * Get all dashboards that are allowed (visible) for a list of roles
     * @param roles the list of roles to search for
     * @return code 200 Ok and the (possibly empty) list of dashboards
     */
    @GetMapping()
    ResponseEntity<List<DashboardDTO>> getDashboardsForRoles(@RequestBody @NotEmpty List<UserRoleDTO> roles) {
        var dashboards = service.getDashboardsForRoles(userRoleMapper.fromDTOs(roles));
        return ResponseEntity.ok(dashboardMapper.toDTOs(dashboards));
    }

    /**
     * Delete an existing dashboard
     * @param id dashboard to delete
     * @return code 204 No Content if successful
     *         code 404 Not Found if no such dashboard has been found
     */
    @DeleteMapping("{id}")
    ResponseEntity<Void> deleteDashboard(@PathVariable @NotBlank String id) {
        service.deleteDashboard(id);
        return ResponseEntity.noContent().build();
    }
}
