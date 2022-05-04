package org.lamisplus.modules.sync.service;

import org.lamisplus.modules.sync.domain.entity.User;
import org.lamisplus.modules.sync.repository.UserRepository;
import org.lamisplus.modules.sync.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Service
public class UserDetailService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private EntityManager entityManager;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        try {
            User user = entityManager
                    .createQuery("SELECT u FROM User u LEFT JOIN FETCH u.role r LEFT JOIN FETCH r.permission p WHERE u.userName = :userName", User.class)
                    .setParameter("userName", userName)
                    .getSingleResult();
            return new UserPrincipal(user);
        } catch (NoResultException e) {
            throw new UsernameNotFoundException("not found");
        }
    }

    /*private User createSecurityUser(String lowercaseUserName, org.lamisplus.modules.sync.domain.entity.User user){
        List<GrantedAuthority> grantedAuthorities = user
                .getRoles()
                .stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                .collect(Collectors.toList());
        return new User(user.getUserName(), user.getPassword(), grantedAuthorities);
    }*/
}
