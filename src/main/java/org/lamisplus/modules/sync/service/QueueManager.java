//package org.lamisplus.modules.sync.service;
//
//import com.google.common.io.ByteStreams;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.io.FileUtils;
//import org.lamisplus.modules.sync.domain.entity.SyncQueue;
//import org.lamisplus.modules.sync.repository.SyncQueueRepository;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.InputStream;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Objects;
//import java.util.Set;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class QueueManager {
//    private static ExecutorService executorService = Executors.newFixedThreadPool(10);
//    private static final Set<Long> runningFacilities = new HashSet<>();
//    private final SyncQueueRepository syncQueueRepository;
//    private final ObjectDeserializer objectDeserializer;
//
//    @Scheduled(fixedDelay = 300000)
//    public void queue() {
//        List<SyncQueue> syncQueues = syncQueueRepository.getAllSyncQueueByFacilitiesNotProcessed();
//        synchronized (runningFacilities) {
//            syncQueues.forEach(syncQueue -> {
//                if (!runningFacilities.contains(syncQueue.getOrganisationUnitId())) {
//                    SyncThread syncThread = new SyncThread(syncQueue.getOrganisationUnitId());
//                    executorService.execute(syncThread);
//                    // add facility to running facilities
//                    runningFacilities.add(syncQueue.getOrganisationUnitId());
//                }
//            });
//        }
//    }
//
//     class SyncThread implements Runnable {
//        private Long facilityId;
//
//        SyncThread(Long facilityId) {
//            this.facilityId = facilityId;
//        }
//
//        @Override
//        public void run() {
//            try {
//                // process facility files
//                List<SyncQueue> syncQueues = syncQueueRepository.getAllSyncQueueByFacilityNotProcessed(facilityId);
//                log.info("available file for processing are : {}", syncQueues.size());
//                syncQueues.forEach(syncQueue -> {
//                    String folder = ("sync/").concat(Long.toString(syncQueue.getOrganisationUnitId())
//                            .concat("/")).concat(syncQueue.getTableName()).concat("/");
//                    File file = FileUtils.getFile(folder, syncQueue.getFileName());
//                    try {
//                        InputStream targetStream = new FileInputStream(file);
//                        byte[] bytes = ByteStreams.toByteArray(Objects.requireNonNull(targetStream));
//                        List<?> list = objectDeserializer.deserialize(bytes, syncQueue.getTableName());
//                        if(!list.isEmpty()){
//                            syncQueue.setProcessed(1);
//                            syncQueueRepository.save(syncQueue);
//                            FileUtils.deleteDirectory(file);
//                            log.info("deleting file : {}", file.getName());
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                });
//            } catch (Exception exception) {
//                exception.printStackTrace();
//            }
//            synchronized (runningFacilities) {
//                // remove from running facilities
//                runningFacilities.remove(facilityId);
//            }
//        }
//    }
//    ;
//}
package org.lamisplus.modules.sync.service;

import com.google.common.io.ByteStreams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.lamisplus.modules.sync.domain.entity.SyncQueue;
import org.lamisplus.modules.sync.repository.SyncQueueRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueManager {

    private final ObjectDeserializer objectDeserializer;
    private final SyncQueueRepository syncQueueRepository;

    public SyncQueue queue(byte[] bytes, String table, Long facilityId) throws Exception {
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
        syncQueue = syncQueueRepository.save(syncQueue);
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
                        if (!list.isEmpty()) {
                            syncQueue.setProcessed(1);
                            syncQueueRepository.save(syncQueue);
                            FileUtils.deleteDirectory(file);
                            log.info("deleting file : {}", file.getName());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
    }
}

