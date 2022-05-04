package org.lamisplus.modules.sync.domain.mapper;

import org.lamisplus.modules.sync.domain.dto.OrganisationUnitLevelDTO;
import org.lamisplus.modules.sync.domain.entity.OrganisationUnitLevel;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrganisationUnitLevelMapper {
    OrganisationUnitLevelDTO toOrganisationUnitLevelDTO(OrganisationUnitLevel organisationUnitLevel);

    OrganisationUnitLevel toOrganisationUnitLevel(OrganisationUnitLevelDTO organisationUnitLevelDTO);

    List<OrganisationUnitLevelDTO> toOrganisationUnitLevelDTOList(List<OrganisationUnitLevel> organisationUnitLevels);
}
