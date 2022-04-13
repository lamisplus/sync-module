package org.lamisplus.modules.sync.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.SerializationUtils;
import org.hibernate.id.UUIDGenerator;
import org.lamisplus.modules.base.controller.AccountController;
import org.lamisplus.modules.base.controller.UserJWTController;
import org.lamisplus.modules.base.controller.apierror.EntityNotFoundException;
import org.lamisplus.modules.base.controller.apierror.RecordExistException;
import org.lamisplus.modules.base.controller.vm.LoginVM;
import org.lamisplus.modules.base.controller.vm.ManagedUserVM;
import org.lamisplus.modules.base.domain.dto.UserDTO;
import org.lamisplus.modules.base.domain.entity.User;
import org.lamisplus.modules.base.security.SecurityUtils;
import org.lamisplus.modules.base.service.UserService;
import org.lamisplus.modules.sync.domain.dto.RemoteUrlDTO;
import org.lamisplus.modules.sync.domain.entity.RemoteAccessToken;
import org.lamisplus.modules.sync.domain.entity.SyncQueue;
import org.lamisplus.modules.sync.repo.RemoteAccessTokenRepository;
import org.lamisplus.modules.sync.utility.HttpConnectionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemoteAccessTokenService {
    private final RemoteAccessTokenRepository remoteAccessTokenRepository;
    private final UserService userService;
    private final UserJWTController userJWTController;
    private final AccountController accountController;


    public RemoteAccessTokenRepository getRemoteAccessTokenRepository() {
        return remoteAccessTokenRepository;
    }

    @SneakyThrows
    public RemoteAccessToken save(byte[] bytes){
        RemoteAccessToken remoteAccessToken = (RemoteAccessToken) SerializationUtils.deserialize(bytes);

        log.info("Username is: {}", remoteAccessToken.getUsername());

        Optional<RemoteAccessToken> optionalRemoteAccessToken = remoteAccessTokenRepository.findByName(remoteAccessToken.getUsername());
        optionalRemoteAccessToken.ifPresent(remoteAccessToken1 -> {
            throw new RecordExistException(RemoteAccessToken.class, "username", ""+remoteAccessToken1.getUsername());
        });
        this.createUserOnServer(remoteAccessToken); // save to user table on the server

        remoteAccessToken.setToken(this.authenticate(remoteAccessToken));
        log.info("RemoteAccessToken: {}", remoteAccessToken);
        remoteAccessTokenRepository.save(remoteAccessToken);
        remoteAccessToken.setPassword("x");
        remoteAccessToken.setStatus(0L);
        return remoteAccessToken;
    }

    @SneakyThrows
    public void sendToRemoteAccessToServer(RemoteAccessToken remoteAccessToken) {
       String url = remoteAccessToken.getUrl().concat("/api/sync/server/remote-access-token");
       //TODO: set currentOrganisationUnit
        log.info("url is {}", url);
        //String remoteAccessTokenString = String.valueOf(remoteAccessToken);

        try {
            byte [] byteArray = SerializationUtils.serialize(remoteAccessToken);
            String response;
            response = new HttpConnectionManager().post(byteArray, "lamisplus", url);


            //For serializing the date on the sync queue
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            final RemoteAccessToken savedRemoteAccessToken = objectMapper.readValue(response, RemoteAccessToken.class);
            //Null the id to create a new record
            savedRemoteAccessToken.setId(null);
            userService.getUserWithRoles().ifPresent(user -> {
                savedRemoteAccessToken.setApplicationUserId(user.getId());
            });
            remoteAccessTokenRepository.save(savedRemoteAccessToken);
        }catch (Exception e){
            throw e;
        }
    }

    public List<RemoteUrlDTO> getRemoteUrls() {
        List<RemoteAccessToken> remoteAccessTokens;

        Optional<User> optionalUser = userService.getUserWithRoles();

        if(optionalUser.isPresent()){
            remoteAccessTokens = remoteAccessTokenRepository.findAllByApplicationUserId(optionalUser.get().getId());
        } else {
            remoteAccessTokens = remoteAccessTokenRepository.findAll();
        }

        List<RemoteUrlDTO> remoteUrlDTOS = new ArrayList<>();
        remoteAccessTokens.forEach(remoteAccessToken -> {
            RemoteUrlDTO remoteUrlDTO = new RemoteUrlDTO();
            remoteUrlDTO.setId(remoteAccessToken.getId());
            remoteUrlDTO.setUrl(remoteAccessToken.getUrl());
            remoteUrlDTO.setUsername(remoteAccessToken.getUsername());
            remoteUrlDTOS.add(remoteUrlDTO);
        });
        return remoteUrlDTOS;
    }


    @SneakyThrows
    public User createUserOnServer(RemoteAccessToken remoteAccessToken) throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName(remoteAccessToken.getUsername());
        userDTO.setLastName(remoteAccessToken.getUsername());
        userDTO.setUserName(remoteAccessToken.getUsername());
        userDTO.setCurrentOrganisationUnitId(0L);
        return userService.registerUser(userDTO, remoteAccessToken.getPassword(), false);
    }

    private String authenticate(RemoteAccessToken remoteAccessToken){
        LoginVM loginVM = new LoginVM();
        loginVM.setUsername(remoteAccessToken.getUsername());
        loginVM.setPassword(remoteAccessToken.getPassword());
        Long sevenDays = 168L;
        return userJWTController.authorize(loginVM, sevenDays).getBody().getIdToken();
    }
}
