package org.lamisplus.modules.sync.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormDataDTO {

    private String uuid;

    private String encounterUuid;

    private Object data;


    private Long organisationUnitId;

}
