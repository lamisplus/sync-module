package org.lamisplus.modules.sync.controller;

import io.github.jhipster.web.util.ResponseUtil;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.sync.controller.apierror.EntityNotFoundException;
import org.lamisplus.modules.sync.domain.dto.MenuDTO;
import org.lamisplus.modules.sync.domain.dto.ModuleMenuDTO;
import org.lamisplus.modules.sync.domain.entity.Menu;
import org.lamisplus.modules.sync.domain.entity.Module;
import org.lamisplus.modules.sync.domain.entity.WebModule;
import org.lamisplus.modules.sync.domain.mapper.MenuMapper;
import org.lamisplus.modules.sync.module.ModuleResponse;
import org.lamisplus.modules.sync.module.ModuleService;
import org.lamisplus.modules.sync.repository.MenuRepository;
import org.lamisplus.modules.sync.repository.ModuleRepository;
import org.lamisplus.modules.sync.repository.WebModuleRepository;
import org.lamisplus.modules.sync.service.MenuService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class ModuleController {
    private final WebModuleRepository webModuleRepository;
    private final MenuRepository menuRepository;
    private final ModuleRepository moduleRepository;
    private final ModuleService moduleService;
    private final MenuMapper menuMapper;
    private final MenuService menuService;


    @GetMapping("/modules/{id:\\d+}/web-modules")
    @Timed
    //@PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public List<WebModule> getModulesByType(@PathVariable("id") Long id) {
        log.debug("Getting web modules for module: {}", id);

        Module module = moduleRepository.findById(id).orElse(null);
        if (module != null) {
            return webModuleRepository.findByModule(module);
        }
        return new ArrayList<>();
    }

    @GetMapping("/modules/{id:\\d+}/menus")
    @Timed
    //@PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public List<MenuDTO> getModulesMenu(@PathVariable("id") Long id) {
        log.debug("Getting menus for module: {}", id);
        Module module = new Module();
        if(id != 0) {
            module = moduleRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(Module.class, "id", "" + id));
        } else {
            module.setId(null);
        }
        return menuRepository.findByModuleId(module.getId()).stream().
                map(menu -> {
                    MenuDTO menuDTO =  menuMapper.toMenuDTO(menu);
                    Menu parent = menu.getParent();
                    if(parent != null)menuDTO.setParentName(parent.getName());
                    return menuDTO;
                }).collect(Collectors.toList());
    }


    @GetMapping("/modules/{id:\\d+}")
    @Timed
    public ResponseEntity<Module> getModule(@PathVariable("id") Long id) {
        log.debug("Getting module: {}", id);

        Optional<Module> module = moduleRepository.findById(id);
        module = module.map(m -> {
            m.setWebModules(null);
            return m;
        });
        return ResponseUtil.wrapOrNotFound(module);
    }

    @GetMapping("/modules")
    @Timed
    public List<Module> getWebModules() {
        log.debug("Getting all active modules");

        List<Module> providers = new LinkedList<>(moduleRepository.findAllWithProviders());
        providers.addAll(moduleRepository.findAllWithoutProviders());
        providers = providers.stream()
                .map(module -> {
                    List<WebModule> webModules = webModuleRepository.findByModule(module);
                    module.setWebModules(new HashSet<>(webModules));
                    return module;
                }).collect(Collectors.toList());

        return providers;
    }

    @PostMapping("/modules/activate")
    public ModuleResponse activateModule(@RequestBody Module module) {
        return moduleService.activate(module);
    }

    @PostMapping("/modules/deactivate")
    public ModuleResponse deactivateModule(@RequestBody Module module) {
        return moduleService.deactivate(module);
    }

    @PostMapping("/modules/uninstall")
    public ModuleResponse shutdownModule(@RequestBody Module module, Boolean uninstall) {
        return moduleService.uninstall(module, uninstall);
    }

    @PostMapping("/modules/update")
    public ModuleResponse updateModule(@RequestBody Module module) {
        return moduleService.update(module, false);
    }

    @PostMapping("/modules/upload")
    public Module uploadModuleData(@RequestParam("file") MultipartFile file) {
        return moduleService.uploadModuleData(file);
    }

    @PostMapping("/modules/install")
    public ModuleResponse installModule(final @RequestBody Module module, @RequestParam Boolean install) {
        return moduleService.installModule(module, install, false);
    }

    @GetMapping("/modules/menus")
    public List<Menu> getMenus() {
        log.debug("Getting all menus for current user");

        List<Menu> menuItems = new ArrayList<>();
        /*Menu menu = new Menu();
        menu.setName("Dashboard");
        menu.setState("dashboard");
        menu.setType(MenuType.ICON);
        menu.setTooltip("Dashboard");
        menu.setIcon("dashboard");
        menuItems.add(menu);*/

        /*menu = new Menu();
        menu.setType(MenuType.SEPARATOR);
        menu.setName("Main Items");
        menuItems.add(menu);*/

       /* menu = new Menu();
        menu.setName("Dashboard");
        menu.setState("dashboard");
        menu.setType(MenuType.LINK);
        menu.setTooltip("Dashboard");
        menu.setIcon("dashboard");
        menuItems.add(menu);*/

        /*Menu admin = new Menu();
        admin.setName("Administration");
        admin.setState("admin");
        admin.setType(MenuType.DROP_DOWN);
        admin.setIcon("settings");
        admin.getAuthorities().add("ROLE_ADMIN");
        admin.setPosition(100);*/

        /*Menu mm = new Menu();
        mm.setName("Modules");
        mm.setPosition(10);
        mm.setState("modules");
        admin.getSubs().add(mm);*/

        /*mm = new Menu();
        mm.setName("System Configuration");
        mm.setPosition(11);
        mm.setState("configuration");
        admin.getSubs().add(mm);*/

        /*mm = new Menu();
        mm.setName("User Management");
        mm.setPosition(10);
        mm.setState("users");
        admin.getSubs().add(mm);*/

        /*mm = new Menu();
        mm.setName("Health Checks");
        mm.setPosition(12);
        mm.setState("health");
        admin.getSubs().add(mm);*/

        /*mm = new Menu();
        mm.setName("Application Metrics");
        mm.setPosition(13);
        mm.setState("metrics");
        admin.getSubs().add(mm);*/

        /*mm = new Menu();
        mm.setName("Log Configurations");
        mm.setPosition(14);
        mm.setState("logs");
        admin.getSubs().add(mm);*/

        Set<Module> modules = new HashSet<>(moduleRepository.findByActiveIsTrueAndInErrorIsFalse());
        Set<Menu> menusL1 = new HashSet<>();
        modules.forEach(module -> menusL1.addAll(menuRepository.findByModuleAndLevel(module, "LEVEL_1").stream()
                .map(menu1 -> {
                    Set<Menu> menuL2 = new TreeSet<>(menuRepository.findByLevelAndParentName("LEVEL_2", menu1.getName()));
                    menuL2 = menuL2.stream()
                            .map(menu2 -> {
                                menu1.setType("DROP_DOWN");
                                Set<Menu> menuL3 = new TreeSet<>(menuRepository.findByLevelAndParentName("LEVEL_3", menu2.getName()));
                                menu2.setSubs(new HashSet<>(menuL3));
                                if (!menuL3.isEmpty()) {
                                    menu2.setType("DROP_DOWN");
                                }
                                return menu2;
                            })
                            .sorted(Comparator.comparing(Menu::getName))
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    /*if (!menu1.getName().equals("Administration")) {
                        menu1.setSubs(new HashSet<>(menuL2));
                    } else {
                        Set<Menu> adminSub = admin.getSubs();
                        adminSub.addAll(menuL2);
                        admin.setSubs(adminSub);
                    }*/
                    return menu1;
                })
                //.filter(menu1 -> !menu1.getName().equals("Administration"))
                .collect(Collectors.toList())
        ));
        List<Menu> menus = new ArrayList<>(menusL1);
        Collections.sort(menus);
        menuItems.addAll(menus);
        //menuItems.add(admin);

        return menuItems.stream().distinct().collect(Collectors.toList());
    }

    @GetMapping("/modules/installed")
    @Cacheable(cacheNames = "modules")
    public List<Module> getModules() {
        log.debug("Get all installed modules");
        return moduleRepository.findAll().stream()
                .map(module -> {
                    module.setWebModules(null);
                    return module;
                })
                .collect(Collectors.toList());
    }

    @PutMapping("/modules/{id}")
    public ModuleResponse updateDetails(@PathVariable Long id, @RequestBody Module module) {
        return moduleService.updateDetails(id, module);
    }

    @PutMapping("/modules/{id}/menus")
    public ResponseEntity<List<Menu>> updateModuleMenu(@PathVariable Long id, @RequestBody ModuleMenuDTO moduleMenuDTO) {
        return ResponseEntity.ok(this.menuService.updateModuleMenu(id, moduleMenuDTO));
    }


}
