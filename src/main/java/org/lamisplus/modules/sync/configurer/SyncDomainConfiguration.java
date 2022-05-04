package org.lamisplus.modules.sync.configurer;


import com.foreach.across.modules.hibernate.jpa.repositories.config.EnableAcrossJpaRepositories;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableAcrossJpaRepositories(transactionManagerRef = "syncJpaTransactionManager",
        entityManagerFactoryRef = "syncJpaEntityManagerFactory",
        basePackages = {"org.lamisplus.modules.sync.repository"})
public class SyncDomainConfiguration {
}
