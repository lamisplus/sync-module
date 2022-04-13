package org.lamisplus.modules.sync.repo;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.base.domain.entity.*;
import org.lamisplus.modules.sync.domain.entity.SyncHistory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SyncUUIDUpdateRepository {
    private final JdbcTemplate jdbcTemplate;

    public void updateAllPatient(List<Patient> patients) {

        String query = "update patient set uuid=? where id=?";
        List<Object[]> inputList = new ArrayList<Object[]>();
        for(Patient patient:patients){
            Object[] tmp = {patient.getUuid(), patient.getId()};
            inputList.add(tmp);
        }
        jdbcTemplate.batchUpdate(query, inputList);
    }

    public void updateAllVisit(List<Visit> visits) {

        String query = "update visit set uuid=? where id=?";
        List<Object[]> inputList = new ArrayList<Object[]>();
        for(Visit visit:visits){
            Object[] tmp = {visit.getUuid(), visit.getId()};
            inputList.add(tmp);
        }
        jdbcTemplate.batchUpdate(query, inputList);
    }

    public void updateAllEncounter(List<Encounter> encounters) {

        String query = "update encounter set uuid=? where id=?";
        List<Object[]> inputList = new ArrayList<Object[]>();
        for(Encounter encounter:encounters){
            Object[] tmp = {encounter.getUuid(), encounter.getId()};
            inputList.add(tmp);
        }
        jdbcTemplate.batchUpdate(query, inputList);
    }

    public void updateAllFormData(List<FormData> formDataList) {

        String query = "update form_data set uuid=? where id=?";
        List<Object[]> inputList = new ArrayList<Object[]>();
        for(FormData formData:formDataList){
            Object[] tmp = {formData.getUuid(), formData.getId()};
            inputList.add(tmp);
        }
        jdbcTemplate.batchUpdate(query, inputList);
    }

    public void saveAllAppointment(List<Appointment> appointments) {

        String query = "update appointment set uuid=? where id=?";
        List<Object[]> inputList = new ArrayList<Object[]>();
        for(Appointment appointment:appointments){
            Object[] tmp = {appointment.getUuid(), appointment.getId()};
            inputList.add(tmp);
        }
        jdbcTemplate.batchUpdate(query, inputList);
    }

}
