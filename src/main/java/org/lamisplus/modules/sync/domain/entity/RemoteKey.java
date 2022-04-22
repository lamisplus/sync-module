package org.lamisplus.modules.sync.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
@Entity
@EqualsAndHashCode
@Table(name = "remote_key")
public class RemoteKey implements Serializable {
    @Id
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic
    @Column(name = "key")
    private String key;

    @Basic
    @Column(name = "uuid")
    private String uuid;

}
