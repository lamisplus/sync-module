package org.lamisplus.modules.sync.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.sync.domain.dto.OrganisationUnitLevelDTO;
import org.lamisplus.modules.sync.domain.entity.OrganisationUnit;
import org.lamisplus.modules.sync.service.OrganisationUnitLevelService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/organisation-unit-levels")
@Slf4j
@RequiredArgsConstructor
public class OrganisationUnitLevelController {
    private final OrganisationUnitLevelService organisationUnitLevelService;

    @PostMapping("/v2")
    @PreAuthorize("hasAnyAuthority('Super Admin','Facility Admin', 'Admin', 'Data Clerk', 'DEC', 'M&E Officer')")
    public ResponseEntity<OrganisationUnitLevelDTO> save(@Valid @RequestBody OrganisationUnitLevelDTO organisationUnitLevelDTO) {
        return ResponseEntity.ok(organisationUnitLevelService.save(organisationUnitLevelDTO));
    }

    @PutMapping("/v2/{id}")
    @PreAuthorize("hasAnyAuthority('Super Admin','Facility Admin', 'Admin', 'Data Clerk', 'DEC', 'M&E Officer')")
    public ResponseEntity<OrganisationUnitLevelDTO> update(@PathVariable Long id, @Valid @RequestBody OrganisationUnitLevelDTO organisationUnitLevelDTO){
        return ResponseEntity.ok(organisationUnitLevelService.update(id, organisationUnitLevelDTO));
    }

    @GetMapping("/v2")
    @PreAuthorize("hasAnyAuthority('Super Admin','Facility Admin', 'Admin', 'Data Clerk', 'DEC', 'M&E Officer')")
    public ResponseEntity<List<OrganisationUnitLevelDTO>> getAllOrganizationUnitLevel(@RequestParam(required = false, defaultValue = "2") Integer status) {
        return ResponseEntity.ok(organisationUnitLevelService.getAllOrganizationUnitLevel(status));
    }

    @GetMapping("/v2/{id}/organisation-units")
    @PreAuthorize("hasAnyAuthority('Super Admin','Facility Admin', 'Admin', 'Data Clerk', 'DEC', 'M&E Officer')")
    public ResponseEntity<List<OrganisationUnit>> getAllOrganisationUnitsByOrganizationUnitLevel(@PathVariable Long id) {
        return ResponseEntity.ok(organisationUnitLevelService.getAllOrganisationUnitsByOrganizationUnitLevel(id));
    }

    @GetMapping("/v2/parent-organisation-unit-level/{id}/organisation-units")
    @PreAuthorize("hasAnyAuthority('Super Admin','Facility Admin', 'Admin', 'Data Clerk', 'DEC', 'M&E Officer')")
    public ResponseEntity<List<OrganisationUnit>> getAllParentOrganisationUnitsByOrganizationUnitLevel(@PathVariable Long id) {
        return ResponseEntity.ok(organisationUnitLevelService.getAllParentOrganisationUnitsByOrganizationUnitLevel(id));
    }


    @GetMapping("/v2/{id}")
    @PreAuthorize("hasAnyAuthority('Super Admin','Facility Admin', 'Admin', 'Data Clerk', 'DEC', 'M&E Officer')")
    public ResponseEntity<OrganisationUnitLevelDTO> getOrganizationUnitLevel(@PathVariable Long id) {
        return ResponseEntity.ok(organisationUnitLevelService.getOrganizationUnitLevel(id));
    }

    /*@DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable Long id) {
        organisationUnitLevelService.delete(id);
    }*/
}
