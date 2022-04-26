package org.lamisplus.modules.sync;

import com.foreach.across.config.AcrossApplication;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.web.AcrossWebModule;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.base.BaseModule;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@EnableScheduling
@AcrossApplication(
        modules = {
                AcrossHibernateJpaModule.NAME,
                AcrossWebModule.NAME, BaseModule.NAME
        },
        modulePackageClasses = {BaseModule.class})
@Slf4j
@EnableSwagger2
public class SyncApplication extends AcrossModule {

    public static void main(String[] args) {
        SpringApplication.run(SyncApplication.class, args);
    }

    public static final String NAME = "SyncModule";

    public static String modulePath = System.getProperty("user.dir");


    public SyncApplication() {
        super();
        addApplicationContextConfigurer(new ComponentScanConfigurer(
                getClass().getPackage().getName() +".controller",
                getClass().getPackage().getName() +".service",
                getClass().getPackage().getName() +".config",
                getClass().getPackage().getName() +".domain",
                getClass().getPackage().getName() +".domain.mapper",
                getClass().getPackage().getName() +".installers",
                getClass().getPackage().getName() +".utility",
                getClass().getPackage().getName() +".component",
                getClass().getPackage().getName() +".repo"));
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

    @Bean
    //Reads database properties from the config.yml
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new FileSystemResource(modulePath + File.separator +"config.yml"));
        propertySourcesPlaceholderConfigurer.setProperties(yaml.getObject());
        propertySourcesPlaceholderConfigurer.setIgnoreResourceNotFound(true);
        return propertySourcesPlaceholderConfigurer;
    }

}