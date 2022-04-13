package org.lamisplus.modules.sync.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
import org.lamisplus.modules.sync.repo.RemoteAccessTokenRepository;
//import org.lamisplus.modules.sync.service.ObjectSerializer;
import org.lamisplus.modules.sync.service.ObjectSerializer;
import org.lamisplus.modules.sync.service.RemoteAccessTokenService;
import org.lamisplus.modules.sync.service.SyncClientService;
import org.lamisplus.modules.sync.service.SyncHistoryService;
import org.lamisplus.modules.sync.utility.HttpConnectionManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/sync")
public class ClientController {
    private final SyncHistoryService syncHistoryService;
    private final OrganisationUnitRepository organisationUnitRepository;
    private final RemoteAccessTokenService remoteAccessTokenService;
    private final SyncClientService syncClientService;


    @RequestMapping(value = "/upload",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    //@CircuitBreaker(name = "service2", fallbackMethod = "getDefaultMessage")
    //@Retry(name = "retryService2", fallbackMethod = "retryFallback")
    public @ResponseBody ResponseEntity sender(@Valid @RequestBody UploadDTO uploadDTO) throws Exception {
        try {
            syncClientService.sender(uploadDTO);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch(final Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    //@GetMapping("/facilities")
    @RequestMapping(value = "/facilities",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OrganisationUnit>> getOrganisationUnitWithRecords() {
        return ResponseEntity.ok(organisationUnitRepository.findOrganisationUnitWithRecords());
    }

    @RequestMapping(value = "/sync-history",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SyncHistory>> getSyncHistory() {
        return ResponseEntity.ok(syncHistoryService.getSyncHistories());
    }


    @RequestMapping(value = "/remote-urls",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RemoteUrlDTO>> getRemoteUrls() {
        return ResponseEntity.ok(remoteAccessTokenService.getRemoteUrls());
    }

    @RequestMapping(value = "/remote-access-token",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @SneakyThrows
    public void sendToRemoteAccessToServer(@Valid @RequestBody RemoteAccessToken remoteAccessToken) {
        remoteAccessTokenService.sendToRemoteAccessToServer(remoteAccessToken);
    }
}
