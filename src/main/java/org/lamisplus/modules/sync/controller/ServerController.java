package org.lamisplus.modules.sync.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.sync.domain.entity.SyncQueue;
import org.lamisplus.modules.sync.service.ServerRemoteAccessTokenService;
import org.lamisplus.modules.sync.service.SyncQueueService;
import org.lamisplus.modules.sync.service.SyncServerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/sync")
public class ServerController {
    private final SyncQueueService syncQueueService;
    private final ServerRemoteAccessTokenService serverRemoteAccessTokenService;
    private final SyncServerService syncServerService;
    private final SimpMessageSendingOperations messagingTemplate;



    @PostMapping("/{table}/{facilityId}/{name}")
    @CircuitBreaker(name = "server2", fallbackMethod = "getReceiverDefault")
    public ResponseEntity<SyncQueue> receiver(
            @RequestBody byte[] bytes,
            @RequestHeader("Hash-Value") String hash,
            @RequestHeader("token") String token,
            @PathVariable String table,
            @PathVariable Long facilityId,
            @PathVariable String name) throws Exception {

        SyncQueue syncQueue = syncServerService.save(bytes, hash, table, facilityId, name);
        messagingTemplate.convertAndSend("/topic/sync", 3);
        return ResponseEntity.ok(syncQueue);
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
    public ResponseEntity<?> save(@Valid @RequestBody byte[] bytes) throws Exception {

        try {
            return ResponseEntity.ok(serverRemoteAccessTokenService.save(bytes));
        }catch (Exception e){
            return new ResponseEntity<>("Error!, Please try again", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
