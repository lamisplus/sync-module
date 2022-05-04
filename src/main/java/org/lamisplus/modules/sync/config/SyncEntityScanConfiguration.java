package org.lamisplus.modules.sync.config;

import com.foreach.across.core.annotations.ModuleConfiguration;
import com.foreach.across.modules.hibernate.provider.HibernatePackageConfigurer;
import com.foreach.across.modules.hibernate.provider.HibernatePackageRegistry;
import org.lamisplus.modules.sync.domain.entity.MyEntity;
import org.lamisplus.modules.sync.domain.entity.SyncDomain;
import org.lamisplus.modules.sync.domain.entity.SyncQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@ModuleConfiguration("SyncJpaModule")
@EntityScan(basePackageClasses = SyncDomain.class)
public class SyncEntityScanConfiguration implements HibernatePackageConfigurer {
    private static final Logger log = LoggerFactory.getLogger(org.lamisplus.modules.sync.config.SyncEntityScanConfiguration.class);

    public SyncEntityScanConfiguration() {
    }

    public void configureHibernatePackage(HibernatePackageRegistry hibernatePackageRegistry) {
        hibernatePackageRegistry.addPackageToScan(SyncDomain.class);
    }
}
