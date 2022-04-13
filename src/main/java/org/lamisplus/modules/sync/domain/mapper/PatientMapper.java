package org.lamisplus.modules.sync.domain.mapper;

import org.lamisplus.modules.base.domain.dto.PatientDTO;
import org.lamisplus.modules.base.domain.entity.Patient;
import org.lamisplus.modules.base.domain.entity.Visit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    Patient toPatient(PatientDTO patientDTO);

    @Mappings({
            @Mapping(source="visit.id", target="visitId"),
            @Mapping(source="patient.id", target="patientId"),
            @Mapping(source="patient.uuid", target="uuid"),
            @Mapping(source="patient.organisationUnitId", target="organisationUnitId")
    })
    PatientDTO toPatientDTO(Visit visit, Patient patient);


    @Mappings({
            @Mapping(source="patient.id", target="patientId"),
            @Mapping(source="patient.organisationUnitId", target="organisationUnitId")
    })
    PatientDTO toPatientDTO(Patient patient);
}
