package org.lamisplus.modules.sync.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data

public class OrganisationUnitDTO {

    private Long id;

    @NotBlank(message = "name is mandatory")
    private String name;

    private String description;

    @NotNull(message = "organisationUnitLevelId is mandatory")
    private Long organisationUnitLevelId;

    private Long parentOrganisationUnitId;

    private String parentOrganisationUnitName;

    private String parentParentOrganisationUnitName;

    private Object details;

}
