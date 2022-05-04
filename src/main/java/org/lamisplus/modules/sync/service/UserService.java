package org.lamisplus.modules.sync.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.lamisplus.modules.sync.controller.apierror.EntityNotFoundException;
import org.lamisplus.modules.sync.controller.apierror.RecordExistException;
import org.lamisplus.modules.sync.domain.dto.UserDTO;
import org.lamisplus.modules.sync.domain.entity.ApplicationUserOrganisationUnit;
import org.lamisplus.modules.sync.domain.entity.OrganisationUnit;
import org.lamisplus.modules.sync.domain.entity.Role;
import org.lamisplus.modules.sync.domain.entity.User;
import org.lamisplus.modules.sync.domain.mapper.UserMapper;
import org.lamisplus.modules.sync.repository.RoleRepository;
import org.lamisplus.modules.sync.repository.UserRepository;
import org.lamisplus.modules.sync.security.RolesConstants;
import org.lamisplus.modules.sync.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.lamisplus.modules.sync.util.Constants.ArchiveStatus.ARCHIVED;


@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;

    private final UserMapper userMapper;

    @Transactional
    public Optional<User> getUserWithAuthoritiesByUsername(String userName) {
        return userRepository.findOneWithRoleByUserName(userName);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithRoles() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithRoleByUserName);
    }

    public User save(UserDTO userDTO, String password) {
        Optional<User> optionalUser = userRepository.findOneByUserName(userDTO.getUserName());
        optionalUser.ifPresent(existingUser -> {
                    throw new RecordExistException(User.class, "Name", userDTO.getUserName());
        });
        return this.registerOrUpdateUser(userDTO, password);
    }

    public User registerOrUpdateUser(UserDTO userDTO, String password){
        User newUser = new User();
        if(userDTO.getId() != null){
            newUser.setId(userDTO.getId());
        }
        
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setUserName(userDTO.getUserName());
        newUser.setEmail(userDTO.getEmail());
        newUser.setPhoneNumber(userDTO.getPhoneNumber());
        newUser.setGender(userDTO.getGender());
        newUser.setCurrentOrganisationUnitId(getUserWithRoles().get().getCurrentOrganisationUnitId());
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(userDTO.getFirstName());
        newUser.setLastName(userDTO.getLastName());
        if (userDTO.getDetails() != null) {
            newUser.setDetails(userDTO.getDetails());
        }

        if (userDTO.getRoles() == null || userDTO.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            Role role = roleRepository.findAll().stream()
                    .filter(name -> RolesConstants.USER.equals(name.getName()))
                    .findAny()
                    .orElse(null);
            if (role != null)
                roles.add(role);
            newUser.setRole(roles);
        } else {
            newUser.setRole(getRolesFromStringSet(userDTO.getRoles()));
        }

        userRepository.save(newUser);
        return newUser;
    }

    public User update(Long id, UserDTO userDTO, String password){
        userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(User.class, "id", ""+id));
        userDTO.setId(id);

        return this.registerOrUpdateUser(userDTO, password);
    }

    public void delete(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(User.class, "id", ""+id));
        user.setArchived(ARCHIVED);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAllByArchived(pageable, 0).map(UserDTO::new);
    }

    public User update(Long id, User user) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (!optionalUser.isPresent()) throw new EntityNotFoundException(User.class, "Id", id + "");
        user.setId(id);
        return userRepository.save(user);
    }

    private HashSet<Role> getRolesFromStringSet(Set<String> roles) {
        HashSet roleSet = new HashSet<>();
        Role roleToAdd = new Role();
        for (String r : roles) {
            // add roles by either id or name
            if (null != r) {
                roleToAdd = roleRepository.findByName(r).get();
                if (null == roleToAdd && NumberUtils.isParsable(r))
                    roleToAdd = roleRepository.findById(Long.valueOf(r)).get();
            } else {
                ResponseEntity.badRequest();
                return null;
            }
            roleSet.add(roleToAdd);
        }
        return roleSet;
    }

    @Transactional
    public List<UserDTO> getAllUserByRole(Long roleId) {
        HashSet<Role> roles = new HashSet<>();
        Optional<Role> role = roleRepository.findById(roleId);
        roles.add(role.get());

        return userMapper.usersToUserDTOs(userRepository.findAllByRoleIn(roles));
    }

    public UserDTO changeOrganisationUnit(Long organisationUnitId, UserDTO userDTO) {
        Optional<User> optionalUser = userRepository.findById(userDTO.getId());

        boolean found = false;
        for (ApplicationUserOrganisationUnit applicationUserOrganisationUnit : userDTO.getApplicationUserOrganisationUnits()) {
            Long orgUnitId = applicationUserOrganisationUnit.getOrganisationUnitId();
            if (organisationUnitId.longValue() == orgUnitId.longValue()) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new EntityNotFoundException(OrganisationUnit.class, "Id", organisationUnitId + "");
        }
        User user = optionalUser.get();
        user.setCurrentOrganisationUnitId(organisationUnitId);
        return userMapper.userToUserDTO(userRepository.save(user));
    }
}
