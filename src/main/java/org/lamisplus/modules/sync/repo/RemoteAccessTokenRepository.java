package org.lamisplus.modules.sync.repo;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.sync.domain.entity.RemoteAccessToken;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RemoteAccessTokenRepository {
    private final JdbcTemplate jdbcTemplate;

    public List<RemoteAccessToken> findAll() {
        return jdbcTemplate.query("SELECT * FROM remote_access_token",
                new BeanPropertyRowMapper<RemoteAccessToken>(RemoteAccessToken.class));
    }
    public List<RemoteAccessToken> findAllByName(String username) {
        return jdbcTemplate.query("SELECT * FROM remote_access_token WHERE username = ?",
                new BeanPropertyRowMapper<RemoteAccessToken>(RemoteAccessToken.class), username);
    }

    public Optional<RemoteAccessToken> findById(Long id) {
        return jdbcTemplate.query("SELECT * FROM remote_access_token WHERE id=?",
                new BeanPropertyRowMapper<RemoteAccessToken>(RemoteAccessToken.class), id).stream().findFirst();
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM remote_access_token WHERE id=?", id);
    }

    public int save(RemoteAccessToken remoteAccessToken) {
        if(remoteAccessToken.getId() == null || remoteAccessToken.getId() == 0){
            return jdbcTemplate.update("INSERT INTO remote_access_token (password, token, url, username, application_user_id) VALUES (?, ?, ?, ?, ?)",
                    remoteAccessToken.getPassword(), remoteAccessToken.getToken(), remoteAccessToken.getUrl(),
                    remoteAccessToken.getUsername(), remoteAccessToken.getApplicationUserId());

        }
        return jdbcTemplate.update("UPDATE remote_access_token SET password=?, token=?, url=?, username=?, application_user_id=? " +
                        "WHERE id=? ",
                remoteAccessToken.getPassword(), remoteAccessToken.getToken(), remoteAccessToken.getUrl(),
                remoteAccessToken.getUsername(), remoteAccessToken.getApplicationUserId(), remoteAccessToken.getId());
    }
}
