package org.lamisplus.modules.sync.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lamisplus.modules.base.repository.*;
import org.lamisplus.modules.sync.domain.mapper.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

class ObjectDeserializerTest {
    @Mock
    PatientRepository patientRepository;
    @Mock
    VisitRepository visitRepository;
    @Mock
    EncounterRepository encounterRepository;
    @Mock
    FormDataRepository formDataRepository;
    @Mock
    AppointmentRepository appointmentRepository;
    @Mock
    PatientMapper patientMapper;
    @Mock
    VisitMapper visitMapper;
    @Mock
    EncounterMapper encounterMapper;
    @Mock
    FormDataMapper formDataMapper;
    @Mock
    AppointmentMapper appointmentMapper;
    @Mock
    Logger log;
    @InjectMocks
    ObjectDeserializer objectDeserializer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testRestTemplate() {
        RestTemplate result = objectDeserializer.restTemplate();
        Assertions.assertEquals(null, result);
    }

    @Test
    void testDeserialize() throws Exception {
        when(patientMapper.toPatient(any())).thenReturn(null);
        when(visitMapper.toVisit(any())).thenReturn(null);
        when(encounterMapper.toEncounter(any())).thenReturn(null);
        when(formDataMapper.toFormData(any())).thenReturn(null);
        when(appointmentMapper.toAppointment(any())).thenReturn(null);

        List result = objectDeserializer.deserialize(new byte[]{(byte) 0}, "table");
        Assertions.assertEquals(new ArrayList(), result);
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme