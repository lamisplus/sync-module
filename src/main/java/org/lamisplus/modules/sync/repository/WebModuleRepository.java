package org.lamisplus.modules.sync.repository;

import org.lamisplus.modules.sync.domain.entity.Module;
import org.lamisplus.modules.sync.domain.entity.WebModule;
import org.lamisplus.modules.sync.domain.enumeration.ModuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WebModuleRepository extends JpaRepository<WebModule, Long> {
    List<WebModule> findByType(ModuleType type);

    List<WebModule> findByModule(Module module);

    List<WebModule> findByProvidesFor(WebModule webModule);

    @Query("select m from WebModule w join w.module m where m.active = true and w.providesFor is null")
    List<WebModule> findByProvidesForIn();

    @Query("select m from WebModule w join w.module m where m.active = true and w.providesFor is not null")
    List<WebModule> findAllProviders();
}
