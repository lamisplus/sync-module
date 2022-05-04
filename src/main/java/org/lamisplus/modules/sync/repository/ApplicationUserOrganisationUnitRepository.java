package org.lamisplus.modules.sync.repository;

import org.lamisplus.modules.sync.domain.entity.ApplicationUserOrganisationUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationUserOrganisationUnitRepository extends JpaRepository<ApplicationUserOrganisationUnit, Long> , JpaSpecificationExecutor {
    Optional<ApplicationUserOrganisationUnit> findByApplicationUserIdAndOrganisationUnitId(Long applicationUserId, Long organisationUnitId);

    Optional<ApplicationUserOrganisationUnit> findOneByApplicationUserIdAndOrganisationUnitIdAndArchived(Long applicationUserId, Long organisationUnitId, int archived);

    Optional<ApplicationUserOrganisationUnit> findByIdAndArchived(Long id, int archived);

    List<ApplicationUserOrganisationUnit> findAllByArchived(int archived);

    List<ApplicationUserOrganisationUnit> findAllByApplicationUserIdAndArchived(Long applicationUserId, int archived);

}
