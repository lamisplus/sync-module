package org.lamisplus.modules.sync.domain.mapper;


import org.lamisplus.modules.base.domain.dto.EncounterDTO;
import org.lamisplus.modules.base.domain.entity.Encounter;
import org.lamisplus.modules.base.domain.entity.Form;
import org.lamisplus.modules.base.domain.entity.Patient;
import org.lamisplus.modules.base.domain.entity.Visit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface EncounterMapper {
    @Mappings({
            @Mapping(source="visit.id", target="visitId"),
            @Mapping(source="patient.id", target="patientId"),
            @Mapping(source="encounter.id", target="encounterId"),
            @Mapping(source="patient.uuid", target="patientUuid"),
            @Mapping(source="visit.uuid", target="visitUuid"),
            @Mapping(source="encounter.uuid", target="uuid"),
            @Mapping(source="encounter.createdBy", target="createdBy"),
            @Mapping(source="encounter.dateCreated", target="dateCreated"),
            @Mapping(source="encounter.modifiedBy", target="modifiedBy"),
            @Mapping(source="encounter.dateModified", target="dateModified"),
            @Mapping(source="encounter.organisationUnitId", target="organisationUnitId"),
            @Mapping(source="encounter.archived", target="archived")

    })
    EncounterDTO toEncounterDTO(Encounter encounter, Patient patient, Visit visit);

    Encounter toEncounter(EncounterDTO encounterDTO);

    @Mappings({
            @Mapping(source="patient.id", target="patientId"),
            @Mapping(source="encounter.id", target="encounterId"),
            @Mapping(source="patient.uuid", target="patientUuid"),
            @Mapping(source="encounter.uuid", target="uuid"),
            @Mapping(source="form.id", target="formId"),
            @Mapping(source="form.name", target="formName"),
            @Mapping(source="encounter.programCode", target="programCode"),
            @Mapping(source="encounter.organisationUnitId", target="organisationUnitId"),
            @Mapping(source="encounter.createdBy", target="createdBy"),
            @Mapping(source="encounter.dateCreated", target="dateCreated"),
            @Mapping(source="encounter.modifiedBy", target="modifiedBy"),
            @Mapping(source="encounter.dateModified", target="dateModified"),
            @Mapping(source="encounter.archived", target="archived")
    })
    EncounterDTO toEncounterDTO(Patient patient, Encounter encounter, Form form);




}
