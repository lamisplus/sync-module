package org.lamisplus.modules.sync.service;

import com.google.common.hash.Hashing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.sync.controller.apierror.EntityNotFoundException;
import org.lamisplus.modules.sync.domain.entity.RemoteAccessToken;
import org.lamisplus.modules.sync.domain.entity.SyncQueue;
import org.lamisplus.modules.sync.repo.RemoteAccessTokenRepository;
import org.lamisplus.modules.sync.repo.SyncQueueRepository;
import org.lamisplus.modules.sync.utility.AESUtil;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.GeneralSecurityException;


@Service
@RequiredArgsConstructor
@Slf4j
public class SyncServerService {
    private final SyncQueueRepository syncQueueRepository;
    private final QueueManager queueManager;
    private final RemoteAccessTokenRepository remoteAccessTokenRepository;


    public SyncQueue save(byte[] bytes, String hash, String table, Long facilityId, String name) throws Exception {
        log.info("I am in the server");

        RemoteAccessToken remoteAccessToken = remoteAccessTokenRepository.findByNameAndOrganisationUnitId(name, facilityId)
                .orElseThrow(()-> new EntityNotFoundException(RemoteAccessToken.class, "Name & organisationUnitId", name +" & " + facilityId));

        // Verify the hash value of the byte, if the do not values match set processed to -1
        Integer processed;
        if (!hash.equals(Hashing.sha256().hashBytes(bytes).toString())) processed = -1;
        else processed = 0;

        SecretKey secretKey = AESUtil.getPrivateAESKeyFromDB(remoteAccessToken);

        bytes = this.decrypt(bytes, secretKey);

        SyncQueue syncQueue = queueManager.setQueue(bytes,table, facilityId);
        syncQueue.setProcessed(processed);

        if(syncQueueRepository.save(syncQueue) > 0){
            syncQueue = syncQueueRepository.findByFileNameAndOrganisationUnitAnd(syncQueue.getFileName(), syncQueue.getOrganisationUnitId(), syncQueue.getDateCreated()).get();
            return syncQueue;
        }
        return null;
    }

    private byte[] decrypt(byte[] bytes, SecretKey secretKey) throws GeneralSecurityException, IOException {

        Cipher decryptCipher = Cipher.getInstance("AES");
        decryptCipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedMessageBytes = decryptCipher.doFinal(bytes);
        return decryptedMessageBytes;
    }
}
