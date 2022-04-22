package org.lamisplus.modules.sync.repo;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.base.domain.entity.User;
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
    public Optional<RemoteAccessToken> findByName(String username) {
        return jdbcTemplate.query("SELECT * FROM remote_access_token WHERE username = ?",
                new BeanPropertyRowMapper<RemoteAccessToken>(RemoteAccessToken.class), username).stream().findFirst();
    }

    public Optional<RemoteAccessToken> findById(Long id) {
        return jdbcTemplate.query("SELECT * FROM remote_access_token WHERE id=?",
                new BeanPropertyRowMapper<RemoteAccessToken>(RemoteAccessToken.class), id).stream().findFirst();
    }

    public Optional<RemoteAccessToken> findByUrl(String url) {
        return jdbcTemplate.query("SELECT * FROM remote_access_token WHERE url=?",
                new BeanPropertyRowMapper<RemoteAccessToken>(RemoteAccessToken.class), url).stream().findFirst();
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM remote_access_token WHERE id=?", id);
    }

    public int save(RemoteAccessToken remoteAccessToken) {
        if(remoteAccessToken.getId() == null || remoteAccessToken.getId() == 0){
            return jdbcTemplate.update("INSERT INTO remote_access_token (password, token, url, username, application_user_id, organisation_unit_id, pr_key, pub_key) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    remoteAccessToken.getPassword(), remoteAccessToken.getToken(), remoteAccessToken.getUrl(),
                    remoteAccessToken.getUsername(), remoteAccessToken.getApplicationUserId(), remoteAccessToken.getOrganisationUnitId(),
                    remoteAccessToken.getPrKey(), remoteAccessToken.getPubKey());

        }
        return jdbcTemplate.update("UPDATE remote_access_token SET password=?, token=?, url=?, username=?, " +
                        "application_user_id=?, organisation_unit_id=?, pr_key=?, pub_key=? WHERE id=? ",
                remoteAccessToken.getPassword(), remoteAccessToken.getToken(), remoteAccessToken.getUrl(),
                remoteAccessToken.getUsername(), remoteAccessToken.getApplicationUserId(),
                remoteAccessToken.getOrganisationUnitId(), remoteAccessToken.getPrKey(), remoteAccessToken.getPubKey(), remoteAccessToken.getId());
    }


    public List<RemoteAccessToken> findAllByApplicationUserId(Long applicationUserId) {
        return jdbcTemplate.query("SELECT * FROM remote_access_token WHERE application_user_id=?",
                new BeanPropertyRowMapper<RemoteAccessToken>(RemoteAccessToken.class), applicationUserId);

    }

    public int save(User user) {
        return jdbcTemplate.update("INSERT INTO user (user_name, password, archived, current_organisation_unit_id, first_name, last_name) VALUES (?, ?, ?, ?, ?, ?)",
                user.getUserName(), user.getPassword(), user.getArchived(), user.getCurrentOrganisationUnitId(), user.getFirstName(), user.getLastName());

    }

    public Optional<RemoteAccessToken> findByNameAndUrlAndOrganisationUnitId(String username, String url, Long organisationUnitId) {
        return jdbcTemplate.query("SELECT * FROM remote_access_token WHERE username = ? AND url=? AND organisation_unit_id=?",
                new BeanPropertyRowMapper<RemoteAccessToken>(RemoteAccessToken.class), username, url, organisationUnitId).stream().findFirst();
    }

    public Optional<RemoteAccessToken> findByNameAndOrganisationUnitId(String username, Long organisationUnitId) {
        return jdbcTemplate.query("SELECT * FROM remote_access_token WHERE username = ? AND organisation_unit_id=?",
                new BeanPropertyRowMapper<RemoteAccessToken>(RemoteAccessToken.class), username, organisationUnitId).stream().findFirst();
    }

    public Optional<RemoteAccessToken> findWherePrivateKeyIsNull(String publicKey) {
        return jdbcTemplate.query("SELECT * FROM remote_access_token WHERE pr_key = ?",
                new BeanPropertyRowMapper<RemoteAccessToken>(RemoteAccessToken.class), publicKey).stream().findFirst();
    }

}
