package org.lamisplus.modules.sync.repository;

import org.lamisplus.modules.sync.domain.entity.Module;
import org.lamisplus.modules.sync.domain.entity.ModuleArtifact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModuleArtifactRepository extends JpaRepository<ModuleArtifact, Long> {
    Optional<ModuleArtifact> findByModule(Module module);
}
