package org.lamisplus.modules.sync.service;

import com.google.common.hash.Hashing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.lamisplus.modules.sync.domain.entity.SyncQueue;
import org.lamisplus.modules.sync.repo.SyncQueueRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncQueueService {

    private final SyncQueueRepository syncQueueRepository;
    private final QueueManager queueManager;

    public  void save(SyncQueue syncQueue) {
        syncQueueRepository.save(syncQueue);
    }

    public SyncQueue save(byte[] bytes, String hash, String table, Long facilityId) throws Exception {
        log.info("I am in the server");

        SyncQueue syncQueue = queueManager.setQueue(bytes,table, facilityId);

        // Verify the hash value of the byte, if the do not values match set processed to -1
        if (!hash.equals(Hashing.sha256().hashBytes(bytes).toString())) syncQueue.setProcessed(-1);
        else syncQueue.setProcessed(0);

        if(syncQueueRepository.save(syncQueue) > 0){
            syncQueue = syncQueueRepository.findByFileNameAndOrganisationUnitAnd(syncQueue.getFileName(), syncQueue.getOrganisationUnitId(), syncQueue.getDateCreated()).get();
            return syncQueue;
        }
        return null;
    }

    public SyncQueue getAllSyncQueueById(Long id) {
        //TODO: handle exceptions
        return syncQueueRepository.findById(id).get();
    }
}
