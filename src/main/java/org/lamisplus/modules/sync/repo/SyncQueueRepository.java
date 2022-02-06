package org.lamisplus.modules.sync.repo;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.sync.domain.entity.SyncQueue;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SyncQueueRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<SyncQueue> findAll() {
        return jdbcTemplate.query("SELECT * FROM sync_queue", new BeanPropertyRowMapper<SyncQueue>(SyncQueue.class));
    }

    public List<SyncQueue> getAllByProcessed(Integer processed) {
        return jdbcTemplate.query("SELECT * FROM sync_queue WHERE processed = ?", new BeanPropertyRowMapper<SyncQueue>(SyncQueue.class), processed);
    }

    public List<SyncQueue> getAllSyncQueueByFacilitiesNotProcessed() {
        return jdbcTemplate.query("SELECT * from sync_queue sq where  sq.organisation_unit_id in (select distinct ou.organisation_unit_id from sync_queue ou where ou.processed = 0)", new BeanPropertyRowMapper<SyncQueue>(SyncQueue.class));
    }

    public List<SyncQueue> getAllSyncQueueByFacilityNotProcessed(Long facilityId) {
        return jdbcTemplate.query("SELECT * from sync_queue sq where sq.organisation_unit_id = ? and sq.processed = 0 order by sq.date_created desc",
                new BeanPropertyRowMapper<SyncQueue>(SyncQueue.class), facilityId);
    }

    public Optional<SyncQueue> findById(Long id) {
        return jdbcTemplate.query("SELECT * FROM sync_queue WHERE id=?",
                new BeanPropertyRowMapper<SyncQueue>(SyncQueue.class), id).stream().findFirst();
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM sync_queue WHERE id=?", id);
    }

    public int save(SyncQueue syncQueue) {
        //having issues with pk violation... this is a temporary measure
        //jdbcTemplate.update("select setval('sync_queue_id_seq', (select max(id) from sync_queue))");

        if(syncQueue.getId() == null || syncQueue.getId() == 0){
            return jdbcTemplate.update("INSERT INTO sync_queue (date_created, file_name, organisation_unit_id, processed, table_name) " +
                            "VALUES (?, ?, ?, ?, ?)", syncQueue.getDateCreated(), syncQueue.getFileName(),
                    syncQueue.getOrganisationUnitId(), syncQueue.getProcessed(), syncQueue.getTableName());

        }
        return jdbcTemplate.update("UPDATE sync_queue SET date_created=?, file_name=?, organisation_unit_id=?, processed=?, table_name=? " +
                        "WHERE id=?", syncQueue.getDateCreated(), syncQueue.getFileName(), syncQueue.getOrganisationUnitId(), syncQueue.getProcessed(), syncQueue.getTableName(), syncQueue.getId());
    }

    public Optional<SyncQueue> getLastSaved() {
        return jdbcTemplate.query("SELECT * FROM sync_queue sq ORDER BY sq.date_created DESC limit 1",
                new BeanPropertyRowMapper<SyncQueue>(SyncQueue.class)).stream().findFirst();

    }

    public Optional<Integer> getMaxId() {
        return jdbcTemplate.query("SELECT MAX(id) from sync_queue",
                new BeanPropertyRowMapper<Integer>(Integer.class)).stream().findFirst();

    }

    public Optional<SyncQueue> findByFileNameAndOrganisationUnitAnd(String fileName, Long facilityId, LocalDateTime DateCreated) {
        return jdbcTemplate.query("SELECT * FROM sync_queue WHERE file_name=? AND organisation_unit_id=? AND date_created=?",
                new BeanPropertyRowMapper<SyncQueue>(SyncQueue.class), fileName, facilityId, DateCreated).stream().findFirst();
    }







}
