package org.lamisplus.modules.sync;

import com.foreach.across.config.AcrossApplication;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.web.AcrossWebModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.io.File;
import java.util.Arrays;
import java.util.List;

@EnableScheduling
@AcrossApplication(
        modules = {
                AcrossWebModule.NAME
        },
        excludeAutoConfigurations = {DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class } )
@Slf4j
@EnableSwagger2
public class SyncApplication extends AcrossModule {

    public static String modulePath = System.getProperty("user.dir");
    @Value("${sync.datasource.driver-class-name}")
    private String driverClassName;
    @Value("${sync.datasource.url}")
    private String url;
    @Value("${sync.datasource.username}")
    private String username;
    @Value("${sync.datasource.password}")
    private String password;


    public static void main(String[] args) {
        SpringApplication.run(SyncApplication.class, args);
    }

    public static final String NAME = "SyncModule";

    public SyncApplication() {
        super();
        addApplicationContextConfigurer(new ComponentScanConfigurer(
                getClass().getPackage().getName() +".controller",
                getClass().getPackage().getName() +".service",
                getClass().getPackage().getName() +".configurer",
                getClass().getPackage().getName() +".module",
                getClass().getPackage().getName() +".domain",
                getClass().getPackage().getName() +".domain.mapper",
                getClass().getPackage().getName() +".installers",
                getClass().getPackage().getName() +".util",
                getClass().getPackage().getName() +".component",
                "org.springframework.web.socket",
                getClass().getPackage().getName() +".repository"));
    }

    public String getName() {
        return NAME;
    }

    /*
     * Provides sensible defaults and convenience methods for configuration.
     * @return a Docket
     */

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .securityContexts(Arrays.asList(securityContext()))
                .securitySchemes(Arrays.asList(apiKey()))
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

    /*
     *
     * @return ApiInfo for documentation
     */

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Lamisplus")
                .description("Lamisplus Application Api Documentation")
                .license("Apache 2.0")
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
                .termsOfServiceUrl("http://swagger.io/terms/")
                .version("1.0.0").contact(new Contact("Development Team","http://lamisplus.org/base-module", "info@lamisplus.org"))
                .build();
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder().securityReferences(defaultAuth()).build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Arrays.asList(new SecurityReference("JWT", authorizationScopes));
    }

    /*
     * @Param name
     * @Param keyName
     * @Param passAs
     * @return ApiKey
     * Sending Authorization:
     */
    private ApiKey apiKey() {
        return new ApiKey("JWT", "Authorization", "header");
    }


    //runtime JPA module and datasource
    @Bean
    @Primary
    @ConfigurationProperties("org.lamisplus.modules.sync")
    public DataSourceProperties syncDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("org.lamisplus.modules.sync")
    public DataSource syncDataSource() {
        return syncDataSourceProperties().initializeDataSourceBuilder()
                .driverClassName(driverClassName)
                .password(password)
                .url(url)
                .username(username).build();
    }

    @Bean(name = "syncJpaEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(syncDataSource());
        em.setPackagesToScan(new String[]{"org.lamisplus.modules.sync.domain.entity"});
        em.setPersistenceUnitName("sync");
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        //em.setJpaProperties();

        return em;
    }

    @Bean(name = "syncJpaTransactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier(value = "syncJpaEntityManagerFactory") EntityManagerFactory syncJpaEntityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(syncJpaEntityManagerFactory);

        return transactionManager;
    }

    @Bean(name = "syncJpaExceptionTranslation")
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    @Bean
    public AcrossHibernateJpaModule syncJpaModule() {
        return AcrossHibernateJpaModule.builder().prefix( "sync" ).build();
    }

    /*@Bean
    public DataSource syncDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUsername("postgres");
        dataSource.setUrl("jdbc:postgresql://localhost:5432/lamis3-plus-liquibase-test");
        dataSource.setPassword("emeka");
        dataSource.setDriverClassName("org.postgresql.Driver");
        return dataSource;
    }

    @Bean
    public AcrossHibernateJpaModule syncJpaModule() {
        return AcrossHibernateJpaModule.builder().prefix( "sync" ).build();
    }*/

    /*@Bean
    //Reads database properties from the config.yml
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new FileSystemResource(modulePath + File.separator +"config.yml"));
        propertySourcesPlaceholderConfigurer.setProperties(yaml.getObject());
        propertySourcesPlaceholderConfigurer.setIgnoreResourceNotFound(true);
        return propertySourcesPlaceholderConfigurer;
    }*/

}