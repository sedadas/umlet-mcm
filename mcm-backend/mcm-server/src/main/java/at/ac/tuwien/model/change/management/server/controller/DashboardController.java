package at.ac.tuwien.model.change.management.server.controller;

import at.ac.tuwien.model.change.management.core.service.DashboardService;
import at.ac.tuwien.model.change.management.server.dto.DashboardDTO;
import at.ac.tuwien.model.change.management.server.mapper.DashboardDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final DashboardDtoMapper dashboardDtoMapper;

    @PostMapping("/new")
    ResponseEntity<DashboardDTO> createDashboard(@RequestBody DashboardDTO dto) {
        //TODO!
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{id}")
    ResponseEntity<DashboardDTO> getDashboard(@PathVariable Long id) {
        //TODO!
        return ResponseEntity.noContent().build();
    }

    @GetMapping()
    ResponseEntity<List<DashboardDTO>> getDashboardsForRoles(@RequestBody List<String> roles) {
        //TODO!
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("{id}")
    ResponseEntity<Void> deleteDashboard(@PathVariable Long id) {
        //TODO!
        return ResponseEntity.noContent().build();
    }
}
