package org.lamisplus.modules.sync.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.sync.controller.apierror.EntityNotFoundException;
import org.lamisplus.modules.sync.controller.apierror.RecordExistException;
import org.lamisplus.modules.sync.domain.dto.ApplicationCodesetDTO;
import org.lamisplus.modules.sync.domain.entity.ApplicationCodeSet;
import org.lamisplus.modules.sync.domain.mapper.ApplicationCodesetMapper;
import org.lamisplus.modules.sync.repository.ApplicationCodesetRepository;
import org.lamisplus.modules.sync.util.Constants.ArchiveStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ApplicationCodesetService {

    private final ApplicationCodesetRepository applicationCodesetRepository;
    private final ApplicationCodesetMapper applicationCodesetMapper;
    private final ArchiveStatus constant;

    public List<ApplicationCodesetDTO> getAllApplicationCodeset(){
        return applicationCodesetMapper.toApplicationCodesetDTOList(applicationCodesetRepository.findAllByArchivedNotOrderByIdAsc(constant.ARCHIVED));
    }

    public ApplicationCodeSet save(ApplicationCodesetDTO applicationCodesetDTO){
        Optional<ApplicationCodeSet> applicationCodesetOptional = applicationCodesetRepository.findByDisplayAndCodesetGroupAndArchived(applicationCodesetDTO.getDisplay(),
                applicationCodesetDTO.getCodesetGroup(), constant.UN_ARCHIVED);
        if (applicationCodesetOptional.isPresent()) {
            throw new RecordExistException(ApplicationCodeSet.class,"Display:",applicationCodesetDTO.getDisplay());
        }

        final ApplicationCodeSet applicationCodeset = applicationCodesetMapper.toApplicationCodeset(applicationCodesetDTO);
        applicationCodeset.setCode(UUID.randomUUID().toString());
        applicationCodeset.setArchived(constant.UN_ARCHIVED);

        return applicationCodesetRepository.save(applicationCodeset);
    }

    public List<ApplicationCodesetDTO> getApplicationCodeByCodesetGroup(String codeSetGroup){
        return applicationCodesetRepository.findAllByCodesetGroupAndArchivedOrderByIdAsc(codeSetGroup, constant.UN_ARCHIVED);
    }

    public ApplicationCodesetDTO getApplicationCodeset(Long id){
        final ApplicationCodeSet applicationCodeset = applicationCodesetRepository.findByIdAndArchived(id, constant.UN_ARCHIVED)
                .orElseThrow(() -> new EntityNotFoundException(ApplicationCodeSet.class,"Display:",id+""));

        return  applicationCodesetMapper.toApplicationCodesetDTO(applicationCodeset);
    }

    public ApplicationCodeSet update(Long id, ApplicationCodesetDTO applicationCodesetDTO){
        applicationCodesetRepository.findByIdAndArchivedNot(id, constant.ARCHIVED)
                .orElseThrow(() -> new EntityNotFoundException(ApplicationCodeSet.class,"Display:",id+""));

        final ApplicationCodeSet applicationCodeset = applicationCodesetMapper.toApplicationCodeset(applicationCodesetDTO);
        applicationCodeset.setId(id);
        if(applicationCodeset.getArchived() == null) {
            //deactivate the codeset, 1 is archived, 0 is unarchived, 2 is deactivate
            applicationCodeset.setArchived(constant.UN_ARCHIVED);
        }
        return applicationCodesetRepository.save(applicationCodeset);
    }

    public void delete(Long id){
        ApplicationCodeSet applicationCodeset = applicationCodesetRepository.findByIdAndArchived(id, constant.UN_ARCHIVED)
                .orElseThrow(() -> new EntityNotFoundException(ApplicationCodeSet.class,"Display:",id+""));
        applicationCodeset.setArchived(constant.ARCHIVED);
    }

    public Boolean exist(String display, String codesetGroup){
        return applicationCodesetRepository.existsByDisplayAndCodesetGroup(display, codesetGroup);
    }
}
