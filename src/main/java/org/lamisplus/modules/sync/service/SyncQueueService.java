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
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncQueueService {

    private final SyncQueueRepository syncQueueRepository;
    private final QueueManager queueManager;
    private final GenerateKeys generateKeys;
    private final RemoteAccessTokenRepository remoteAccessTokenRepository;

    public  void save(SyncQueue syncQueue) {
        syncQueueRepository.save(syncQueue);
    }

    public SyncQueue save(byte[] bytes, String hash, String table, Long facilityId, String name) throws Exception {
        log.info("I am in the server");

        SyncQueue syncQueue = queueManager.setQueue(bytes,table, facilityId);
        RemoteAccessToken remoteAccessToken = remoteAccessTokenRepository.findByNameAndOrganisationUnitId(name, facilityId)
                .orElseThrow(()-> new EntityNotFoundException(RemoteAccessToken.class, "Name & organisationUnitId", name +" & " + facilityId));

        bytes = this.decrypt(bytes, remoteAccessToken);

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

    private byte[] decrypt(byte[] bytes, RemoteAccessToken remoteAccessToken) throws GeneralSecurityException, IOException {

        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, generateKeys.readPrivateKey(remoteAccessToken));
        byte[] decryptedMessageBytes = decryptCipher.doFinal(bytes);
        return decryptedMessageBytes;
    }
}
