package org.lamisplus.modules.sync.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.base.domain.dto.*;
import org.lamisplus.modules.base.domain.entity.*;
import org.lamisplus.modules.sync.domain.mapper.*;
import org.lamisplus.modules.base.repository.*;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ObjectDeserializer {

    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;
    private final EncounterRepository encounterRepository;
    private final FormDataRepository formDataRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientMapper patientMapper;
    private final VisitMapper visitMapper;
    private final EncounterMapper encounterMapper;
    private final FormDataMapper formDataMapper;
    private final AppointmentMapper appointmentMapper;


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    public List<?> deserialize(byte[] bytes, String table) throws Exception {
        String data = new String(bytes, StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        if (table.equals("patient")) {
            log.info("Saving " + table + " on Server");
            return processAndSavePatientsOnServer(data, objectMapper);
        }
        if (table.equals("visit")) {
            log.info("Saving " + table + " on Server");
            return processAndSaveVisitsOnServer(data, objectMapper);
        }
        if (table.equals("encounter")) {
            log.info("Saving " + table + " on Server");
            return processAndSaveEncountersOnServer(data, objectMapper);
        }
        if (table.equals("form_data")) {
            log.info("Saving " + table + " on Server");
            return processAndSaveFormDatasOnServer(data, objectMapper);
        }
        if (table.equals("appointment")) {
            log.info("Saving " + table + " on Server");
            return processAndSaveAppointmentsOnServer(data, objectMapper);
        }
        List<String> msg = new LinkedList<>();
        msg.add("Nothing was saved on the server");
        return msg;
    }

    private List<Patient> processAndSavePatientsOnServer(String data, ObjectMapper objectMapper) throws JsonProcessingException {
        List<Patient> patients = new LinkedList<>();
        List<PatientDTO> patientDTOS = objectMapper.readValue(data, new TypeReference<List<PatientDTO>>() {
        });

        patientDTOS.forEach(patientDTO -> {
            Patient patient = patientMapper.toPatient(patientDTO);
            patientRepository.findByUuid(patient.getUuid()).ifPresent(value -> patient.setId(value.getId()));
            patients.add(patient);
        });
        List<Patient> savedPatients = patientRepository.saveAll(patients);
        log.info("number of patients save on server => : {}", savedPatients.size());
        return patientRepository.saveAll(savedPatients);
    }

    private List<Visit> processAndSaveVisitsOnServer(String data, ObjectMapper objectMapper) throws JsonProcessingException {
        List<Visit> visits = new LinkedList<>();
        List<VisitDTO> visitDTOS = objectMapper.readValue(data, new TypeReference<List<VisitDTO>>() {
        });
        visitDTOS.forEach(visitDTO -> {
            Visit visit = visitMapper.toVisit(visitDTO);
            patientRepository.findByUuid(visitDTO.getPatientUuid())
                    .ifPresent(patient -> {
                        visit.setPatientId(patient.getId());
                        visitRepository.findByUuid(visit.getUuid())
                                .ifPresent(visitDb -> visit.setId(visitDb.getId()));
                        visits.add(visit);
                    });
        });
        List<Visit> savedVisits = visitRepository.saveAll(visits);
        log.info("number of visits save on server => : {}", savedVisits.size());
        return savedVisits;
    }

    private List<FormData> processAndSaveFormDatasOnServer(String data, ObjectMapper objectMapper) throws JsonProcessingException {
        List<FormData> formDataList = new LinkedList<>();
        List<FormDataDTO> formDataDTOS = objectMapper.readValue(data, new TypeReference<List<FormDataDTO>>() {
        });
        formDataDTOS.forEach(formDataDTO -> {
            FormData formData = formDataMapper.toFormData(formDataDTO);
            encounterRepository.findByUuid(formDataDTO.getEncounterUuid())
                    .ifPresent(encounter -> {
                        formData.setEncounterId(encounter.getId());
                        formDataRepository.findByUuid(formData.getUuid()).ifPresent(formDataDb -> {
                            formData.setId(formDataDb.getId());
                        });
                        formDataList.add(formData);
                    });
        });
        List<FormData> savedFormData = formDataRepository.saveAll(formDataList);
        log.info("number of form-data save on server => : {}", savedFormData.size());
        return savedFormData;
    }

    private List<Encounter> processAndSaveEncountersOnServer(String data, ObjectMapper objectMapper) throws JsonProcessingException {
        List<Encounter> encounters = new LinkedList<>();
        List<EncounterDTO> encounterDTOS =
                objectMapper.readValue(data, new TypeReference<List<EncounterDTO>>() {
                });
        encounterDTOS.forEach(encounterDTO -> {
            Encounter encounter = encounterMapper.toEncounter(encounterDTO);
            patientRepository.findByUuid(encounterDTO.getPatientUuid())
                    .ifPresent(patient -> {
                        encounter.setPatientId(patient.getId());
                        visitRepository.findByUuid(encounterDTO.getVisitUuid())
                                .ifPresent(visit -> {
                                    encounter.setVisitId(visit.getId());
                                    encounterRepository.findByUuid(encounter.getUuid())
                                            .ifPresent(encounterDb -> encounter.setId(encounterDb.getId()));
                                    encounters.add(encounter);
                                });

                    });
        });
        List<Encounter> savedEncounters = encounterRepository.saveAll(encounters);
        log.info("number of encounters save on server => : {}", savedEncounters.size());
        return savedEncounters;
    }

    private List<Appointment> processAndSaveAppointmentsOnServer(String data, ObjectMapper objectMapper) throws JsonProcessingException {
        List<Appointment> appointments = new LinkedList<>();
        List<AppointmentDTO> appointmentDTOS =
                objectMapper.readValue(data, new TypeReference<List<AppointmentDTO>>() {
                });
        appointmentDTOS.forEach(appointmentDTO -> {
            Appointment appointment = appointmentMapper.toAppointment(appointmentDTO);
            patientRepository.findByUuid(appointmentDTO.getPatientUuid())
                    .ifPresent(patient -> {
                        appointment.setPatientId(patient.getId());
                        visitRepository.findByUuid(appointmentDTO.getVisitUuid())
                                .ifPresent(visit -> {
                                    appointment.setVisitId(visit.getId());
                                    encounterRepository.findByUuid(appointment.getUuid())
                                            .ifPresent(encounter -> {
                                                appointment.setId(encounter.getId());
                                            });
                                    appointmentRepository.findByUuid(appointment.getUuid())
                                            .ifPresent(appointmentDb
                                                    -> appointment.setId(appointmentDb.getId()));
                                    appointments.add(appointmentRepository.save(appointment));
                                });
                    });
        });
        log.info("number of appointments save on server => : {}", appointments.size());
        return appointments;
    }
}
