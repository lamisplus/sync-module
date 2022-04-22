package org.lamisplus.modules.sync.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.SerializationUtils;
import org.lamisplus.modules.base.controller.UserJWTController;
import org.lamisplus.modules.base.controller.apierror.RecordExistException;
import org.lamisplus.modules.base.controller.vm.LoginVM;
import org.lamisplus.modules.base.domain.dto.UserDTO;
import org.lamisplus.modules.base.domain.entity.User;
import org.lamisplus.modules.base.service.UserService;
import org.lamisplus.modules.sync.domain.dto.RemoteUrlDTO;
import org.lamisplus.modules.sync.domain.entity.RemoteAccessToken;
import org.lamisplus.modules.sync.repo.RemoteAccessTokenRepository;
import org.lamisplus.modules.sync.utility.AESUtil;
import org.lamisplus.modules.sync.utility.HttpConnectionManager;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientRemoteAccessTokenService {
    private final RemoteAccessTokenRepository remoteAccessTokenRepository;
    private final UserService userService;
    //private final UserJWTController userJWTController;
    private final GenerateKeys generateKeys;
    //private final AESUtil aesUtil;


    public RemoteAccessTokenRepository getRemoteAccessTokenRepository() {
        return remoteAccessTokenRepository;
    }


    @SneakyThrows
    public void sendToRemoteAccessToServer(RemoteAccessToken remoteAccessToken) {
        remoteAccessToken = generateKeys.keyGenerateAndReturnKey(remoteAccessToken);
        remoteAccessToken.setAnyPubKey(remoteAccessToken.getPubKey());
        remoteAccessToken.setPrKey(null);
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

            remoteAccessToken.set


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


    /*public RemoteAccessToken sendAndSaveKey(RemoteAccessToken remoteAccessToken) throws GeneralSecurityException, IOException {
        String key = DatatypeConverter.printBase64Binary(AESUtil.getKeyFromPassword(remoteAccessToken.getPassword(), "ead60638-c065-4ea4-a17a-3a01287e4cab").getEncoded());
        remoteAccessToken.setPrByte(generateKeys.encrypt(key.getBytes(), remoteAccessToken));
        return remoteAccessToken;
    }*/

    private String decryptWithPrivateKey(RemoteAccessToken remoteAccessToken) throws GeneralSecurityException, IOException {
        PrivateKey privateKey = this.generateKeys.readPrivateKey(remoteAccessToken);
        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedMessageBytes = decryptCipher.doFinal(encryptedMessageBytes);
        String decryptedMessage = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
    }
}
