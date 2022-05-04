package org.lamisplus.modules.sync.controller;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.sync.domain.entity.Permission;
import org.lamisplus.modules.sync.repository.PermissionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionRepository permissionRepository;

    @GetMapping
    //@PreAuthorize("hasAuthority('user_read')")
    public ResponseEntity<List<Permission>> getAll() {
        return ResponseEntity.ok(this.permissionRepository.findAllByArchived(0));
    }


    @PostMapping
    //@PreAuthorize("hasAuthority('user_write')")
    public ResponseEntity<List<Permission>> save(@RequestBody List<Permission> permissions) {
        return ResponseEntity.ok(this.permissionRepository.saveAll(permissions));
    }
}
