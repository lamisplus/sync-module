package org.lamisplus.modules.sync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.sync.controller.apierror.EntityNotFoundException;
import org.lamisplus.modules.sync.controller.apierror.IllegalTypeException;
import org.lamisplus.modules.sync.controller.apierror.RecordExistException;
import org.lamisplus.modules.sync.domain.dto.OrganisationUnitDTO;
import org.lamisplus.modules.sync.domain.entity.OrganisationUnit;
import org.lamisplus.modules.sync.domain.entity.OrganisationUnitHierarchy;
import org.lamisplus.modules.sync.domain.entity.OrganisationUnitLevel;
import org.lamisplus.modules.sync.domain.mapper.OrganisationUnitMapper;
import org.lamisplus.modules.sync.repository.OrganisationUnitHierarchyRepository;
import org.lamisplus.modules.sync.repository.OrganisationUnitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.lamisplus.modules.sync.util.Constants.ArchiveStatus.ARCHIVED;
import static org.lamisplus.modules.sync.util.Constants.ArchiveStatus.UN_ARCHIVED;

//import org.audit4j.core.util.Log;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class OrganisationUnitService {
    private static final Long FIRST_ORG_LEVEL = 1L;
    private final OrganisationUnitRepository organisationUnitRepository;
    private final OrganisationUnitMapper organisationUnitMapper;
    private final OrganisationUnitHierarchyRepository organisationUnitHierarchyRepository;

    public List<OrganisationUnit> save(Long parentOrganisationUnitId, Long organisationUnitLevelId, List<OrganisationUnitDTO> organisationUnitDTOS) {
        List<OrganisationUnit> organisationUnits = new ArrayList<>();

        OrganisationUnit orgUnit = organisationUnitRepository.findByIdAndArchived(parentOrganisationUnitId,
                UN_ARCHIVED).orElseThrow(() -> new EntityNotFoundException(OrganisationUnitLevel.class, "parentOrganisationUnitId", parentOrganisationUnitId+""));

        OrganisationUnitLevel organisationUnitLevel = orgUnit.getOrganisationUnitLevelByOrganisationUnitLevelId();
        //if has no subset is 0 while has subset is 1
        if(organisationUnitLevel.getStatus() == 0){
            throw new IllegalTypeException(OrganisationUnitLevel.class, "parentOrganisationUnitLevel", organisationUnitLevel+" cannot have subset");
        }

        //loop to extract organisationUnit from dto and save
        organisationUnitDTOS.forEach(organisationUnitDTO -> {
            Optional<OrganisationUnit> organizationOptional = organisationUnitRepository.findByNameAndParentOrganisationUnitIdAndArchived(organisationUnitDTO.getName(), organisationUnitDTO.getId(), UN_ARCHIVED);
            if(organizationOptional.isPresent())throw new RecordExistException(OrganisationUnit.class, "Name", organisationUnitDTO.getName() +"");
            final OrganisationUnit organisationUnit = organisationUnitMapper.toOrganisationUnit(organisationUnitDTO);
            Long level = organisationUnitLevelId;
            //Set parentId
            organisationUnit.setParentOrganisationUnitId(parentOrganisationUnitId);
            //Set organisation unit level
            organisationUnit.setOrganisationUnitLevelId(organisationUnitLevelId);
            //Save organisation unit
            OrganisationUnit organisationUnit1 = organisationUnitRepository.save(organisationUnit);

            List<OrganisationUnitHierarchy> organisationUnitHierarchies = new ArrayList<>();
            OrganisationUnit returnOrgUnit = organisationUnit1;

            Long parent_org_unit_id = 1L;
            while(parent_org_unit_id > 0){
                parent_org_unit_id = organisationUnit1.getParentOrganisationUnitId();
                organisationUnitHierarchies.add(new OrganisationUnitHierarchy(null, returnOrgUnit.getId(), organisationUnit1.getParentOrganisationUnitId(),
                        level, null, null, null));

                Optional<OrganisationUnit> organisationUnitOptional = organisationUnitRepository.findById(organisationUnit1.getParentOrganisationUnitId());
                if(organisationUnitOptional.isPresent()){
                    organisationUnit1 = organisationUnitOptional.get();
                }
                --parent_org_unit_id;
            }
            organisationUnitHierarchyRepository.saveAll(organisationUnitHierarchies);
            organisationUnits.add(returnOrgUnit);
        });
        return organisationUnits;
    }

    //TODO: work on update organisation unit
    public OrganisationUnit update(Long id, OrganisationUnitDTO organisationUnitDTO) {
        Optional<OrganisationUnit> organizationOptional = organisationUnitRepository.findByIdAndArchived(id, UN_ARCHIVED);
        if(!organizationOptional.isPresent())throw new EntityNotFoundException(OrganisationUnit.class, "Id", id +"");
        final OrganisationUnit organisationUnit = organisationUnitMapper.toOrganisationUnit(organisationUnitDTO);

        organisationUnit.setId(id);
        return organisationUnitRepository.save(organisationUnit);
    }

    public Integer delete(Long id) {
        Optional<OrganisationUnit> organizationOptional = organisationUnitRepository.findByIdAndArchived(id, UN_ARCHIVED);
        if (!organizationOptional.isPresent())throw new EntityNotFoundException(OrganisationUnit.class, "Id", id +"");
        organizationOptional.get().setArchived(ARCHIVED);
        return organizationOptional.get().getArchived();
    }

    public OrganisationUnit getOrganizationUnit(Long id){
        Optional<OrganisationUnit> organizationOptional = organisationUnitRepository.findByIdAndArchived(id, UN_ARCHIVED);
        if (!organizationOptional.isPresent())throw new EntityNotFoundException(OrganisationUnit.class, "Id", id +"");
        return organizationOptional.get();
    }

    public List<OrganisationUnit> getOrganisationUnitByParentOrganisationUnitId(Long id) {
        return  organisationUnitRepository.findAllOrganisationUnitByParentOrganisationUnitIdAndArchived(id, UN_ARCHIVED);
    }

    public Page<OrganisationUnit> getAllOrganizationUnit(Pageable pageable) {
        return organisationUnitRepository.findAllByArchivedOrderByIdAsc(UN_ARCHIVED, pageable);
    }

    public List<OrganisationUnit> getAllOrganizationUnit(Page<OrganisationUnit> organisationUnitPage) {
        List<OrganisationUnit> organisationUnits = new ArrayList<>();
        organisationUnitPage.getContent().forEach(organisationUnit -> {
            organisationUnits.add( this.findOrganisationUnits(organisationUnit, organisationUnit.getId()));
        });
        return organisationUnits;
    }

    /*public List<OrganisationUnit> getOrganisationUnitByParentOrganisationUnitIdAndOrganisationUnitLevelId(Long parentOrgUnitId, Long orgUnitLevelId) {
        OrganisationUnit parentOrganisationUnit = organisationUnitRepository.findByIdAndArchived(parentOrgUnitId, UN_ARCHIVED).orElseThrow(
                () -> new EntityNotFoundException(OrganisationUnit.class, "Parent OrganisationUnit", "invalid"));

            List<OrganisationUnit> organisationUnits = new ArrayList<>();
        organisationUnitRepository.findAllByParentOrganisationUnitIdAndOrganisationUnitLevelId(parentOrgUnitId, orgUnitLevelId).forEach(organisationUnit -> {
            organisationUnit.setParentOrganisationUnitName(parentOrganisationUnit.getName());
            organisationUnits.add(organisationUnit);
        });
        return organisationUnits;
    }*/

    /*public List<OrganisationUnit> getOrganisationUnitByOrganisationUnitLevelId(Long id) {
        List<OrganisationUnit> organisationUnits = new ArrayList<>();
        organisationUnitRepository.findAllByOrganisationUnitLevelId(id).forEach(organisationUnit -> {
            Long orgUnitId = organisationUnit.getParentOrganisationUnitId();
            organisationUnits.add(this.findOrganisationUnits(organisationUnit, orgUnitId));
        });

        return organisationUnits;
    }*/

    public List<OrganisationUnitDTO> getOrganisationUnitSubsetByParentOrganisationUnitIdAndOrganisationUnitLevelId(Long parent_org_unit_id, Long org_unit_level_id) {
        List<OrganisationUnitHierarchy> organisationUnitHierarchies = organisationUnitHierarchyRepository.findAllByParentOrganisationUnitIdAndOrganisationUnitLevelId(parent_org_unit_id, org_unit_level_id);
        List<OrganisationUnitDTO> organisationUnitDTOS = new ArrayList<>();
        organisationUnitHierarchies.forEach(organisationUnitHierarchy -> {
            OrganisationUnit organisationUnit = organisationUnitHierarchy.getOrganisationUnitByOrganisationUnitId();
            Long orgUnitId = organisationUnit.getParentOrganisationUnitId();
            organisationUnitDTOS.add(organisationUnitMapper.toOrganisationUnitDTO(this.findOrganisationUnits(organisationUnit, orgUnitId)));
        });
        return organisationUnitDTOS;
    }

    public List<OrganisationUnit> getAllOrganisationUnitByOrganisationUnitLevelId(Long organisationUnitLevelId) {
        List<Long> levels = new ArrayList<>();
        for(Long i = FIRST_ORG_LEVEL; i < organisationUnitLevelId; i++){
            levels.add(i);
        }
        return organisationUnitRepository.findAllByOrganisationUnitLevelIdIn(levels);
    }

    public OrganisationUnit findOrganisationUnits(OrganisationUnit organisationUnit, Long orgUnitId){
        for(int i=0; i<2; i++) {
            Optional<OrganisationUnit> optionalOrganisationUnit = organisationUnitRepository.findByIdAndArchived(orgUnitId, UN_ARCHIVED);
            if(optionalOrganisationUnit.isPresent()){
                if(organisationUnit.getParentOrganisationUnitName() == null) {
                    organisationUnit.setParentOrganisationUnitName(optionalOrganisationUnit.get().getName());
                }else if(organisationUnit.getParentParentOrganisationUnitName() == null) {
                    organisationUnit.setParentParentOrganisationUnitName(optionalOrganisationUnit.get().getName());
                }
                orgUnitId = optionalOrganisationUnit.get().getParentOrganisationUnitId();
            }
        }
        return organisationUnit;
    }
}
