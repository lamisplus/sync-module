package org.lamisplus.modules.sync.module;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.AcrossListableBeanFactory;
import com.foreach.across.core.context.ClassPathScanningCandidateModuleProvider;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.support.AcrossContextBuilder;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.hibernate.jpa.aop.JpaRepositoryInterceptor;
import com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.lamisplus.modules.sync.configurer.ApplicationProperties;
import org.lamisplus.modules.sync.configurer.ContextProvider;
import org.lamisplus.modules.sync.domain.entity.Module;
import org.lamisplus.modules.sync.domain.entity.ModuleArtifact;
import org.lamisplus.modules.sync.repository.ModuleArtifactRepository;
import org.lamisplus.modules.sync.repository.ModuleDependencyRepository;
import org.lamisplus.modules.sync.repository.ModuleRepository;
import org.lamisplus.modules.sync.yml.ModuleConfig;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.data.repository.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessageMappingInfo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.socket.messaging.WebSocketAnnotationMethodMessageHandler;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@Slf4j
@RequiredArgsConstructor
@Service
public class ModuleManager {
    private final AcrossContext parent;
    private final ChildContextsHolder contextsHolder;
    private final ModuleRepository moduleRepository;
    private final ModuleArtifactRepository moduleArtifactRepository;
    private final ModuleFileStorageService storageService;
    private final ModuleConfigProcessor moduleConfigProcessor;
    private final ModuleDependencyRepository moduleDependencyRepository;
    private final ApplicationProperties properties;
    private final ModuleDeleteService moduleService;
    private final SimpMessageSendingOperations messagingTemplate;
    private final ModuleMapModifier moduleMapModifier;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public synchronized ModuleResponse bootstrapModule(final Module module, Boolean install, Boolean multi) {
        log.debug("Bootstrap...");
        if (install == null || module.getInstallOnBoot() != null && module.getInstallOnBoot()) {
            install = true;
        }
        ModuleResponse response = new ModuleResponse();
        response.setModule(module);
        log.debug("\tStarting module {}", module.getName());

        if (isInstalled(module)) {
            log.debug("Nothing to do, module {} already installed and running", module.getName());
            response.setMessage(String.format("Nothing to do, module %s already installed and running", module.getName()));
            response.setType(ModuleResponse.Type.SUCCESS);
            return response;
        }
        module.setInError(true);
        if (!module.isNew()) {
            moduleRepository.save(module);
        }
        try {
            final Path moduleRuntimePath = Paths.get(properties.getModulePath(), "runtime", module.getName());
            if (Files.exists(moduleRuntimePath)) {
                FileUtils.deleteDirectory(moduleRuntimePath.toFile());
            }
            Optional<ModuleArtifact> moduleArtifact = Optional.empty();
            if (!module.isNew()) {
                moduleArtifact = moduleArtifactRepository.findByModule(module);
            }

            if (moduleArtifact.isPresent()) {
                byte[] data = moduleArtifact.get().getData();
                Path tmpFile = Files.createTempFile("", ".jar");
                IOUtils.copy(new ByteArrayInputStream(data), new FileOutputStream(tmpFile.toFile()));
                ModuleUtils.copyPathFromJar(tmpFile.toUri().toURL(), "/", moduleRuntimePath);
                FileUtils.deleteQuietly(tmpFile.toFile());
            } else {
                try {
                    storageService.readFile(module.getArtifact());
                } catch (FileNotFoundException e) {
                    response.setMessage(String.format("Module artifact for %s not found", module.getName()));
                    response.setType(ModuleResponse.Type.ERROR);
                    return response;
                }
                ModuleUtils.copyPathFromJar(storageService.getURL(module.getArtifact()), "/", moduleRuntimePath);
            }
            ClassLoader classLoader = ModuleLifecycle.class.getClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);

            ModuleUtils.addClassPathUrl(moduleRuntimePath.toUri().toURL(), ClassLoader.getSystemClassLoader());

        } catch (Exception e) {
            e.printStackTrace();
        }

        ClassPathScanningCandidateComponentProvider provider =
                new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(ModuleLifecycle.class));
        Set<BeanDefinition> beans = provider.findCandidateComponents(module.getBasePackage());
        for (BeanDefinition bd : beans) {
            try {
                Class<?> clz = ModuleLifecycle.class.getClassLoader().loadClass(bd.getBeanClassName());
                Object object = clz.newInstance();
                ((ModuleLifecycle) object).preInstall();
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
        removeDuplicateJpaTransactionManager();
        removeDuplicateJmxMappingEndpoint();
        removeDuplicateSimpMessage();
        //removeDuplicateErrorMapping();
        final AcrossContext[] context = new AcrossContext[1];
        try {
            final boolean[] moduleFound = {false};
            ClassPathScanningCandidateModuleProvider moduleProvider =
                    new ClassPathScanningCandidateModuleProvider(parent.getParentApplicationContext());
            moduleProvider.findCandidateModules(module.getBasePackage())
                    .forEach((mn, moduleSupplier) -> {
                        if (mn.equals(module.getName())) {
                            moduleFound[0] = true;
                            AcrossModule acrossModule = moduleSupplier.get();
                            context[0] = bootstrapAcrossModule(acrossModule);
                            removeDuplicateJpaTransactionManager();
                            removeDuplicateJmxMappingEndpoint();
                            removeDuplicateSimpMessage();
                        }
                    });
            if (context[0] == null) {
                String message = "Could not install module";
                if (!moduleFound[0]) {
                    message = "Could not install module; module class not found";
                }
                response.setMessage(message);
                response.setType(ModuleResponse.Type.ERROR);
                return response;
            }
            if (install) {
                response.setModule(persistModuleInfo(context[0], module));
            }
        } catch (Exception e) {
            log.error("Error installing module {}: {}", module.getName(), e.getMessage());
            response.setMessage(String.format("Error installing module %s: %s", module.getName(), e.getMessage()));
            response.setType(ModuleResponse.Type.ERROR);
            return response;
        }

        removeDuplicateJpaTransactionManager();
        removeDuplicateJmxMappingEndpoint();
        removeDuplicateSimpMessage();

        if (context[0] != null) {
            AcrossContextInfo contextInfo = AcrossContextUtils.getContextInfo(context[0]);
            final boolean[] error = {false};
            contextInfo.getModules().forEach(moduleInfo -> moduleInfo.getExposedBeanDefinitions()
                    .forEach((bdn, ebd) -> {
                        List<String> bdns = Arrays.asList(BeanFactoryUtils
                                .beanNamesForTypeIncludingAncestors(parent.getParentApplicationContext(),
                                        ebd.getBeanClass()));
                        bdns.forEach(bdn1 -> {
                            try {
                                detectHandlerMethods(bdn1,
                                        AcrossContextUtils.getApplicationContext(context[0]),
                                        ContextProvider.getBean(PrefixingRequestMappingHandlerMapping.class));
                            } catch (Exception e) {
                                log.error("Error installing module {}: {}", module.getName(), e.getMessage());
                                response.setMessage(String.format("Error installing module %s: %s", module.getName(), e.getMessage()));
                                response.setType(ModuleResponse.Type.ERROR);
                                error[0] = true;
                            }
                        });
                    })
            );
            if (error[0]) {
                return response;
            }
            WebSocketAnnotationMethodMessageHandler webSocketAnnotationMethodMessageHandler =
                    ContextProvider.getBean(WebSocketAnnotationMethodMessageHandler.class);
            webSocketAnnotationMethodMessageHandler.setApplicationContext(
                    AcrossContextUtils.getApplicationContext(context[0]));
            webSocketAnnotationMethodMessageHandler.afterPropertiesSet();

            contextsHolder.getContexts().put(module.getName(), context[0]);
            moduleMapModifier.modifyModuleMap(AcrossContextUtils.getApplicationContext(context[0]));
        }

        printEntityManager();
        removeDuplicateRepositories();
        log.info("Module {} successfully installed.", module.getName());
        if (multi != null && !multi) {
            messagingTemplate.convertAndSend("/topic/modules-changed", module.getName());
        }

        module.setInError(false);
        module.setInstallOnBoot(false);
        moduleRepository.save(module);
        response.setMessage(String.format("Module %s successfully installed", module.getName()));
        response.setType(ModuleResponse.Type.SUCCESS);
        return response;
    }

    private void printEntityManager() {
        Arrays.asList(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(parent.getParentApplicationContext(),
                LocalContainerEntityManagerFactoryBean.class))
                .forEach(bdn -> {
                    if (bdn.contains("@")) {
                        log.info("Removing: {}", bdn);
                        removeBean(bdn);
                    }
                });

        Arrays.asList(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(parent.getParentApplicationContext(),
                SharedEntityManagerCreator.class))
                .forEach(bdn -> {
                    if (!bdn.contains("#0")) {
                        log.info("Removing: {}", bdn);
                        removeBean(bdn);
                    }
                });

    }

    private void removeDuplicateRepositories() {
        Arrays.asList(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(parent.getParentApplicationContext(),
                Repository.class))
                .forEach(bdn -> {
                    if (bdn.contains("@")) {
                        log.info("Removing: {}", bdn);
                        removeBean(bdn);
                    }
                });

        Arrays.asList(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(parent.getParentApplicationContext(),
                EntityManager.class))
                .forEach(bdn -> {
                    if (bdn.contains("@")) {
                        log.info("Removing: {}", bdn);
                        removeBean(bdn);
                    }
                });
    }

    public ModuleResponse shutdownModule(Module module, Boolean uninstall, boolean update) {
        ModuleResponse response = new ModuleResponse();
        response.setType(ModuleResponse.Type.SUCCESS);
        response.setMessage("Module successfully shut down");
        if (uninstall == null) {
            uninstall = true;
        }
        if (!update) {
            if (!moduleDependencyRepository.findDependents(module).isEmpty()) {
                log.warn("Could not stop module {}; dependents still running", module.getName());
                response.setType(ModuleResponse.Type.ERROR);
                response.setMessage("Module is required by running module: stop dependent first");
            }
        }
        String name = module.getName();
        final AcrossContext[] context = {null};
        contextsHolder.getContexts().forEach((f, c) -> {
            if (c.getModule(name) != null) {
                context[0] = c;
            }
        });

        if (context[0] != null) {
            ClassPathScanningCandidateComponentProvider provider =
                    new ClassPathScanningCandidateComponentProvider(false);
            provider.addIncludeFilter(new AssignableTypeFilter(ModuleLifecycle.class));
            Set<BeanDefinition> beans = provider.findCandidateComponents(module.getBasePackage());
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            for (BeanDefinition bd : beans) {
                try {
                    Class<?> clz = classLoader.loadClass(bd.getBeanClassName());
                    Object object = clz.newInstance();
                    ((ModuleLifecycle) object).preUninstall();
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                }
            }
            final Path moduleRuntimePath = Paths.get(properties.getModulePath(), "runtime", module.getName());
            try {
                FileUtils.deleteDirectory(moduleRuntimePath.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }

            AcrossContextInfo contextInfo = AcrossContextUtils.getContextInfo(context[0]);

            contextInfo.getModules().forEach(this::removeBeanDefinitions);

            contextsHolder.getContexts().remove(module.getName());
            List<RequestMappingInfo> mappingInfos = new ArrayList<>();
            PrefixingRequestMappingHandlerMapping handlerMapping =
                    ContextProvider.getBean(PrefixingRequestMappingHandlerMapping.class);
            handlerMapping.getHandlerMethods()
                    .forEach((mappingInfo, handlerMethod) -> {
                        try {
                            ContextProvider.getBean(handlerMethod.getBeanType());
                        } catch (BeansException e) {
                            log.debug("Removing mapping: {}", mappingInfo);
                            mappingInfos.add(mappingInfo);
                        }
                    });
            mappingInfos.forEach(handlerMapping::unregisterMapping);

            List<SimpMessageMappingInfo> messageMappingInfos = new ArrayList<>();
            WebSocketAnnotationMethodMessageHandler webSocketAnnotationMethodMessageHandler =
                    ContextProvider.getBean(WebSocketAnnotationMethodMessageHandler.class);
            webSocketAnnotationMethodMessageHandler.getHandlerMethods()
                    .forEach((mappingInfo, handlerMethod) -> {
                        try {
                            ContextProvider.getBean(handlerMethod.getBeanType());
                        } catch (BeansException e) {
                            log.debug("Removing STOMP mapping: {}", mappingInfo);
                            messageMappingInfos.add(mappingInfo);
                        }
                    });
            messageMappingInfos
                    .forEach(simpMessageMappingInfo -> unregisterMessagingMapping(
                            webSocketAnnotationMethodMessageHandler, simpMessageMappingInfo));

            context[0] = null;

        }
        if (uninstall || update) {
            moduleRepository.findByName(name).ifPresent(moduleService::deleteModule);
        }
        messagingTemplate.convertAndSend("/topic/modules-changed", module.getName());
        return response;
    }

    private void removeDuplicateJmxMappingEndpoint() {
        Arrays.asList(BeanFactoryUtils.beanNamesForAnnotationIncludingAncestors(parent.getParentApplicationContext(),
                Endpoint.class))
                .forEach(bdn -> {
                    if (bdn.contains("@")) {
                        removeBean(bdn);
                    }
                });
    }

    private void removeDuplicateJpaTransactionManager() {
        Arrays.asList(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(parent.getParentApplicationContext(),
                JpaTransactionManager.class))
                .forEach(bdn -> {
                    if (bdn.contains("@")) {
                        removeBean(bdn);
                    }
                });

        Arrays.asList(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
                parent.getParentApplicationContext(), JpaRepositoryInterceptor.class))
                .forEach(bdn -> {
                    if (bdn.contains("@")) {
                        removeBean(bdn);
                    }
                });
    }

    private void removeDuplicateSimpMessage() {
        Arrays.asList(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(parent.getParentApplicationContext(),
                SimpMessageSendingOperations.class))
                .forEach(bdn -> {
                    if (bdn.contains("@")) {
                        removeBean(bdn);
                    }
                });

    }

    private void removeBean(String bn) {
        log.debug("Removing: {}", bn);
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) parent.getParentApplicationContext()
                .getAutowireCapableBeanFactory();
        BeanFactory bf = ((DefaultListableBeanFactory) registry).getParentBeanFactory();
        if (bf != null) {
            try {
                ((AcrossListableBeanFactory) bf).removeBeanDefinition(bn);
            } catch (Exception ignored) {
            }
        }
    }

    /*
    private void removeDuplicateErrorMapping() {
        List<RequestMappingInfo> mappingInfos = new ArrayList<>();
        PrefixingRequestMappingHandlerMapping handlerMapping = ContextProvider.getBean(PrefixingRequestMappingHandlerMapping.class);
        handlerMapping.getHandlerMethods()
            .forEach((mappingInfo, handlerMethod) -> {
                log.debug("Mapping info: {}", mappingInfo);
                if (mappingInfo.toString().contains("{[/error]") || handlerMethod.toString().contains("BasicErrorController")){
                    log.debug("Removing mapping: {}; {}", mappingInfo, handlerMethod);
                    mappingInfos.add(mappingInfo);
                }
            });
        mappingInfos.forEach(mappingInfo -> handlerMapping.unregisterMapping(mappingInfo));
    }
    */

    private DataSource getDataSource() {
        return parent.getDataSource();
    }

    private ZonedDateTime toZonedDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private void unregisterMessagingMapping(Object object, Object mappingInfo) {
        Field field = ReflectionUtils.findField(object.getClass(), "handlerMethods");
        if (field != null) {
            ReflectionUtils.makeAccessible(field);
            try {
                Map map = (Map) field.get(object);
                map.remove(mappingInfo);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        Method method = ReflectionUtils.findMethod(object.getClass(), "getDirectLookupDestinations", SimpMessageMappingInfo.class);
        if (method != null) {
            ReflectionUtils.makeAccessible(method);
            field = ReflectionUtils.findField(object.getClass(), "destinationLookup");
            if (field != null) {
                ReflectionUtils.makeAccessible(field);
                try {
                    @SuppressWarnings("unchecked")
                    Collection<String> patterns = (Collection<String>) method.invoke(object, mappingInfo);
                    for (String pattern : patterns) {
                        Map map = (Map) field.get(object);
                        map.remove(pattern);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void detectHandlerMethods(Object handler, ApplicationContext applicationContext,
                                      RequestMappingHandlerMapping handlerMapping) {
        Class<?> handlerType = (handler instanceof String ?
                applicationContext.getType((String) handler) : handler.getClass());
        if (handlerType != null) {
            Class<?> userType = ClassUtils.getUserClass(handlerType);
            Map<Method, RequestMappingInfo> methods = MethodIntrospector.selectMethods(userType,
                    (MethodIntrospector.MetadataLookup<RequestMappingInfo>) method -> {
                        try {
                            for (Method m : handlerMapping.getClass().getDeclaredMethods()) {
                                if (m.getName().contains("getMappingForMethod")) {
                                    ReflectionUtils.makeAccessible(m);
                                    return (RequestMappingInfo) m.invoke(handlerMapping, method, userType);
                                }
                            }
                            return null;
                        } catch (Throwable ex) {
                            throw new IllegalStateException("Invalid mapping on  handler class [" +
                                    userType.getName() + "]: " + method, ex);
                        }
                    });
            methods.forEach((method, mapping) -> {
                Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
                if (!handler.toString().contains("basicErrorController") && mapping != null) {
                    handlerMapping.registerMapping(mapping, ContextProvider.getBean(handler.toString()), invocableMethod);
                }
            });
        }
    }

    public boolean isInstalled(Module module) {
        return contextsHolder.getContexts().values().stream()
                .flatMap(ctx -> ctx.getModules().stream())
                .anyMatch(m -> m.getName().equals(module.getName()));
    }

    public ModuleConfig getModuleConfig(Module module) {
        List<ModuleConfig> moduleConfigs = new ArrayList<>();
        try {
            ModuleUtils.loadModuleConfig(storageService.readFile(module.getArtifact()), "module.yml", moduleConfigs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return !moduleConfigs.isEmpty() ? moduleConfigs.get(0) : null;
    }

    private void removeBeanDefinitions(AcrossModuleInfo moduleInfo) {
        moduleInfo.getExposedBeanDefinitions().forEach((bdn, ebd) -> {
            List<String> bdns = Arrays.asList(BeanFactoryUtils
                    .beanNamesForTypeIncludingAncestors(parent.getParentApplicationContext(),
                            ebd.getBeanClass()));
            if (bdns.size() > 1) {
                String bn = "";
                for (String s : bdns) {
                    if (s.contains(moduleInfo.getName())) {
                        bn = s;
                        break;
                    }
                }
                if (StringUtils.isNotBlank(bn)) {
                    removeBean(bn);
                }
            } else if (bdns.size() == 1) {
                removeBean(bdns.get(0));
            }
        });
    }

    private Module persistModuleInfo(AcrossContext context, Module module) {
        Collection<AcrossModule> modules = context.getModules();
        final Module[] savedModule = {module};
        modules.stream()
                .filter(acrossModule -> acrossModule.getName().equals(module.getName()))
                .forEach(acrossModule -> {
                    VersionInfo versionInfo = getVersionInfo(acrossModule);
                    module.setVersion(versionInfo.getVersion());
                    module.setDescription(versionInfo.getProjectName());
                    module.setBuildTime(toZonedDateTime(versionInfo.getBuildTime()));
                    ModuleConfig moduleConfig = getModuleConfig(module);
                    if (moduleConfig != null) {
                        savedModule[0] = moduleRepository.save(module);
                        if (!module.isNew()) {
                            jdbcTemplate.update("delete from module_dependencies where module_id = ?", module.getId());
                        }
                        moduleConfigProcessor.processModuleConfig(moduleConfig, savedModule[0]);

                        if (moduleConfig.isStore()) {
                            Optional<ModuleArtifact> moduleArtifact = Optional.empty();
                            if (!module.isNew()) {
                                moduleArtifact = moduleArtifactRepository.findByModule(module);
                            }
                            if (!moduleArtifact.isPresent()) {
                                ModuleArtifact artifact = new ModuleArtifact();
                                artifact.setModule(savedModule[0]);
                                try {
                                    InputStream stream = storageService.readFile(savedModule[0].getArtifact());
                                    byte[] data = IOUtils.toByteArray(stream);
                                    artifact.setData(data);
                                    moduleArtifactRepository.save(artifact);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
        return savedModule[0];
    }

    private AcrossContext bootstrapAcrossModule(AcrossModule acrossModule) {
        AcrossContext context;
        DataSource dataSource = getDataSource();
        acrossModule.addRuntimeDependency(AcrossHibernateJpaModule.NAME);
        context = new AcrossContextBuilder()
                .applicationContext(parent.getParentApplicationContext())
                .dataSource(dataSource)
                .installerDataSource(dataSource)
                .modules(acrossModule)
                .build();
        try {
            log.info("Bootstrapping...");
            context.bootstrap();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return context;
    }

    private VersionInfo getVersionInfo(AcrossModule clazz) {
        Class<?> c = ClassUtils.getUserClass(clazz);

        VersionInfo versionInfo = VersionInfo.UNKNOWN;

        // Retrieve the manifest
        String className = c.getSimpleName() + ".class";
        String classPath = c.getResource(className).toString();

        if (classPath.contains("file:/")) {
            String manifestPath = classPath.replace("/" + c.getName().replace(".", "/") + ".class",
                    "/META-INF/MANIFEST.MF");

            try (InputStream is = new URL(manifestPath).openStream()) {
                Manifest manifest = new Manifest(is);
                Attributes attr = manifest.getMainAttributes();

                versionInfo = new VersionInfo();
                versionInfo.manifest = manifest;
                versionInfo.available = true;
                versionInfo.projectName = StringUtils.defaultString(
                        attr.getValue("Implementation-Title"), VersionInfo.UNKNOWN_VALUE
                );
                versionInfo.version = StringUtils.defaultString(
                        attr.getValue("Implementation-Version"), VersionInfo.UNKNOWN_VALUE
                );

                String buildTime = attr.getValue("Build-Time");

                if (buildTime != null) {
                    try {
                        versionInfo.buildTime = DateUtils.parseDate(buildTime, "yyyyMMdd-HHmm",
                                "yyyy-MM-dd'T'HH:mm:ss'Z'");
                    } catch (ParseException pe) {
                        log.error(
                                "Manifest {} specifies Build-Time attribute with value {}, but not in the expected format of yyyyMMdd-HHmm",
                                manifestPath, buildTime);
                    }
                }
            } catch (IOException ioe) {
                log.warn("No MANIFEST.MF found at {}", manifestPath);
            }
        }

        return versionInfo;
    }

    @Data
    public static class VersionInfo {
        public static final VersionInfo UNKNOWN = new VersionInfo();

        public static final String UNKNOWN_VALUE = "unknown";

        public Manifest manifest;
        public Date buildTime;
        public String version = UNKNOWN_VALUE;
        public String projectName = UNKNOWN_VALUE;
        public boolean available;
    }
}
