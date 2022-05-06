package org.lamisplus.modules.sync.repository;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.lamisplus.modules.sync.domain.entity.SyncQueue;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SyncQueueTransactionHandlerImp implements SyncQueueTransactionHandler{


    private final LocalSessionFactoryBean localSessionFactoryBean;

    @Override
    @Transactional("syncTransactionManger")
    public SyncQueue save(SyncQueue syncQueue) {
        Session currentSession = getCurrentSession ();
        if(syncQueue.getId () == null){
         currentSession.save (syncQueue);
         return  syncQueue;
        }
        currentSession.update (syncQueue);
        return syncQueue;
    }


    @Override
    @Transactional("syncTransactionManger")
    public void delete(Long id) {
//
    }
    @NotNull
    private Session getCurrentSession() {
        return Objects.requireNonNull (localSessionFactoryBean.getObject ().getCurrentSession ());
    }
}
