package org.lamisplus.modules.sync.repository;

import org.lamisplus.modules.sync.domain.entity.RemoteAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RemoteAccessTokenRepository extends JpaRepository<RemoteAccessToken, Long> {
}
