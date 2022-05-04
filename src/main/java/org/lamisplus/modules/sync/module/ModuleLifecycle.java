package org.lamisplus.modules.sync.module;

public interface ModuleLifecycle {
    default void preInstall() {
    }

    default void preUninstall() {
    }
}
