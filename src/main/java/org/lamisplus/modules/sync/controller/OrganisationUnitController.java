package org.lamisplus.modules.sync.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.sync.domain.dto.OrganisationUnitDTO;
import org.lamisplus.modules.sync.domain.entity.OrganisationUnit;
import org.lamisplus.modules.sync.service.OrganisationUnitService;
import org.lamisplus.modules.sync.util.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/organisation-units")
@Slf4j
@RequiredArgsConstructor
public class OrganisationUnitController {

    private final OrganisationUnitService organisationUnitService;

    @PostMapping("/v2")
    @PreAuthorize("hasAnyAuthority('Super Admin','Facility Admin', 'Admin', 'Data Clerk', 'DEC', 'M&E Officer')")
    public ResponseEntity<List<OrganisationUnit>> save(@RequestParam Long parentOrganisationUnitId, @RequestParam Long organisationUnitLevelId,
                                                       @Valid @RequestBody List<OrganisationUnitDTO> organisationUnitDTOS) {
        return ResponseEntity.ok(organisationUnitService.save(parentOrganisationUnitId, organisationUnitLevelId, organisationUnitDTOS));
    }

    @PutMapping("/v2/{id}")
    @PreAuthorize("hasAnyAuthority('Super Admin','Facility Admin', 'Admin', 'Data Clerk', 'DEC', 'M&E Officer')")
    public ResponseEntity<OrganisationUnit> update(@PathVariable Long id, @RequestBody OrganisationUnitDTO organisationUnitDTO) {
        return ResponseEntity.ok(organisationUnitService.update(id, organisationUnitDTO));
    }

    @GetMapping("/v2/{id}")
    @PreAuthorize("hasAnyAuthority('Super Admin','Facility Admin', 'Admin', 'Data Clerk', 'DEC', 'M&E Officer')")
    public ResponseEntity<OrganisationUnit> getOrganizationUnit(@PathVariable Long id) {
        return ResponseEntity.ok(organisationUnitService.getOrganizationUnit(id));
    }

    @GetMapping("/v2")
    public ResponseEntity<List<OrganisationUnit>> getAllOrganizationUnit(@PageableDefault(value = 100) Pageable pageable) {

        Page page = organisationUnitService.getAllOrganizationUnit(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(organisationUnitService.getAllOrganizationUnit(page), headers, HttpStatus.OK);
    }

    @GetMapping ("/parent-organisation-units/{id}")
    public  ResponseEntity<List<OrganisationUnit>>  getOrganisationUnitByParentOrganisationUnitId(@PathVariable Long id) {
        return ResponseEntity.ok(this.organisationUnitService.getOrganisationUnitByParentOrganisationUnitId(id));
    }

    /*@GetMapping ("/parent-organisation-units/{id}/organisation-units-level/{lid}")
    public  ResponseEntity<List<OrganisationUnit>>  getOrganisationUnitByParentOrganisationUnitIdAndOrganisationUnitLevelId(
            @PathVariable Long id, @PathVariable Long lid) {
        return ResponseEntity.ok(this.organisationUnitService.getOrganisationUnitByParentOrganisationUnitIdAndOrganisationUnitLevelId(id, lid));
    }*/

    @GetMapping ("/parent-organisation-units/{id}/organisation-units-level/{lid}/hierarchy")
    public  ResponseEntity<List<OrganisationUnitDTO>>  getOrganisationUnitSubsetByParentOrganisationUnitIdAndOrganisationUnitLevelId(
            @PathVariable Long id, @PathVariable Long lid) {
        return ResponseEntity.ok(this.organisationUnitService.
                getOrganisationUnitSubsetByParentOrganisationUnitIdAndOrganisationUnitLevelId(id, lid));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Integer> delete(@PathVariable Long id) {
        return ResponseEntity.ok(organisationUnitService.delete(id));
    }

    /*//For updating organisation unit from excel
    @GetMapping ("/organisation-unit-levels/test")
    public  ResponseEntity<List>  getAll() {
        return ResponseEntity.ok(this.organisationUnitService.getAll());
    }*/
}
