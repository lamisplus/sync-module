package org.lamisplus.modules.sync.domain.mapper;

import org.lamisplus.modules.base.domain.entity.Patient;
import org.lamisplus.modules.sync.domain.dto.PatientDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    PatientDTO toPatientDTO(Patient patient);

    Patient toPatient(PatientDTO patientDTO);
}
