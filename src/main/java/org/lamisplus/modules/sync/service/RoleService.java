package org.lamisplus.modules.sync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lamisplus.modules.sync.controller.apierror.EntityNotFoundException;
import org.lamisplus.modules.sync.controller.apierror.RecordExistException;
import org.lamisplus.modules.sync.domain.dto.RoleDTO;
import org.lamisplus.modules.sync.domain.entity.Permission;
import org.lamisplus.modules.sync.domain.entity.Role;
import org.lamisplus.modules.sync.domain.entity.RolePermission;
import org.lamisplus.modules.sync.repository.PermissionRepository;
import org.lamisplus.modules.sync.repository.RolePermissionRepository;
import org.lamisplus.modules.sync.repository.RoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

import static org.lamisplus.modules.sync.util.Constants.ArchiveStatus.UN_ARCHIVED;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;


    @PersistenceContext
    EntityManager em;

    public void save(RoleDTO roleDTO) {
        Optional<Role> RoleOptional = roleRepository.findByName(roleDTO.getName());
        if (RoleOptional.isPresent()) throw new RecordExistException(Role.class, "Name", roleDTO.getName());
        Role role = new Role();
        role.setName(roleDTO.getName());
        HashSet<Permission> permissions = getPermissions(roleDTO.getPermissions());
        role.setArchived(UN_ARCHIVED);
        if(StringUtils.isBlank(role.getCode())) {
            role.setCode(UUID.randomUUID().toString());
        }
        Role savedRole =  roleRepository.save(role);
        List<RolePermission> rolePermissions = new ArrayList<>();
        RolePermission rolePermission = new RolePermission();

        permissions.forEach(permission -> {
            rolePermission.setPermissionId(permission.getId());
            rolePermission.setRoleId(savedRole.getId());
            rolePermissions.add(rolePermission);
        });
        rolePermissionRepository.saveAll(rolePermissions);
    }

    public Role get(Long id) {
        Optional<Role> roleOptional = this.roleRepository.findById(id);
        if (!roleOptional.isPresent()) throw new EntityNotFoundException(Role.class, "Id", id + "");
        return roleOptional.get();
    }

    public Role updateName(long id, String name) {
        Optional<Role> roleOptional = roleRepository.findById(id);
        if(!roleOptional.isPresent())throw new EntityNotFoundException(Role.class, "Id", id +"");
        Role updatedRole = roleOptional.get();
        updatedRole.setName(name);
        updatedRole.setArchived(UN_ARCHIVED);
        return roleRepository.save(updatedRole);
    }

    public Role updatePermissions(long id, List<Permission> permissions) {
        Optional<Role> roleOptional = roleRepository.findById(id);
        if(!roleOptional.isPresent())throw new EntityNotFoundException(Role.class, "Id", id +"");
        Role updatedRole = roleOptional.get();
        HashSet<Permission> permissionsSet = getPermissions(permissions);
        updatedRole.setPermission(permissionsSet);
        return roleRepository.save(updatedRole);
    }

    private HashSet<Permission> getPermissions(List<Permission> permissions) {
        HashSet permissionsSet = new HashSet<>();
        Permission permissionToAdd = new Permission();
        for(Permission p : permissions){
            try {
                // add permissions by either id or name
                if (null != p.getName()) {
                    permissionToAdd = permissionRepository.findByNameAndArchived(p.getName(), UN_ARCHIVED).get();
                }  else {
                    ResponseEntity.badRequest();
                    return null;
                }
                permissionsSet.add(permissionToAdd);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return permissionsSet;
    }
}
