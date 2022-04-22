package org.lamisplus.modules.sync.repo;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.sync.domain.entity.RemoteKey;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RemoteKeyRepository {
    private final JdbcTemplate jdbcTemplate;

    public List<RemoteKey> findAll() {
        return jdbcTemplate.query("SELECT * FROM remote_key",
                new BeanPropertyRowMapper<RemoteKey>(RemoteKey.class));
    }

    public Optional<RemoteKey> findByUUID(String uuid) {
        return jdbcTemplate.query("SELECT * FROM remote_key WHERE uuid = ?",
                new BeanPropertyRowMapper<RemoteKey>(RemoteKey.class), uuid).stream().findFirst();
    }

    public Optional<RemoteKey> findById(Long id) {
        return jdbcTemplate.query("SELECT * FROM remote_key WHERE id = ?",
                new BeanPropertyRowMapper<RemoteKey>(RemoteKey.class), id).stream().findFirst();
    }

    public int save(RemoteKey remoteKey) {
        if(remoteKey.getId() == null || remoteKey.getId() == 0){
            return jdbcTemplate.update("INSERT INTO remote_key (key, uuid) VALUES (?,?)",
                    remoteKey.getKey(), remoteKey.getUuid());
        }
        return jdbcTemplate.update("UPDATE remote_key SET key=? WHERE id=? ",
                remoteKey.getKey(), remoteKey.getId());
    }

}
