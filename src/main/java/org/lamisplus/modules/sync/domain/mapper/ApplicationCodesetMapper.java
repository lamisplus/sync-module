package org.lamisplus.modules.sync.domain.mapper;


import org.lamisplus.modules.sync.domain.dto.ApplicationCodesetDTO;
import org.lamisplus.modules.sync.domain.entity.ApplicationCodeSet;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ApplicationCodesetMapper {

    ApplicationCodeSet toApplicationCodeset(ApplicationCodesetDTO applicationCodesetDTO);

    ApplicationCodesetDTO toApplicationCodesetDTO(ApplicationCodeSet applicationCodeset);

    List<ApplicationCodesetDTO> toApplicationCodesetDTOList(List<ApplicationCodeSet> applicationCodesets);

    @Named("mapWithoutFields")
    @Mappings({
            @Mapping(target = "language", ignore = true),
            @Mapping(target = "codesetGroup", ignore = true)
    })
    ApplicationCodesetDTO mapWithoutFields(ApplicationCodeSet applicationCodeset);

    @IterableMapping(qualifiedByName="mapWithoutFields")
    List<ApplicationCodesetDTO> toApplicationCodesetDTOListMapWithoutFields(List<ApplicationCodeSet> applicationCodeset);
}
