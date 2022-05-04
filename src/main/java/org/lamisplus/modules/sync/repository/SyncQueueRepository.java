package org.lamisplus.modules.sync.repository;

import org.lamisplus.modules.sync.domain.entity.SyncQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SyncQueueRepository extends JpaRepository<SyncQueue, Long>, JpaSpecificationExecutor {

}
