package org.lamisplus.modules.sync.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.sync.domain.entity.RemoteAccessToken;
import org.lamisplus.modules.sync.domain.entity.SyncQueue;
import org.lamisplus.modules.sync.service.RemoteAccessTokenService;
import org.lamisplus.modules.sync.service.SyncQueueService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/sync")
public class ServerController {
    private final SyncQueueService syncQueueService;
    private final RemoteAccessTokenService remoteAccessTokenService;


    @PostMapping("/{table}/{facilityId}")
    @CircuitBreaker(name = "server2", fallbackMethod = "getReceiverDefault")
    public ResponseEntity<SyncQueue> receiver(
            @RequestBody byte[] bytes,
            @RequestHeader("Hash-Value") String hash,
            @RequestHeader("token") String token,
            @PathVariable String table,
            @PathVariable Long facilityId) throws Exception {

        SyncQueue syncQueue = syncQueueService.save(bytes, hash, table, facilityId);
        return ResponseEntity.ok(syncQueue);
    }

    public ResponseEntity<String> getReceiverDefault(Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @RequestMapping(value = "/sync-queue/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SyncQueue> getSyncQueue(@PathVariable Long id){
        return ResponseEntity.ok(syncQueueService.getAllSyncQueueById(id));
    }

    @RequestMapping(value = "/server/remote-access-token",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @SneakyThrows
    public ResponseEntity<RemoteAccessToken> save(@Valid @RequestBody byte[] bytes) {

        return ResponseEntity.ok(remoteAccessTokenService.save(bytes));
    }
}
