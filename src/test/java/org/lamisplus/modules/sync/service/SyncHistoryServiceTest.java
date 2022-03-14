package org.lamisplus.modules.sync.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lamisplus.modules.base.repository.OrganisationUnitRepository;
import org.lamisplus.modules.sync.domain.entity.SyncHistory;
import org.lamisplus.modules.sync.repo.RemoteAccessTokenRepository;
import org.lamisplus.modules.sync.repo.SyncHistoryRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

class SyncHistoryServiceTest {
    @Mock
    SyncHistoryRepository syncHistoryRepository;
    @Mock
    RemoteAccessTokenRepository remoteAccessTokenRepository;
    @Mock
    OrganisationUnitRepository organisationUnitRepository;
    @Mock
    Logger log;
    @InjectMocks
    SyncHistoryService syncHistoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testSave() {
        when(syncHistoryRepository.save(any())).thenReturn(0);

        syncHistoryService.save(new SyncHistory());
    }

    @Test
    void testGetSyncHistory() {
        when(syncHistoryRepository.findByTableNameAndOrganisationUnitId(anyString(), anyLong())).thenReturn(java.util.Optional.of(new SyncHistory()));

        SyncHistory result = syncHistoryService.getSyncHistory("table", Long.valueOf(1));
        Assertions.assertEquals(new SyncHistory(), result);
    }

    @Test
    void testGetSyncHistories() {
        List<SyncHistory> syncHistories = new ArrayList<>();
        when(syncHistoryRepository.findSyncHistories()).thenReturn(syncHistories);

        List<SyncHistory> result = syncHistoryService.getSyncHistories();
        Assertions.assertEquals(new ArrayList<SyncHistory>(), result);
    }

    @Test
    void testGetSyncQueueIdForClient() {
        when(syncHistoryRepository.findSyncQueueByProcessed(anyInt())).thenReturn(new ArrayList<SyncHistory>());
        when(syncHistoryRepository.saveAll(any())).thenReturn(0);
        when(remoteAccessTokenRepository.findById(anyLong())).thenReturn(null);

        syncHistoryService.getSyncQueueIdForClient();
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme