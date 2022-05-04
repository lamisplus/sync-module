package org.lamisplus.modules.sync.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class ModuleMenuDTO {

    private Long moduleId;

    private String moduleName;

    List<MenuDTO> menuDTOS;
}
