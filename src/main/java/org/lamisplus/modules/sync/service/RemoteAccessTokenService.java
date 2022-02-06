package org.lamisplus.modules.sync.service;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.sync.domain.dto.RemoteUrlDTO;
import org.lamisplus.modules.sync.domain.entity.RemoteAccessToken;
import org.lamisplus.modules.sync.repo.RemoteAccessTokenRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RemoteAccessTokenService {
    private final RemoteAccessTokenRepository remoteAccessTokenRepository;

    public void save(RemoteAccessToken remoteAccessToken) {
        System.out.println("Username is:"+remoteAccessToken.getUsername());
        List<RemoteAccessToken> remoteAccessTokens = remoteAccessTokenRepository.findAllByName(remoteAccessToken.getUsername());
        if(remoteAccessTokens != null && !remoteAccessTokens.isEmpty()){
            throw new IllegalStateException("username already exist");
        }
        remoteAccessTokenRepository.save(remoteAccessToken);
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
