package org.lamisplus.modules.sync.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class OrganisationUnitLevelDTO {

    private Long id;

    @NotBlank(message = "name is mandatory")
    private String name;

    private String description;

    @NotNull(message = "status is mandatory")
    private Integer status;

    @NotNull(message = "parentOrganisationUnitLevelId is mandatory")
    private Long parentOrganisationUnitLevelId;
}
