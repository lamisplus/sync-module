package org.lamisplus.modules.sync.repo;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.sync.domain.entity.SyncHistory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SyncHistoryRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<SyncHistory> findAll() {
        return jdbcTemplate.query("SELECT * FROM sync_history", new BeanPropertyRowMapper<SyncHistory>(SyncHistory.class));
    }

    public List<SyncHistory> findSyncHistories(Long organisationUnitId) {
        return jdbcTemplate.query("SELECT * from sync_history sh where organisation_unit_id=? order by sh.date_last_sync desc limit 100",
                new BeanPropertyRowMapper<SyncHistory>(SyncHistory.class), organisationUnitId);
    }

    public Optional<SyncHistory> findByTableNameAndOrganisationUnitId(String tableName, Long organisationUnitId) {
        return jdbcTemplate.query("SELECT * FROM sync_history WHERE table_name=? AND organisation_unit_id=?order by id desc limit 1",
                new BeanPropertyRowMapper<SyncHistory>(SyncHistory.class), tableName, organisationUnitId).stream().findFirst();
    }

    public List<SyncHistory> findSyncQueueByProcessed(int processed) {
        return jdbcTemplate.query("SELECT * from sync_history sh where sh.processed = ? order by sh.date_last_sync desc limit 5",
                new BeanPropertyRowMapper<SyncHistory>(SyncHistory.class), processed);
    }

    public List<SyncHistory> findSyncQueueIdByProcessed(int processed) {
        return jdbcTemplate.query("SELECT id FROM sync_history WHERE processed=?",
                new BeanPropertyRowMapper<SyncHistory>(SyncHistory.class), processed);
    }

    public Optional<SyncHistory> findById(Long id) {
        return jdbcTemplate.query("SELECT * FROM sync_history WHERE id=?",
                new BeanPropertyRowMapper<SyncHistory>(SyncHistory.class), id).stream().findFirst();
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM Sync_history WHERE id=?", id);
    }

    public int save(SyncHistory syncHistory) {
        if(syncHistory.getId() == null || syncHistory.getId() == 0){
            return jdbcTemplate.update("INSERT INTO sync_history (date_last_sync, organisation_unit_id, table_name, " +
                            "processed, sync_queue_id, remote_access_token_id, upload_size) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    syncHistory.getDateLastSync(), syncHistory.getOrganisationUnitId(),
                    syncHistory.getTableName(), syncHistory.getProcessed(), syncHistory.getSyncQueueId(),
                    syncHistory.getRemoteAccessTokenId(), syncHistory.getUploadSize());

        }
        return jdbcTemplate.update("UPDATE sync_history SET date_last_sync=?, organisation_unit_id=?, table_name=?, " +
                        "processed=?, sync_queue_id=?, remote_access_token_id=?, upload_size=? WHERE id=?",
                syncHistory.getDateLastSync(), syncHistory.getOrganisationUnitId(),
                syncHistory.getTableName(), syncHistory.getProcessed(), syncHistory.getSyncQueueId(),
                syncHistory.getRemoteAccessTokenId(), syncHistory.getUploadSize(), syncHistory.getId());
    }

    public int saveAll(List<SyncHistory> syncHistoryList) {
        int saveCount = 0;
        for (SyncHistory syncHistory : syncHistoryList) {
            saveCount = saveCount + this.save(syncHistory);
        }
        return saveCount;
    }
}
