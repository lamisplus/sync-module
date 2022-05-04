package org.lamisplus.modules.sync.module;

import lombok.Data;
import org.lamisplus.modules.sync.domain.entity.Module;

@Data
public class ModuleResponse {
    public enum Type {ERROR, SUCCESS}

    private Type type;
    private String message;
    private Module module;
}
