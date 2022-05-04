package org.lamisplus.modules.sync.yml;

import lombok.Data;

import java.util.Set;

@Data
public class Role {
    Set<Permission> permissions;
    private String name;
    private String authority;
    private String description;
}
