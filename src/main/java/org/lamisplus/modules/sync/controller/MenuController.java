package org.lamisplus.modules.sync.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.sync.domain.dto.MenuDTO;
import org.lamisplus.modules.sync.domain.entity.Menu;
import org.lamisplus.modules.sync.service.MenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/menus")
@Slf4j
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

    /*@PutMapping("/modules/{id}")
    public ResponseEntity<List<Menu>> update(@PathVariable Long id, @RequestBody ModuleMenuDTO moduleMenuDTO) {
        return ResponseEntity.ok(this.menuService.update(id, moduleMenuDTO));
    }*/

    @GetMapping
    public ResponseEntity<List<MenuDTO>> getAllMenus(@RequestParam(required = false, defaultValue = "false") Boolean withChild) {
        return ResponseEntity.ok(this.menuService.getAllMenus(withChild));
    }

    @GetMapping("/parent/{id}")
    public ResponseEntity<List<MenuDTO>> getAllMenusByParentId(@PathVariable Integer id) {
        return ResponseEntity.ok(this.menuService.getAllMenusByParentId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Menu> update(@PathVariable Long id, @Valid @RequestBody MenuDTO menuDTO) {
        return ResponseEntity.ok(this.menuService.update(id, menuDTO));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        this.menuService.delete(id);
    }

    @PostMapping
    public ResponseEntity<Menu> save(@Valid @RequestBody MenuDTO menuDTO, @RequestParam(required = false, defaultValue = "true") Boolean isModule) {
        return ResponseEntity.ok(this.menuService.save(menuDTO, isModule));
    }
}
