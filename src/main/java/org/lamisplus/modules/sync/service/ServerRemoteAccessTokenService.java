package org.lamisplus.modules.sync.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
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
import org.lamisplus.modules.sync.utility.AESUtil;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerRemoteAccessTokenService {
    private final RemoteAccessTokenRepository remoteAccessTokenRepository;
    private final UserService userService;
    private final UserJWTController userJWTController;
    private final GenerateKeys generateKeys;
    //private final AESUtil aesUtil;


    @SneakyThrows
    public RemoteAccessToken save(byte[] bytes){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        RemoteAccessToken remoteAccessToken = (RemoteAccessToken) SerializationUtils.deserialize(bytes);
        log.info("Username is: {}", remoteAccessToken.getUsername());

        Optional<RemoteAccessToken> optionalRemoteAccessToken = remoteAccessTokenRepository.findByName(remoteAccessToken.getUsername());
        optionalRemoteAccessToken.ifPresent(remoteAccessToken1 -> {
            throw new RecordExistException(RemoteAccessToken.class, "username", ""+remoteAccessToken1.getUsername());
        });
        String key = this.generateAESKey(remoteAccessToken);

        //Encrypt AESKey with client public key
        byte[] aesKey = this.generateKeys.encrypt(key.getBytes(StandardCharsets.UTF_8), remoteAccessToken);
        remoteAccessToken.setAnyByteKey(aesKey);

        this.createUserOnServer(remoteAccessToken); // save to user table on the server

        remoteAccessToken.setToken(this.authenticate(remoteAccessToken));

        remoteAccessToken = this.generateKeys.keyGenerateAndReturnKey(remoteAccessToken);


        //byte[] prKeyBytes = objectMapper.writeValueAsBytes(remoteAccessToken.getPrKey());

        //byte[] pubKeyBytes = objectMapper.writeValueAsBytes(remoteAccessToken.getPubKey());

        //FileUtils.writeByteArrayToFile(new File("pr_key"), prKeyBytes);
        //FileUtils.writeByteArrayToFile(new File("pub_key"), pubKeyBytes);

       // prKeyBytes= FileUtils.readFileToByteArray(new File("pr_key"));
        //pubKeyBytes = FileUtils.readFileToByteArray(new File("pub_key"));

        //log.info("private is {}", objectMapper.readValue(prKeyBytes, new TypeReference<String>() {}));
        //log.info("public is {}", objectMapper.readValue(pubKeyBytes, new TypeReference<String>() {}));


        //log.info("RemoteAccessToken: {}", remoteAccessToken);

        remoteAccessTokenRepository.save(remoteAccessToken);
        remoteAccessToken.setPrKey("x");
        remoteAccessToken.setPassword("x");
        remoteAccessToken.setStatus(0L);
        //remoteAccessToken.setPrKey("x");
        return remoteAccessToken;
    }

    /*@SneakyThrows
    public void sendToRemoteAccessToServer(RemoteAccessToken remoteAccessToken) {
       String url = remoteAccessToken.getUrl().concat("/api/sync/server/remote-access-token");
       //TODO: set currentOrganisationUnit
        log.info("url is {}", url);
        //String remoteAccessTokenString = String.valueOf(remoteAccessToken);

        try {
            byte [] byteArray = SerializationUtils.serialize(remoteAccessToken);
            String response = new HttpConnectionManager().post(byteArray, "lamisplus", url);


            //For serializing the date on the sync queue
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            final RemoteAccessToken savedRemoteAccessToken = objectMapper.readValue(response, RemoteAccessToken.class);
            //Null the id to create a new record
            savedRemoteAccessToken.setId(null);
            User user = userService.getUserWithRoles().orElse(null);
            Long applicationUserId = 0L;

            if(user != null){
                applicationUserId = user.getId();
            }
            savedRemoteAccessToken.setApplicationUserId(applicationUserId);
            remoteAccessTokenRepository.save(savedRemoteAccessToken);
        }catch (Exception e){
            throw e;
        }
    }*/

    /*public List<RemoteUrlDTO> getRemoteUrls() {
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
    }*/


    @SneakyThrows
    public User createUserOnServer(RemoteAccessToken remoteAccessToken) {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName(remoteAccessToken.getUsername());
        userDTO.setLastName(remoteAccessToken.getUsername());
        userDTO.setUserName(remoteAccessToken.getUsername());

        if(null != remoteAccessToken.getOrganisationUnitId())userDTO.setCurrentOrganisationUnitId(remoteAccessToken.getOrganisationUnitId());
        else userDTO.setCurrentOrganisationUnitId(0L);

        return userService.registerUser(userDTO, remoteAccessToken.getPassword(), false);
    }

    private String authenticate(RemoteAccessToken remoteAccessToken){
        LoginVM loginVM = new LoginVM();
        loginVM.setUsername(remoteAccessToken.getUsername());
        loginVM.setPassword(remoteAccessToken.getPassword());
        Long sevenDays = 168L;
        return userJWTController.authorize(loginVM, sevenDays).getBody().getIdToken();
    }

    public String generateAESKey(RemoteAccessToken remoteAccessToken) throws GeneralSecurityException, IOException {
        return DatatypeConverter.printBase64Binary(AESUtil.getKeyFromPassword(remoteAccessToken.getPassword(), "ead60638-c065-4ea4-a17a-3a01287e4cab").getEncoded());
        //remoteAccessToken.setPrByte(generateKeys.encrypt(key.getBytes(), remoteAccessToken));
        //remoteAccessToken.setAnyPubKey(key);
        //return remoteAccessToken;
    }
}
