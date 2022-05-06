package org.lamisplus.modules.sync.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.sync.domain.entity.SyncQueue;
import org.lamisplus.modules.sync.repository.SyncQueueTransactionHandler;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.lamisplus.modules.sync.repository.SyncQueueRepository;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/sync")
public class ClientController {
    private final SyncQueueRepository syncQueueRepository;

    private  final SyncQueueTransactionHandler syncTransactionHandler;


    @RequestMapping(value = "/sync-queue",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SyncQueue>> getAllSyncQueue() {
        try {
            List<SyncQueue> syncQueues = syncQueueRepository.findAll();
            return ResponseEntity.ok(syncQueues);
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    @RequestMapping(value = "/sync-queue",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SyncQueue> save(@Valid @RequestBody SyncQueue syncQueue) {
        return ResponseEntity.ok(syncTransactionHandler.save(syncQueue));
    }

}
