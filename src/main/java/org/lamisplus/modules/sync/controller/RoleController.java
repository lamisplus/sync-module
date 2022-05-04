package org.lamisplus.modules.sync.controller;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.sync.controller.apierror.EntityNotFoundException;
import org.lamisplus.modules.sync.domain.dto.RoleDTO;
import org.lamisplus.modules.sync.domain.dto.UserDTO;
import org.lamisplus.modules.sync.domain.entity.Role;
import org.lamisplus.modules.sync.repository.RoleRepository;
import org.lamisplus.modules.sync.service.RoleService;
import org.lamisplus.modules.sync.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static org.lamisplus.modules.sync.util.Constants.ArchiveStatus.ARCHIVED;
import static org.lamisplus.modules.sync.util.Constants.ArchiveStatus.UN_ARCHIVED;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/roles")
public class RoleController {
    private final RoleService roleService;
    private final RoleRepository roleRepository;
    private final UserService userService;


    @GetMapping("/v2/{id}")
    public ResponseEntity<Role> getById(@PathVariable Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException(Role.class, "id", ""+id));

        return ResponseEntity.ok(role);
    }

    @GetMapping
    public ResponseEntity<List<Role>> getAll() {
        List<Role> roles = roleRepository.findAllByArchived(UN_ARCHIVED);
        return ResponseEntity.ok(roles);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void save(@Valid @RequestBody RoleDTO roleDTO) throws Exception {
        roleService.save(roleDTO);
    }

    @PostMapping("/v2/{id}")
    public ResponseEntity<Role> update(@Valid @RequestBody RoleDTO role, @PathVariable Long id) {
        try {
            Role updatedRole = new Role();
            if (!role.getPermissions().isEmpty()){
                updatedRole = roleService.updatePermissions(id, role.getPermissions());
            }
            if (role.getName() != null){
                updatedRole = roleService.updateName(id, role.getName());
            }
            return ResponseEntity.ok(updatedRole);
        } catch (Exception e) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST);
        }
        return null;
    }

    @DeleteMapping("/v2/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteRole(@PathVariable Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException(Role.class, "id", ""+id));
        role.setArchived(ARCHIVED);
        try {
            roleRepository.save(role);
        } catch (Exception e) {
            throw e;
        }
    }

    @GetMapping("/v2/{id}/users")
    public ResponseEntity<List<UserDTO>> getAllUserByRole(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getAllUserByRole(id));
    }
}
