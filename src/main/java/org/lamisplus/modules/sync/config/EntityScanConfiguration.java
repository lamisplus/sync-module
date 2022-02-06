package org.lamisplus.modules.sync.config;

import com.foreach.across.core.annotations.ModuleConfiguration;
import com.foreach.across.modules.hibernate.provider.HibernatePackageConfigurer;
import com.foreach.across.modules.hibernate.provider.HibernatePackageRegistry;
import org.lamisplus.modules.base.domain.BaseDomain;
import org.lamisplus.modules.sync.domain.SyncDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ModuleConfiguration({"AcrossHibernateJpaModule"})
public class EntityScanConfiguration implements HibernatePackageConfigurer {
    private static final Logger log = LoggerFactory.getLogger(org.lamisplus.modules.sync.config.EntityScanConfiguration.class);

    public EntityScanConfiguration() {
    }

    public void configureHibernatePackage(HibernatePackageRegistry hibernatePackageRegistry) {
        hibernatePackageRegistry.addPackageToScan(SyncDomain.class, BaseDomain.class);
    }
}
