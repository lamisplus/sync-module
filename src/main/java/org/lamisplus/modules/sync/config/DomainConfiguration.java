package org.lamisplus.modules.sync.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor
@EnableJpaRepositories(basePackages = "org.lamisplus.modules.sync.repository")
@Slf4j
@EnableTransactionManagement
public class DomainConfiguration {

    private final DataSource dataSource;

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan("org.lamisplus.modules.sync");
        sessionFactory.setHibernateProperties (hibernateProperties ());
        return sessionFactory;
    }

    @Bean(name = "syncTransactionManger")
    @Primary
    public PlatformTransactionManager hibernateTransactionManager() {
        HibernateTransactionManager transactionManager = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory().getObject());
        return transactionManager;
    }

    @Bean
    public EntityManager entityManager(List<EntityManager> entityManagers) {
        log.info ("entity manager BaseEntity {}", entityManagers.get (0).toString ());
        return entityManagers.get (0);
    }
    private final Properties hibernateProperties() {
        Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.ddl-auto", "update");
        hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        return hibernateProperties;
    }
}
