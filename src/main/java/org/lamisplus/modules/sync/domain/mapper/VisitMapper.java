package org.lamisplus.modules.sync.domain.mapper;


import org.lamisplus.modules.base.domain.dto.VisitDTO;
import org.lamisplus.modules.base.domain.entity.Patient;
import org.lamisplus.modules.base.domain.entity.Visit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface VisitMapper {
    VisitDTO toVisitDTO(Visit visit);

    @Mappings({
            @Mapping(source="visit.id", target="id"),
            @Mapping(source="patient.id", target="patientId"),
            @Mapping(source="patient.uuid", target="patientUuid"),
            @Mapping(source="visit.uuid", target="uuid"),
            @Mapping(source="visit.organisationUnitId", target="organisationUnitId"),
            @Mapping(source="visit.createdBy", target="createdBy"),
            @Mapping(source="visit.dateCreated", target="dateCreated"),
            @Mapping(source="visit.modifiedBy", target="modifiedBy"),
            @Mapping(source="visit.dateModified", target="dateModified"),
            @Mapping(source="visit.archived", target="archived")
    })
    VisitDTO toVisitDTO(Visit visit, Patient patient);

    Visit toVisit(VisitDTO visitDTO);
}
