package org.lamisplus.modules.sync.repository;

import org.lamisplus.modules.sync.domain.entity.OrganisationUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrganisationUnitRepository extends JpaRepository<OrganisationUnit, Long> {
    List<OrganisationUnit> findAllOrganisationUnitByParentOrganisationUnitIdAndArchived(Long id, int archived);

    List<OrganisationUnit> findAllByParentOrganisationUnitIdAndOrganisationUnitLevelId(Long parentOrgUnitId, Long orgUnitLevelId);

    List<OrganisationUnit> findAllByOrganisationUnitLevelId(Long id);

    Optional<OrganisationUnit> findByNameAndParentOrganisationUnitIdAndArchived(String name, Long parentOrganisationUnitId, int archived);

    Optional<OrganisationUnit> findByIdAndArchived(Long id, int archived);

    Page<OrganisationUnit> findAllByArchivedOrderByIdAsc(int unarchived, Pageable pageable);

    List<OrganisationUnit> findAllByOrganisationUnitLevelIdIn(List<Long> organisationUnitLevelId);

    @Query(value = "SELECT id from organisation_unit WHERE name ilike ?1" +
            " AND description ilike '%local%'AND " +
            "parent_organisation_unit_id = (SELECT id from organisation_unit WHERE name = ?2 " +
            "AND organisation_unit_level_id=2)", nativeQuery = true)
    Long findByOrganisationDetails(String parentOrganisationUnitName, String parentsParentOrganisationUnitName);

    @Query(value = "SELECT name FROM organisation_unit WHERE organisation_unit_level_id=2", nativeQuery = true)
    List<String> findAllState();

    @Query(value = "SELECT name FROM organisation_unit WHERE organisation_unit_level_id=3", nativeQuery = true)
    List<String> findAllProvince();

    @Query(value = "select * from organisation_unit org where  org.id in (select distinct ps.organisation_unit_id from patient ps)", nativeQuery = true)
    List<OrganisationUnit> findOrganisationUnitWithRecords();

    //TODO: optimized to only give necessary fields ie id and name
    @Query(value = "SELECT * FROM organisation_unit ou WHERE ou.organisation_unit_level_id = ?1 " +
            "AND ou.archived = ?2", nativeQuery = true)
    List<OrganisationUnit> findByOrganisationsByLevelAndArchived(Long organisationUnitLevelId, int archived);
}
