package org.lamisplus.modules.sync.config;

import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModuleSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class HibernateModuleSetting extends AcrossHibernateJpaModuleSettings {


    @Override
    public void setPrimary(Boolean primary) {
        super.setPrimary (true);
    }

    @Override
    public String getPersistenceUnitName() {
        String persistenceUnitName = super.getPersistenceUnitName ();
        log.info ("persistenceUnitName {} " , persistenceUnitName);
        return persistenceUnitName;
    }

    @Override
    public String getDataSource() {
        String dataSource = super.getDataSource ();
        log.info ("data Source : {} ", dataSource);
        return dataSource;

    }


    @Override
    public Boolean getPrimary() {
        Boolean primary = super.getPrimary ();
        log.info ("primary for Sync {}", primary );
        return primary;
    }


}