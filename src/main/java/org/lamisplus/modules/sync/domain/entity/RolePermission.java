package org.lamisplus.modules.sync.domain.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@Table(name = "role_permission")
@IdClass(RolePermissionPK.class)
public class RolePermission{
    @Column(name = "role_id")
    @Id
    private Long roleId;

    @Column(name = "permission_id")
    @Id
    private Long permissionId;
}