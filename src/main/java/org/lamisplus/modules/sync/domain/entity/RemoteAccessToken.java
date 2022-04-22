package org.lamisplus.modules.sync.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

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
    @NotEmpty(message = "url is mandatory")
    private String url;

    @Basic
    @Column(name = "username")
    @NotEmpty(message = "username is mandatory")
    private String username;

    @Basic
    @Column(name = "password")
    //@NotEmpty(message = "password is mandatory")
    private String password;

    @Basic
    @Column(name = "token")
    private String token;

    @Basic
    @Column(name = "status")
    private Long status;

    @Basic
    @Column(name = "application_user_id")
    private Long applicationUserId;

    @Basic
    @Column(name = "organisation_unit_id")
    private Long organisationUnitId;

    @Basic
    @Column(name = "pr_key")
    private String prKey;

    @Basic
    @Column(name = "pub_key")
    private String pubKey;

    @Basic
    @Column(name = "any_pub_key")
    private String anyPubKey;

    @Transient
    private byte[] anyByteKey;
}
