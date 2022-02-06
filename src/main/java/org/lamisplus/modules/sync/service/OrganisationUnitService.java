package org.lamisplus.modules.sync.service;

import lombok.AllArgsConstructor;
import org.lamisplus.modules.base.domain.entity.OrganisationUnit;
import org.lamisplus.modules.base.repository.OrganisationUnitRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class OrganisationUnitService {
    private final OrganisationUnitRepository organisationUnitRepository;

    public List<OrganisationUnit> findOrganisationUnitWithRecords() {
        return organisationUnitRepository.findOrganisationUnitWithRecords();
    }

}
