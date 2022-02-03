package org.lamisplus.modules.sync.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@Entity
@EqualsAndHashCode
@Table(name = "remote_access_token")
public class RemoteAccessToken  implements Serializable {
    @Id
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic
    @Column(name = "url")
    private String url;

    @Basic
    @Column(name = "username")
    private String username;

    @Basic
    @Column(name = "password")
    private String password;

    @Basic
    @Column(name = "token")
    private String token;

    @OneToMany(mappedBy = "remoteAccessTokenById")
    @JsonIgnore
    @ToString.Exclude
    private List<SyncHistory> syncHistoriesByRemoteAccessTokenId;

}
