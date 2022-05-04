package org.lamisplus.modules.sync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.sync.controller.apierror.EntityNotFoundException;
import org.lamisplus.modules.sync.controller.apierror.RecordExistException;
import org.lamisplus.modules.sync.domain.dto.MenuDTO;
import org.lamisplus.modules.sync.domain.dto.ModuleMenuDTO;
import org.lamisplus.modules.sync.domain.entity.Menu;
import org.lamisplus.modules.sync.domain.entity.Module;
import org.lamisplus.modules.sync.domain.mapper.MenuMapper;
import org.lamisplus.modules.sync.repository.MenuRepository;
import org.lamisplus.modules.sync.repository.ModuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.lamisplus.modules.sync.util.Constants.ArchiveStatus.UN_ARCHIVED;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;
    private final ModuleRepository moduleRepository;
    private final MenuMapper menuMapper;

    public List<Menu> updateModuleMenu(Long moduleId, ModuleMenuDTO moduleMenuDTO) {
        Module module = moduleRepository.findById(moduleId).orElseThrow(()-> new EntityNotFoundException(Module.class, "id", ""+moduleId));
        List<Menu> menus = new ArrayList<>();

        menuRepository.deleteByModuleId(moduleId);
        moduleMenuDTO.getMenuDTOS().forEach(menuDTO -> {
            if(menuDTO.getId() != null){
                menuRepository.deleteById(menuDTO.getId());
            }
            Menu menu =  menuMapper.toMenu(menuDTO);
            menu.setModule(module);
            menus.add(menu);
        });
        return menuRepository.saveAll(menus);
    }

    public List<MenuDTO> getAllMenus(Boolean withChild) {
        if(withChild) {
            return menuRepository.findAllByArchivedOrderByPositionAsc(UN_ARCHIVED).stream().
                    map(menu -> {
                        MenuDTO menuDTO = menuMapper.toMenuDTO(menu);
                        Menu parent = menu.getParent();
                        if (parent != null) menuDTO.setParentName(parent.getName());
                        return menuDTO;
                    }).sorted(Comparator.comparingInt(MenuDTO::getPosition))
                    .collect(Collectors.toList());
        }
        return menuRepository.findAllByArchivedAndParentIdOrderByPositionAsc(UN_ARCHIVED, null).stream().
                map(menu -> {
                    MenuDTO menuDTO = menuMapper.toMenuDTO(menu);
                    Menu parent = menu.getParent();
                    if (parent != null) menuDTO.setParentName(parent.getName());
                    return menuDTO;
                }).sorted(Comparator.comparingInt(MenuDTO::getPosition))
                .collect(Collectors.toList());
    }

    public List<MenuDTO> getAllMenusByParentId(Integer parentId) {
        if(parentId == 0) parentId = null;
        return menuRepository.findAllByArchivedAndParentIdOrderByIdDesc(UN_ARCHIVED, parentId).stream().
                map(menu -> {
                    MenuDTO menuDTO =  menuMapper.toMenuDTO(menu);
                    Menu parent = menu.getParent();
                    if(parent != null)menuDTO.setParentName(parent.getName());
                    return menuDTO;
                }).collect(Collectors.toList());
    }



    public void delete(Long id){
        Menu menu = menuRepository.findById(id).orElseThrow(()-> new EntityNotFoundException(Menu.class, "id", ""+id));
        if(!menu.getSubs().isEmpty()){
            throw new RecordExistException(Menu.class, "Sub menus ", "This has sub menus reassign or delete them");
        }
        menuRepository.delete(menu);
    }

    public Menu update(Long id, MenuDTO menuDTO){
        menuRepository.findById(id).orElseThrow(()-> new EntityNotFoundException(Menu.class, "id", ""+id));
        Menu menu = menuMapper.toMenu(menuDTO);
        menu.setId(id);
        return menuRepository.save(menu);
    }

    public Menu save(MenuDTO menuDTO, Boolean isModule){
        if(isModule) {
            if(menuDTO.getModuleId() == null){
                throw new EntityNotFoundException(Module.class, "moduleId", "" + "moduleId is null");
            }
            moduleRepository.findById(menuDTO.getModuleId()).orElseThrow(() -> new EntityNotFoundException(Module.class, "moduleId", "" + menuDTO.getModuleId()));
        }

        menuRepository.findByName(menuDTO.getName()).ifPresent(menu -> {
            throw new RecordExistException(Menu.class, "Menu ", menu.getName() +"");
        });

        //menuDTO.setModuleId(null);
        Menu menu = menuMapper.toMenu(menuDTO);
        menu.setId(null);
        return menuRepository.save(menu);
    }
}
