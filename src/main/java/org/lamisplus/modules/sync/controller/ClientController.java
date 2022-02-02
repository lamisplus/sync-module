package org.lamisplus.modules.sync.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.sync.domain.dto.RemoteUrlDTO;
import org.lamisplus.modules.sync.domain.dto.UploadDto;
import org.lamisplus.modules.sync.domain.entity.OrganisationUnit;
import org.lamisplus.modules.sync.domain.entity.RemoteAccessToken;
import org.lamisplus.modules.sync.domain.entity.SyncHistory;
import org.lamisplus.modules.sync.domain.entity.Tables;
import org.lamisplus.modules.sync.repository.OrganisationUnitRepository;
import org.lamisplus.modules.sync.service.ObjectSerializer;
import org.lamisplus.modules.sync.service.RemoteAccessTokenService;
import org.lamisplus.modules.sync.service.SyncHistoryService;
import org.lamisplus.modules.sync.utility.HttpConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/sync")
public class ClientController {
    private static final String UPLOAD = "upload";
    private final ObjectSerializer objectSerializer;
    private final ObjectMapper mapper = new ObjectMapper();
    private final SyncHistoryService syncHistoryService;
    private final OrganisationUnitRepository organisationUnitRepository;
    private final RemoteAccessTokenService remoteAccessTokenService;

    // @Value("${remote.lamis.url}")
    // private String SERVER_URL;
    @RequestMapping(value = "/upload",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @CircuitBreaker(name = "service2", fallbackMethod = "getDefaultMessage")
    @Retry(name = "retryService2", fallbackMethod = "retryFallback")
    public ResponseEntity<String> sender(@RequestBody UploadDto uploadDto) throws Exception {
        System.out.println("path: " + uploadDto.getServerUrl());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        System.out.println("table values: => " + Arrays.toString(Tables.values()));
        for (Tables table : Tables.values()) {
            SyncHistory syncHistory = syncHistoryService.getSyncHistory(table.name(), uploadDto.getFacilityId());
            LocalDateTime dateLastSync = syncHistory.getDateLastSync();
            log.info("last date sync 1 {}", dateLastSync);
            List<?> serializeTableRecords = objectSerializer.serialize(table, uploadDto.getFacilityId(), dateLastSync);
            if (!serializeTableRecords.isEmpty()) {
                Object serializeObjet = serializeTableRecords.get(0);

//              log.info("serialize first  object  {} ", serializeObjet.toString());
                log.info("object size:  {} ", serializeTableRecords.size());
                if (!serializeObjet.toString().contains("No table records was retrieved for server sync")) {
                    String pathVariable = table.name().concat("/").concat(Long.toString(uploadDto.getFacilityId()));
                    System.out.println("path: " + pathVariable);
                    String url = uploadDto.getServerUrl().concat("/api/sync/").concat(pathVariable);

                    log.info("url : {}", url);

                    byte[] bytes = mapper.writeValueAsBytes(serializeTableRecords);
//                  System.out.println("output: "+bytes);
                    String response = new HttpConnectionManager().post(bytes, url);
                    System.out.println("==>: " + response);
                    log.info("Done : {}", response);
                    syncHistory.setTableName(table.name());
                    syncHistory.setOrganisationUnitId(uploadDto.getFacilityId());
                    syncHistory.setDateLastSync(LocalDateTime.now());
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
        return ResponseEntity.internalServerError().body(message);
    }

    public ResponseEntity<String> retryFallback(Exception exception) {
        String message = exception.getMessage();
        if (message.contains("Failed to connect")) {
            message = "server is down kindly try again later inside retry!!!";
        }
        return ResponseEntity.internalServerError().body(message);
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

    @RequestMapping(value = "/remote-access-token",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> remoteAccessToken(@RequestBody RemoteAccessToken remoteAccessToken) {
        remoteAccessTokenService.save(remoteAccessToken);
        return ResponseEntity.ok("Successful");
    }

    //@GetMapping("/remote-urls")
    @RequestMapping(value = "/remote-urls",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RemoteUrlDTO>> getRemoteUrls() {
        return ResponseEntity.ok(remoteAccessTokenService.getRemoteUrls());
    }

}
