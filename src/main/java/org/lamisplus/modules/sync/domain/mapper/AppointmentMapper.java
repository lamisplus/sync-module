package org.lamisplus.modules.sync.domain.mapper;

import org.lamisplus.modules.base.domain.dto.AppointmentDTO;
import org.lamisplus.modules.base.domain.entity.Appointment;
import org.lamisplus.modules.base.domain.entity.Patient;
import org.lamisplus.modules.base.domain.entity.Visit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    @Mappings({
            @Mapping(source="appointment.id", target="id"),
            @Mapping(source="patient.uuid", target="patientUuid"),
            @Mapping(source="patient.id", target="patientId"),
            @Mapping(source="visit.id", target="visitId"),
            @Mapping(source="visit.uuid", target="visitUuid"),
            @Mapping(source="appointment.uuid", target="uuid"),
            @Mapping(source="appointment.createdBy", target="createdBy"),
            @Mapping(source="appointment.dateCreated", target="dateCreated"),
            @Mapping(source="appointment.modifiedBy", target="modifiedBy"),
            @Mapping(source="appointment.dateModified", target="dateModified"),
            @Mapping(source="appointment.organisationUnitId", target="organisationUnitId"),
            @Mapping(source="appointment.archived", target="archived")

    })
    AppointmentDTO toAppointmentDTO(Appointment appointment, Patient patient, Visit visit);

    Appointment toAppointment(AppointmentDTO appointmentDTO);

    List<Appointment> toAppointmentList(List<AppointmentDTO> appointmentDTOS);

}
