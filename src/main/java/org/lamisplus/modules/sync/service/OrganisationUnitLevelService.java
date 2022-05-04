package org.lamisplus.modules.sync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.sync.controller.apierror.EntityNotFoundException;
import org.lamisplus.modules.sync.controller.apierror.IllegalTypeException;
import org.lamisplus.modules.sync.controller.apierror.RecordExistException;
import org.lamisplus.modules.sync.domain.dto.OrganisationUnitLevelDTO;
import org.lamisplus.modules.sync.domain.entity.OrganisationUnit;
import org.lamisplus.modules.sync.domain.entity.OrganisationUnitLevel;
import org.lamisplus.modules.sync.domain.mapper.OrganisationUnitLevelMapper;
import org.lamisplus.modules.sync.repository.OrganisationUnitLevelRepository;
import org.lamisplus.modules.sync.repository.OrganisationUnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.lamisplus.modules.sync.util.Constants.ArchiveStatus.UN_ARCHIVED;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class OrganisationUnitLevelService {
    private final OrganisationUnitLevelRepository organisationUnitLevelRepository;
    private final OrganisationUnitRepository organisationUnitRepository;
    private final OrganisationUnitLevelMapper organisationUnitLevelMapper;
    private final OrganisationUnitService organisationUnitService;


    public OrganisationUnitLevelDTO save(OrganisationUnitLevelDTO organisationUnitLevelDTO) {
        Optional<OrganisationUnitLevel> organizationOptional = organisationUnitLevelRepository.findByNameAndArchived(organisationUnitLevelDTO.getName(), UN_ARCHIVED);
        if(organizationOptional.isPresent())throw new RecordExistException(OrganisationUnitLevel.class, "Name", organisationUnitLevelDTO.getName() +"");

        OrganisationUnitLevel organisationUnitLevel = organisationUnitLevelRepository.findByIdAndArchived(organisationUnitLevelDTO.getParentOrganisationUnitLevelId(),
                UN_ARCHIVED).orElseThrow(() -> new EntityNotFoundException(OrganisationUnitLevel.class, "ParentOrganisationUnitLevel", organisationUnitLevelDTO.getParentOrganisationUnitLevelId()+""));

        //if has no subset is 0 while has subset is 1
        if(organisationUnitLevel.getStatus() == 0){
            throw new IllegalTypeException(OrganisationUnitLevel.class, "ParentOrganisationUnitLevel", "cannot have subset");
        }

        organisationUnitLevel = organisationUnitLevelMapper.toOrganisationUnitLevel(organisationUnitLevelDTO);
        organisationUnitLevel.setArchived(UN_ARCHIVED);
        organisationUnitLevelRepository.save(organisationUnitLevel);
        return organisationUnitLevelMapper.toOrganisationUnitLevelDTO(organisationUnitLevel);
    }

    public OrganisationUnitLevelDTO update(Long id, OrganisationUnitLevelDTO organisationUnitLevelDTO) {
        organisationUnitLevelRepository.findByIdAndArchived(id, UN_ARCHIVED)
                .orElseThrow(() -> new EntityNotFoundException(OrganisationUnitLevel.class, "Id", id +""));
        OrganisationUnitLevel organisationUnitLevel = organisationUnitLevelMapper.toOrganisationUnitLevel(organisationUnitLevelDTO);
        organisationUnitLevel.setId(id);
        organisationUnitLevel.setArchived(UN_ARCHIVED);
        organisationUnitLevelRepository.save(organisationUnitLevel);
        return organisationUnitLevelMapper.toOrganisationUnitLevelDTO(organisationUnitLevel);
    }

    /*public Integer delete(Long id) {
        Optional<OrganisationUnitLevel> organizationOptional = organisationUnitLevelRepository.findByIdAndArchived(id, UN_ARCHIVED);
        if (!organizationOptional.isPresent())throw new EntityNotFoundException(OrganisationUnitLevel.class, "Id", id +"");
        return organizationOptional.get().getArchived();
    }*/

    public OrganisationUnitLevelDTO getOrganizationUnitLevel(Long id){
        OrganisationUnitLevel organisationUnitLevel = organisationUnitLevelRepository.findByIdAndArchived(id, UN_ARCHIVED)
                .orElseThrow(() -> new EntityNotFoundException(OrganisationUnitLevel.class, "Id", id +""));
        OrganisationUnitLevelDTO organisationUnitLevelDTO = organisationUnitLevelMapper.toOrganisationUnitLevelDTO(organisationUnitLevel);
        return organisationUnitLevelDTO;
    }

    public List<OrganisationUnitLevelDTO> getAllOrganizationUnitLevel(Integer status) {
        if(status != null && status < 2){
            return organisationUnitLevelMapper.toOrganisationUnitLevelDTOList(
                    organisationUnitLevelRepository.findAllByStatusAndArchivedOrderByIdAsc(status, UN_ARCHIVED));
        }
        return organisationUnitLevelMapper.toOrganisationUnitLevelDTOList(organisationUnitLevelRepository.findAllByArchivedOrderByIdAsc(UN_ARCHIVED));
    }

    public List<OrganisationUnit> getAllOrganisationUnitsByOrganizationUnitLevel(Long id) {
        organisationUnitLevelRepository.findByIdAndArchived(id, UN_ARCHIVED)
                .orElseThrow(() -> new EntityNotFoundException(OrganisationUnitLevel.class, "Id", id +""));
        return organisationUnitRepository.findByOrganisationsByLevelAndArchived(id, UN_ARCHIVED)
                .stream()
                .map(organisationUnit -> organisationUnitService.findOrganisationUnits(organisationUnit, organisationUnit.getId()))
                .collect(Collectors.toList());
    }

    public List<OrganisationUnit> getAllParentOrganisationUnitsByOrganizationUnitLevel(Long id) {
        OrganisationUnitLevel organisationUnitLevel = organisationUnitLevelRepository.findByIdAndArchived(id, UN_ARCHIVED)
                .orElseThrow(() -> new EntityNotFoundException(OrganisationUnitLevel.class, "Id", id +""));

        if(organisationUnitLevel.getParentOrganisationUnitLevelId() == null || organisationUnitLevel.getParentOrganisationUnitLevelId() == 0){
            throw new EntityNotFoundException(OrganisationUnitLevel.class, organisationUnitLevel.getName(), "has no parent");
        }
        return organisationUnitRepository.findByOrganisationsByLevelAndArchived(organisationUnitLevel.getParentOrganisationUnitLevelId(), UN_ARCHIVED)
                .stream()
                .map(organisationUnit -> organisationUnitService.findOrganisationUnits(organisationUnit, organisationUnit.getId()))
                .collect(Collectors.toList());
    }
}
