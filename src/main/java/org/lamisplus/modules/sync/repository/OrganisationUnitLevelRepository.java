package org.lamisplus.modules.sync.repository;

import org.lamisplus.modules.sync.domain.entity.OrganisationUnitLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganisationUnitLevelRepository extends JpaRepository<OrganisationUnitLevel, Long> {

    Optional<OrganisationUnitLevel> findByIdAndArchived(Long id, int archived);

    Optional<OrganisationUnitLevel> findByNameAndArchived(String name, int archived);

    List<OrganisationUnitLevel> findAllByArchivedOrderByIdAsc(int archived);

    Optional<OrganisationUnitLevel> findByParentOrganisationUnitLevelIdAndArchived(Long parentOrganisationUnitLevelId, int archived);

    List<OrganisationUnitLevel> findAllByStatusAndArchivedOrderByIdAsc(Integer status, int archived);
}
