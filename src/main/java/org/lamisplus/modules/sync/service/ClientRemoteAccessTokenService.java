package org.lamisplus.modules.sync.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.SerializationUtils;
import org.lamisplus.modules.base.domain.entity.User;
import org.lamisplus.modules.base.service.UserService;
import org.lamisplus.modules.sync.domain.dto.RemoteUrlDTO;
import org.lamisplus.modules.sync.domain.entity.RemoteAccessToken;
import org.lamisplus.modules.sync.domain.entity.RemoteKey;
import org.lamisplus.modules.sync.repo.RemoteAccessTokenRepository;
import org.lamisplus.modules.sync.repo.RemoteKeyRepository;
import org.lamisplus.modules.sync.utility.HttpConnectionManager;
import org.lamisplus.modules.sync.utility.RSAUtils;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientRemoteAccessTokenService {
    private final RemoteAccessTokenRepository remoteAccessTokenRepository;
    private final RemoteKeyRepository remoteKeyRepository;
    private final UserService userService;
    private final RSAUtils rsaUtils;



    public void sendToRemoteAccessToServer(RemoteAccessToken clientRemoteAccessToken) throws IOException, GeneralSecurityException {
        final RemoteAccessToken remoteAccessToken = this.rsaUtils.keyGenerateAndReturnKey(clientRemoteAccessToken);
        String uuid = UUID.randomUUID().toString();

        RemoteKey remoteKey = new RemoteKey();
        remoteKey.setId(null);
        remoteKey.setKey(remoteAccessToken.getPrKey());
        remoteKey.setUuid(uuid);

        remoteKeyRepository.save(remoteKey);
        userService.getUserWithRoles().ifPresent(user -> {
            remoteAccessToken.setOrganisationUnitId(user.getCurrentOrganisationUnitId());
        });

        remoteAccessToken.setRemoteId(remoteKeyRepository.findByUUID(uuid).get().getId());

        remoteAccessToken.setAnyPubKey(remoteAccessToken.getPubKey());
        remoteAccessToken.setPrKey(null);
       String url = remoteAccessToken.getUrl().concat("/api/sync/server/remote-access-token");
       //TODO: set currentOrganisationUnit
        log.info("url is {}", url);

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
            //Key things
            String pubKey = remoteKeyRepository.findById(savedRemoteAccessToken.getRemoteId()).get().getKey();
            remoteAccessToken.setPrKey(pubKey);
            String aesKey = this.decryptWithPrivateKey(savedRemoteAccessToken.getAnyByteKey(), remoteAccessToken);
            savedRemoteAccessToken.setPrKey(aesKey);

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

    private String decryptWithPrivateKey(byte[] encryptedMessageBytes, RemoteAccessToken remoteAccessToken) throws GeneralSecurityException, IOException {
        PrivateKey privateKey = this.rsaUtils.readPrivateKey(remoteAccessToken);
        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedMessageBytes = decryptCipher.doFinal(encryptedMessageBytes);
        String decryptedMessage = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
        return decryptedMessage;
    }
}
