package org.lamisplus.modules.sync.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.SerializationUtils;
import org.hibernate.id.UUIDGenerator;
import org.lamisplus.modules.base.controller.apierror.RecordExistException;
import org.lamisplus.modules.sync.domain.dto.RemoteUrlDTO;
import org.lamisplus.modules.sync.domain.entity.RemoteAccessToken;
import org.lamisplus.modules.sync.domain.entity.SyncQueue;
import org.lamisplus.modules.sync.repo.RemoteAccessTokenRepository;
import org.lamisplus.modules.sync.utility.HttpConnectionManager;
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

    public RemoteAccessToken save(byte[] bytes) {
        RemoteAccessToken remoteAccessToken = (RemoteAccessToken)SerializationUtils.deserialize(bytes);
        log.info("Username is: {}", remoteAccessToken.getUsername());

        Optional<RemoteAccessToken> optionalRemoteAccessToken = remoteAccessTokenRepository.findByName(remoteAccessToken.getUsername());
        optionalRemoteAccessToken.ifPresent(remoteAccessToken1 -> {
            throw new RecordExistException(RemoteAccessToken.class, "username", ""+remoteAccessToken1.getUsername());
        });
        remoteAccessToken.setToken(UUID.randomUUID().toString());
        remoteAccessTokenRepository.save(remoteAccessToken);
        RemoteAccessToken remoteAccessToken1 = remoteAccessTokenRepository.findByName(remoteAccessToken.getUsername()).get();
        remoteAccessToken1.setPassword("x");
        remoteAccessToken1.setStatus(0L);
        return remoteAccessToken1;
    }

    public void sendToRemoteAccessToServer(RemoteAccessToken remoteAccessToken) {
       String url = remoteAccessToken.getUrl().concat("/api/sync/server/remote-access-token");
        log.info("url is {}", remoteAccessToken.getUrl());
        //String remoteAccessTokenString = String.valueOf(remoteAccessToken);

        try {
            byte [] byteArray = SerializationUtils.serialize(remoteAccessToken);
            String response = new HttpConnectionManager().post(byteArray, url);

            //For serializing the date on the sync queue
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            remoteAccessToken = objectMapper.readValue(response, RemoteAccessToken.class);
            remoteAccessTokenRepository.save(remoteAccessToken);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<RemoteUrlDTO> getRemoteUrls() {
        List<RemoteUrlDTO> remoteUrlDTOS = new ArrayList<>();
        List<RemoteAccessToken> remoteAccessTokens =  remoteAccessTokenRepository.findAll();
        remoteAccessTokens.forEach(remoteAccessToken -> {
            RemoteUrlDTO remoteUrlDTO = new RemoteUrlDTO();
            remoteUrlDTO.setId(remoteAccessToken.getId());
            remoteUrlDTO.setUrl(remoteAccessToken.getUrl());
            remoteUrlDTO.setUsername(remoteAccessToken.getUsername());
            remoteUrlDTOS.add(remoteUrlDTO);
        });
        return remoteUrlDTOS;
    }
}
