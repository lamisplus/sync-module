package org.lamisplus.modules.sync.service;

import com.google.common.hash.Hashing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.lamisplus.modules.sync.controller.apierror.EntityNotFoundException;
import org.lamisplus.modules.sync.domain.entity.RemoteAccessToken;
import org.lamisplus.modules.sync.domain.entity.SyncQueue;
import org.lamisplus.modules.sync.repo.RemoteAccessTokenRepository;
import org.lamisplus.modules.sync.repo.SyncQueueRepository;
import org.lamisplus.modules.sync.utility.AESUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncQueueService {

    private final SyncQueueRepository syncQueueRepository;

    public  void save(SyncQueue syncQueue) {
        syncQueueRepository.save(syncQueue);
    }

    public SyncQueue getAllSyncQueueById(Long id) {
        //TODO: handle exceptions
        return syncQueueRepository.findById(id).get();
    }


    @Scheduled(fixedDelay = 300000)
    public void deleteProcessed(){
        syncQueueRepository.getAllByProcessed(1).stream().map(syncQueue -> {
            File file = new File(syncQueue.getFileName());
            if (file.exists()) {
                file.delete();
            }
            return syncQueue;
        }).collect(Collectors.toList());
    }
}
