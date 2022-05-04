package org.lamisplus.modules.sync.domain.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.lamisplus.modules.sync.domain.entity.Permission;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class RoleDTO {
    private Long id;
    @NotBlank(message = "name is mandatory")
    private String name;
    private String code;
    private List<Permission> permissions;
}
