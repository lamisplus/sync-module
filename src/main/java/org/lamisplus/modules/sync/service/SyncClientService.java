package org.lamisplus.modules.sync.service;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.lamisplus.modules.base.controller.apierror.EntityNotFoundException;
import org.lamisplus.modules.base.repository.OrganisationUnitRepository;
import org.lamisplus.modules.sync.domain.dto.UploadDTO;
import org.lamisplus.modules.sync.domain.entity.RemoteAccessToken;
import org.lamisplus.modules.sync.domain.entity.SyncHistory;
import org.lamisplus.modules.sync.domain.entity.SyncQueue;
import org.lamisplus.modules.sync.domain.entity.Tables;
import org.lamisplus.modules.sync.repo.RemoteAccessTokenRepository;
import org.lamisplus.modules.sync.utility.HttpConnectionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncClientService {
    private final ObjectMapper mapper = new ObjectMapper();
    private final SyncHistoryService syncHistoryService;
    private final RemoteAccessTokenRepository remoteAccessTokenRepository;
    private final ObjectSerializer objectSerializer;

    @Async
    public CompletableFuture<String> sender(UploadDTO uploadDTO) throws Exception {
        log.info("path: {}", uploadDTO.getServerUrl());
        RemoteAccessToken remoteAccessToken = remoteAccessTokenRepository.findByUrl(uploadDTO.getServerUrl())
                .orElseThrow(() -> new EntityNotFoundException(RemoteAccessToken.class, "url", ""+uploadDTO.getServerUrl()));
        if(remoteAccessToken.getToken() == null) new EntityNotFoundException(RemoteAccessToken.class, "token", ""+remoteAccessToken.getToken());

        String token = " Bearer ".concat(remoteAccessToken.getToken());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);



        log.info("table values: => {}", Arrays.toString(Tables.values()));

        for (Tables table : Tables.values()) {
            SyncHistory syncHistory = syncHistoryService.getSyncHistory(table.name(), uploadDTO.getFacilityId());
            LocalDateTime dateLastSync = syncHistory.getDateLastSync();
            log.info("last date sync 1 {}", dateLastSync);
            List<?> serializeTableRecords = objectSerializer.serialize(table, uploadDTO.getFacilityId(), dateLastSync);

            if (!serializeTableRecords.isEmpty()) {
                Object serializeObject = serializeTableRecords.get(0);
                log.info("object size:  {} ", serializeTableRecords.size());
                if (!serializeObject.toString().contains("No table records was retrieved for server sync")) {
                    String pathVariable = table.name().concat("/").concat(Long.toString(uploadDTO.getFacilityId()));
                    //log.info("path: {}", pathVariable);
                    String url = uploadDTO.getServerUrl().concat("/api/sync/").concat(pathVariable);

                    log.info("url : {}", url);

                    byte[] bytes = mapper.writeValueAsBytes(serializeTableRecords);
                    String response = new HttpConnectionManager().post(bytes, token, url);

                    log.info("Done : {}", response);

                    syncHistory.setTableName(table.name());
                    syncHistory.setOrganisationUnitId(uploadDTO.getFacilityId());
                    syncHistory.setDateLastSync(LocalDateTime.now());
                    try {
                        //For serializing the date on the sync queue
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.registerModule(new JavaTimeModule());
                        objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                        SyncQueue syncQueue = objectMapper.readValue(response, SyncQueue.class);

                        syncHistory.setProcessed(syncQueue.getProcessed());
                        syncHistory.setSyncQueueId(syncQueue.getId());

                        //get remote access token id
                        syncHistory.setRemoteAccessTokenId(remoteAccessToken.getId());
                        syncHistory.setUploadSize(serializeTableRecords.size());
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    syncHistoryService.save(syncHistory);
                }
            }
        }
        return CompletableFuture.completedFuture("Successful");
    }
}

