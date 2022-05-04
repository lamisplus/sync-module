package org.lamisplus.modules.sync.domain.dto;

import lombok.Data;

@Data
public class ModuleDependencyDTO {
    private Long id;
    private String name;
    private Boolean active;
    private String requiredVersion;
    private String installedVersion;
    private Boolean versionSatisfied;
}
