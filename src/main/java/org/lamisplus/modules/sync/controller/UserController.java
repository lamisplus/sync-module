package org.lamisplus.modules.sync.controller;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.sync.controller.vm.ManagedUserVM;
import org.lamisplus.modules.sync.domain.dto.UserDTO;
import org.lamisplus.modules.sync.domain.entity.Role;
import org.lamisplus.modules.sync.domain.entity.User;
import org.lamisplus.modules.sync.repository.RoleRepository;
import org.lamisplus.modules.sync.repository.UserRepository;
import org.lamisplus.modules.sync.service.UserService;
import org.lamisplus.modules.sync.util.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    //private final SessionRegistry sessionRegistry;


    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(userRepository.findById(id).map(UserDTO::new).get());
    }

    @PostMapping("/{id}/roles")
    public ResponseEntity<Object[]> updateRoles(@Valid @RequestBody List<Role> roles, @PathVariable Long id) throws Exception {
        try {
            User user = userRepository.findById(id).get();
            HashSet rolesSet = new HashSet<>();
            Role roleToAdd = new Role();
            for(Role r : roles){
                // add roles by either id or name
                if(r.getName() != null ) {
                    roleToAdd = roleRepository.findByName(r.getName()).get();
                } else if(r.getId() != null ){
                    roleToAdd = roleRepository.findById(r.getId()).get();
                } else {
                    ResponseEntity.badRequest();
                    return null;
                }
                rolesSet.add(roleToAdd);
            }
            user.setRole(rolesSet);
            userService.update(id, user);
            return ResponseEntity.ok(user.getRole().toArray());
        } catch (Exception e) {
            throw e;
        }
    }


    /*@PostMapping("/organisationUnit/{id}")
    public ResponseEntity<UserDTO> getAllUsers(@PathVariable Long id) {
        UserDTO userDTO = userService
                .getUserWithRoles()
                .map(UserDTO::new)
                .orElseThrow(() -> new EntityNotFoundException(User.class, "Not Found", ""));
        return ResponseEntity.ok(userService.changeOrganisationUnit(id, userDTO));
    }*/

    /*@GetMapping("/logged-in/count")
    public Integer getNumberOfLoggedInUsers() {
        final List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
        return  allPrincipals.size();
    }*/

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void save(@Valid @RequestBody ManagedUserVM managedUserVM) {
        //Check Password Length
        userService.save(managedUserVM, managedUserVM.getPassword());
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public void update(@PathVariable Long id, @Valid @RequestBody ManagedUserVM managedUserVM) {
        userService.update(id, managedUserVM, managedUserVM.getPassword());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public void update(@PathVariable Long id) {
        userService.delete(id);
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(Pageable pageable) {
        final Page<UserDTO> page = userService.getAllManagedUsers(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
