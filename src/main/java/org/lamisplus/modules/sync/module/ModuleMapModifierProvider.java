package org.lamisplus.modules.sync.module;

import com.foreach.across.core.annotations.Exposed;
import org.lamisplus.modules.sync.domain.entity.Module;

@Exposed
public interface ModuleMapModifierProvider {

    Module getModuleToModify();

    String getAngularModuleName();

    String getUmdUrl();

    String getModuleMap();

    default boolean reset() {
        return false;
    }

    default String getUmdLocation() {
        return null;
    }
}
