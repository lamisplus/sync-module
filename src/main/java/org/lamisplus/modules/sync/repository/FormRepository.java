package org.lamisplus.modules.sync.repository;

import org.lamisplus.modules.sync.domain.entity.Form;
import org.lamisplus.modules.sync.domain.entity.Module;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FormRepository extends JpaRepository<Form, String> {
    @Query("select t from Form t join t.module m where t.name = ?1 and m.active = true order by t.priority desc")
    List<Form> findByName(String name, Pageable pageable);

    List<Form> findByNameAndModule(String name, Module module);

    List<Form> findByModule_ActiveTrue();
}
