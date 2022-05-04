package org.lamisplus.modules.sync.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.sync.domain.dto.ApplicationCodesetDTO;
import org.lamisplus.modules.sync.domain.entity.ApplicationCodeSet;
import org.lamisplus.modules.sync.service.ApplicationCodesetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/application-codesets")
@Slf4j
@RequiredArgsConstructor
public class ApplicationCodeSetController {
    private final ApplicationCodesetService applicationCodesetService;

    @GetMapping("/v2/{codesetGroup}")
    public ResponseEntity<List<ApplicationCodesetDTO>> getApplicationCodeByCodeSetGroup(@PathVariable String codesetGroup) {
        return ResponseEntity.ok(this.applicationCodesetService.getApplicationCodeByCodesetGroup(codesetGroup));
    }

    @GetMapping("/v2")
    public ResponseEntity<List<ApplicationCodesetDTO>> getAllApplicationCodesets() {
        return ResponseEntity.ok(this.applicationCodesetService.getAllApplicationCodeset());
    }

    @PostMapping("/v2")
    public ResponseEntity<ApplicationCodeSet> save(@Valid @RequestBody ApplicationCodesetDTO applicationCodesetDTO) {
        return ResponseEntity.ok(applicationCodesetService.save(applicationCodesetDTO));

    }

    @PutMapping("/v2/{id}")
    public ResponseEntity<ApplicationCodeSet> update(@PathVariable Long id, @Valid @RequestBody ApplicationCodesetDTO applicationCodesetDTO) {
        return ResponseEntity.ok(applicationCodesetService.update(id, applicationCodesetDTO));

    }

    @DeleteMapping("/v2/{id}")
    @PreAuthorize("hasAnyAuthority('Super Admin','Admin', 'DEC', 'Data Clerk', 'Facility Admin')")
    public void delete(@PathVariable Long id) {
        this.applicationCodesetService.delete(id);
    }
}
