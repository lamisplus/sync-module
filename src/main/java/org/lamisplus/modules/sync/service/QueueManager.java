package org.lamisplus.modules.sync.service;

import com.google.common.io.ByteStreams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.lamisplus.modules.sync.domain.entity.SyncQueue;
import org.lamisplus.modules.sync.repo.SyncQueueRepository;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueManager {

    private final ObjectDeserializer objectDeserializer;
    private final SyncQueueRepository syncQueueRepository;
    private final SimpMessageSendingOperations messagingTemplate;


    public SyncQueue queue(byte[] bytes, String table, Long facilityId) throws Exception {

        this.setQueue(bytes, table, facilityId);

        Optional<SyncQueue> optionalSyncQueue = syncQueueRepository.getLastSaved();
        if(optionalSyncQueue.isPresent()) return optionalSyncQueue.get();

        return null;
    }

    public SyncQueue setQueue(byte[] bytes, String table, Long facilityId) throws IOException {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss.ms");
        String folder = ("sync/").concat(Long.toString(facilityId).concat("/")).concat(table).concat("/");
        String fileName = dateFormat.format(date) + "_" + timeFormat.format(date) + ".json";
        File file = new File(folder.concat(fileName));
        FileUtils.writeByteArrayToFile(file, bytes);
        SyncQueue syncQueue = new SyncQueue();
        syncQueue.setFileName(fileName);
        syncQueue.setOrganisationUnitId(facilityId);
        syncQueue.setTableName(table);
        syncQueue.setDateCreated(LocalDateTime.now());
        syncQueue.setProcessed(0);
        syncQueue.setProcessedSize(0);
        return syncQueue;
    }


    @Scheduled(fixedDelay = 300000)
    public void process() throws Exception {
        List<SyncQueue> filesNotProcessed = syncQueueRepository.getAllSyncQueueByFacilitiesNotProcessed();
        log.info("available file for processing are : {}", filesNotProcessed.size());
        filesNotProcessed
                .forEach(syncQueue -> {
                    String folder = ("sync/").concat(Long.toString(syncQueue.getOrganisationUnitId())
                            .concat("/")).concat(syncQueue.getTableName()).concat("/");
                    File file = FileUtils.getFile(folder, syncQueue.getFileName());
                    try {
                        InputStream targetStream = new FileInputStream(file);
                        byte[] bytes = ByteStreams.toByteArray(Objects.requireNonNull(targetStream));
                        List<?> list = objectDeserializer.deserialize(bytes, syncQueue.getTableName());
                        messagingTemplate.convertAndSend("/topic/patient-sync-size", list.size());
                        if (!list.isEmpty()) {
                            syncQueue.setProcessed(1);
                            syncQueue.setProcessedSize(list.size());
                            syncQueueRepository.save(syncQueue);
                            FileUtils.deleteQuietly(file);
                            log.info("deleting file : {}", file.getName());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }
}

