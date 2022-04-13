package org.lamisplus.modules.sync.domain.mapper;

import org.lamisplus.modules.base.domain.dto.FormDataDTO;
import org.lamisplus.modules.base.domain.entity.Encounter;
import org.lamisplus.modules.base.domain.entity.FormData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface FormDataMapper {
    @Mappings({
            @Mapping(source="formData.id", target="id"),
            @Mapping(source="encounter.uuid", target="encounterUuid"),
            @Mapping(source="formData.uuid", target="uuid"),
            @Mapping(source="formData.organisationUnitId", target="organisationUnitId")
    })
    FormDataDTO toFormDataDTO(FormData formData, Encounter encounter);

    FormData toFormData(FormDataDTO formDataDTO);

    FormDataDTO toFormDataDTO(FormData formData);
}
