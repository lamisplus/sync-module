package org.lamisplus.modules.sync.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.base.controller.apierror.EntityNotFoundException;
import org.lamisplus.modules.base.domain.entity.OrganisationUnit;
import org.lamisplus.modules.base.repository.OrganisationUnitRepository;
import org.lamisplus.modules.sync.domain.dto.RemoteUrlDTO;
import org.lamisplus.modules.sync.domain.dto.UploadDTO;
import org.lamisplus.modules.sync.domain.entity.RemoteAccessToken;
import org.lamisplus.modules.sync.domain.entity.SyncHistory;
import org.lamisplus.modules.sync.domain.entity.SyncQueue;
import org.lamisplus.modules.sync.domain.entity.Tables;
import org.lamisplus.modules.sync.service.ObjectSerializer;
import org.lamisplus.modules.sync.service.RemoteAccessTokenService;
import org.lamisplus.modules.sync.service.SyncHistoryService;
import org.lamisplus.modules.sync.utility.HttpConnectionManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/sync")
public class ClientController {
    private final ObjectSerializer objectSerializer;
    private final ObjectMapper mapper = new ObjectMapper();
    private final SyncHistoryService syncHistoryService;
    private final OrganisationUnitRepository organisationUnitRepository;
    private final RemoteAccessTokenService remoteAccessTokenService;


    @RequestMapping(value = "/upload",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @CircuitBreaker(name = "service2", fallbackMethod = "getDefaultMessage")
    @Retry(name = "retryService2", fallbackMethod = "retryFallback")
    public ResponseEntity<String> sender(@Valid @RequestBody UploadDTO uploadDTO) throws Exception {
        log.info("path: {}", uploadDTO.getServerUrl());
        /*RemoteAccessToken remoteAccessToken = remoteAccessTokenRepository.findById(uploadDTO.getRemoteAccessTokenId())
                .orElseThrow(() -> new EntityNotFoundException(RemoteAccessToken.class, "id", ""+uploadDTO.getRemoteAccessTokenId()));*/
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        System.out.println("table values: => " + Arrays.toString(Tables.values()));
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
                    System.out.println("path: " + pathVariable);
                    String url = uploadDTO.getServerUrl().concat("/api/sync/").concat(pathVariable);

                    log.info("url : {}", url);

                    byte[] bytes = mapper.writeValueAsBytes(serializeTableRecords);
                    String response = new HttpConnectionManager().post(bytes, url);

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

                        //TODO: get remote access token
                        syncHistory.setRemoteAccessTokenId(2L/*uploadDTO.getRemoteAccessTokenId()*/);
                        syncHistory.setUploadSize(serializeTableRecords.size());
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    syncHistoryService.save(syncHistory);
                }
            }
        }
        return ResponseEntity.ok("Successful");
    }

    public ResponseEntity<String> getDefaultMessage(Exception exception) {
        String message = exception.getMessage();
        if (message.contains("Failed to connect")) {
            message = "server is down kindly try again later";
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }

    public ResponseEntity<String> retryFallback(Exception exception) {
        String message = exception.getMessage();
        if (message.contains("Failed to connect")) {
            message = "server is down kindly try again later inside retry!!!";
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }

    //@GetMapping("/facilities")
    @RequestMapping(value = "/facilities",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OrganisationUnit>> getOrganisationUnitWithRecords() {
        return ResponseEntity.ok(organisationUnitRepository.findOrganisationUnitWithRecords());
    }

    //@GetMapping("/sync-history")
    @RequestMapping(value = "/sync-history",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SyncHistory>> getSyncHistory() {
        return ResponseEntity.ok(syncHistoryService.getSyncHistories());
    }

    /*@RequestMapping(value = "/remote-access-token",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> remoteAccessToken(@Valid @RequestBody RemoteAccessToken remoteAccessToken) {

        remoteAccessTokenService.save(remoteAccessToken);
        return ResponseEntity.ok("Successful");
    }*/

    //@GetMapping("/remote-urls")
    @RequestMapping(value = "/remote-urls",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RemoteUrlDTO>> getRemoteUrls() {
        return ResponseEntity.ok(remoteAccessTokenService.getRemoteUrls());
    }

    @RequestMapping(value = "/remote-access-token",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void sendToRemoteAccessToServer(@Valid @RequestBody RemoteAccessToken remoteAccessToken) {
        remoteAccessTokenService.sendToRemoteAccessToServer(remoteAccessToken);
    }
}
