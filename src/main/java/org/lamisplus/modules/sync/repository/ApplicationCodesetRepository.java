package org.lamisplus.modules.sync.repository;


import org.lamisplus.modules.sync.domain.dto.ApplicationCodesetDTO;
import org.lamisplus.modules.sync.domain.entity.ApplicationCodeSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationCodesetRepository extends JpaRepository<ApplicationCodeSet, Long>, JpaSpecificationExecutor {

    Optional<ApplicationCodeSet> findByDisplayAndCodesetGroup(String display, String codeSetGroup);

     ApplicationCodeSet findByDisplay(String display);

    Boolean existsByDisplayAndCodesetGroup(String display, String codesetGroup);

    Optional<ApplicationCodeSet> findByDisplayAndCodesetGroupAndArchived(String display, String codesetGroup, Integer active);

    Optional<ApplicationCodeSet> findByIdAndArchived(Long id, int archive);

    Optional<ApplicationCodeSet> findByIdAndArchivedNot(Long id, int archive);


    List<ApplicationCodeSet> findAllByArchivedOrderByIdAsc(int archived);

    List<ApplicationCodeSet> findAllByArchivedNotOrderByIdAsc(int archived);

    @Query("SELECT DISTINCT new org.lamisplus.modules.sync.domain.dto.ApplicationCodesetDTO" +
            "(a.id, a.display, a.code) FROM ApplicationCodeSet a WHERE a.codesetGroup = ?1 and a.archived = ?2")
    List<ApplicationCodesetDTO> findAllByCodesetGroupAndArchivedOrderByIdAsc(String codeSetGroup, int archived);

    @Query(value = "SELECT display FROM application_codeset WHERE codeset_group='GENDER'", nativeQuery = true)
    List<String> findAllGender();
}
