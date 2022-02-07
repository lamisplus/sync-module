package org.lamisplus.modules.sync.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.base.domain.entity.OrganisationUnit;
import org.lamisplus.modules.base.repository.OrganisationUnitRepository;
import org.lamisplus.modules.sync.domain.entity.RemoteAccessToken;
import org.lamisplus.modules.sync.domain.entity.SyncHistory;
import org.lamisplus.modules.sync.domain.entity.SyncQueue;
import org.lamisplus.modules.sync.repo.RemoteAccessTokenRepository;
import org.lamisplus.modules.sync.repo.SyncHistoryRepository;
import org.lamisplus.modules.sync.utility.HttpConnectionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncHistoryService {

    private final SyncHistoryRepository syncHistoryRepository;
    private final RemoteAccessTokenRepository remoteAccessTokenRepository;
    private final OrganisationUnitRepository organisationUnitRepository;

    public void save(SyncHistory syncHistory) {
        syncHistoryRepository.save(syncHistory);
    }

    public SyncHistory getSyncHistory(String table, Long facilityId){
        Optional<SyncHistory> syncHistory = syncHistoryRepository.findByTableNameAndOrganisationUnitId(table, facilityId);
        return syncHistory.orElseGet(SyncHistory::new);
    }

    public List<SyncHistory> getSyncHistories() {
        List<SyncHistory> syncHistoryList1 = new ArrayList<>();
        List<SyncHistory> syncHistoryList = syncHistoryRepository.findSyncHistories();
        syncHistoryList.forEach(syncHistory -> {
            Optional<OrganisationUnit> organisationUnit = organisationUnitRepository.findById(syncHistory.getOrganisationUnitId());
            if(organisationUnit.isPresent()) {
                syncHistory.setFacilityName(organisationUnit.get().getName());
                syncHistory.setStatus("Processing");
                syncHistoryList1.add(syncHistory);
            }
        });
        return syncHistoryList1;
    }

    @Scheduled(fixedDelay = 30000)
    public void getSyncQueueIdForClient() {
        List<SyncHistory> histories = new ArrayList<>();
        log.info("Retrieving processed status from server...");

        List<SyncHistory> syncHistories = syncHistoryRepository.findSyncQueueByProcessed(0);
        syncHistories.forEach(syncHistory -> {
            if(syncHistory.getSyncQueueId() != null) {
                try {
                    //For serializing the date on the sync queue
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());
                    objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                    //getting token
                    //TODO: handle error, syncQueueId maybe wrong
                    log.info("sync history for remote access token id is - {}", syncHistory);
                    RemoteAccessToken remoteAccessToken = remoteAccessTokenRepository.findById(syncHistory.getRemoteAccessTokenId())
                            .orElseThrow(() -> new NullPointerException("RemoteAccessToken is null"));

                    //calling the server
                    String url = remoteAccessToken.getUrl().concat("/api/sync/sync-queue/").concat(Long.toString(syncHistory.getSyncQueueId()));

                    log.info("url is {}", url);
                    String response = new HttpConnectionManager().get(url);
                    SyncQueue syncQueue = objectMapper.readValue(response, SyncQueue.class);
                    syncHistory.setProcessed(syncQueue.getProcessed());
                    histories.add(syncHistory);
                    log.info("processed status retrieved from url {}", remoteAccessToken.getUrl());
                    log.info("syncHistory is now {}", syncHistory);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        syncHistoryRepository.saveAll(histories);
    }
}
