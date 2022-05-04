package org.lamisplus.modules.sync.repository;

import org.lamisplus.modules.sync.domain.entity.Module;
import org.lamisplus.modules.sync.domain.entity.ModuleDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ModuleDependencyRepository extends JpaRepository<ModuleDependency, Long> {

    @Query("select m from ModuleDependency m where m.module = :module")
    List<ModuleDependency> findDependencies(@Param("module") Module module);

    @Query("select m from ModuleDependency m where m.dependency = :module")
    List<ModuleDependency> findDependents(@Param("module") Module module);
}
