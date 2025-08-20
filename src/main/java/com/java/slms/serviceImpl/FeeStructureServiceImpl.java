package com.java.slms.serviceImpl;

import com.java.slms.dto.FeeStructureRequestDTO;
import com.java.slms.dto.FeeStructureResponseDTO;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.ClassEntity;
import com.java.slms.model.FeeStructure;
import com.java.slms.model.Session;
import com.java.slms.repository.ClassEntityRepository;
import com.java.slms.repository.FeeStructureRepository;
import com.java.slms.repository.SessionRepository;
import com.java.slms.service.FeeStructureService;
import com.java.slms.util.EntityFetcher;
import com.java.slms.util.EntityNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeeStructureServiceImpl implements FeeStructureService
{

    private final FeeStructureRepository feeStructureRepository;
    private final ClassEntityRepository classEntityRepository;
    private final ModelMapper modelMapper;
    private final SessionRepository sessionRepository;

    @Override
    public FeeStructureResponseDTO createFeeStructure(FeeStructureRequestDTO dto)
    {
        log.info("Creating FeeStructure for Class ID: {}", dto.getClassId());

        ClassEntity classEntity = classEntityRepository.findById(dto.getClassId())
                .orElseThrow(() ->
                {
                    log.error("Class not found with ID: {}", dto.getClassId());
                    return new ResourceNotFoundException("Class not found with ID: " + dto.getClassId());
                });


        if (feeStructureRepository.existsByClassEntity_IdAndSession_Id(dto.getClassId(), dto.getSessionId()))
        {
            throw new AlreadyExistException("Fee structure already exists for this class and session.");
        }

        Session session = EntityFetcher.fetchByIdOrThrow(
                sessionRepository,
                dto.getSessionId(),
                EntityNames.SESSION
        );

        FeeStructure feeStructure = modelMapper.map(dto, FeeStructure.class);
        feeStructure.setClassEntity(classEntity);
        feeStructure.setSession(session);
        feeStructure.setId(null);

        FeeStructure saved = feeStructureRepository.save(feeStructure);
        log.info("FeeStructure with ID {} created successfully for Class ID {}", saved.getId(), dto.getClassId());

        FeeStructureResponseDTO responseDTO = modelMapper.map(saved, FeeStructureResponseDTO.class);
        responseDTO.setSessionId(saved.getSession().getId());
        responseDTO.setSessionName(saved.getSession().getName());
        responseDTO.setClassId(saved.getClassEntity().getId());
        responseDTO.setClassName(saved.getClassEntity().getClassName());

        return responseDTO;
    }

    @Override
    public FeeStructureResponseDTO updateFeeStructure(Long id, FeeStructureRequestDTO dto)
    {
//        log.info("Updating FeeStructure with ID: {}", id);
//
//        FeeStructure existing = feeStructureRepository.findById(id)
//                .orElseThrow(() ->
//                {
//                    log.error("FeeStructure not found with ID: {}", id);
//                    return new ResourceNotFoundException("FeeStructure not found with ID: " + id);
//                });
//
//        log.debug("Current FeeStructure details: {}", existing);
//
//        Long existingClassId = existing.getClassEntity().getId();
//        if (dto.getClassId() != null && !existingClassId.equals(dto.getClassId()))
//        {
//            log.warn("Attempt to update FeeStructure ID {} with mismatched Class ID. Existing: {}, Provided: {}",
//                    id, existingClassId, dto.getClassId());
//            throw new ResourceNotFoundException("FeeStructure with ID: " + id +
//                    " is not linked to Class ID: " + dto.getClassId());
//        }
//
//        if (dto.getFeeType() != null &&
//                !dto.getFeeType().equalsIgnoreCase(existing.getFeeType()))
//        {
//
//            boolean exists = feeStructureRepository
//                    .existsByClassEntity_Id(dto.getFeeType(), existingClassId);
//
//            if (exists)
//            {
//                log.warn("FeeType '{}' already exists for Class ID: {}. Update aborted.",
//                        dto.getFeeType(), existingClassId);
//                throw new AlreadyExistException("FeeType '" + dto.getFeeType() +
//                        "' already exists for Class ID: " + existingClassId);
//            }
//        }

//        if (dto.getFeeType() != null) existing.setFeeType(dto.getFeeType());
//        if (dto.getDefaultAmount() != null) existing.setAmount(dto.getDefaultAmount());
//        if (dto.getDueDate() != null) existing.setDueDate(dto.getDueDate());
//
//        log.debug("Updated FeeStructure fields: FeeType={}, DefaultAmount={}, DueDate={}",
//                existing.getFeeType(), existing.getAmount(), existing.getDueDate());
//
//        FeeStructure updated = feeStructureRepository.save(existing);
//
//        log.info("FeeStructure with ID {} successfully updated", id);
//
//        return modelMapper.map(updated, FeeStructureResponseDTO.class);
        return null;
    }

    @Override
    public FeeStructureResponseDTO getFeeStructureById(Long id)
    {
        FeeStructure feeStructure = feeStructureRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("FeeStructure not found with ID: " + id));
        return modelMapper.map(feeStructure, FeeStructureResponseDTO.class);
    }

    @Override
    public List<FeeStructureResponseDTO> getFeeStructuresByClassId(Long classId)
    {
        return null;
//        return feeStructureRepository.findByClassEntityId(classId).stream().map(fs -> modelMapper.map(fs, FeeStructureResponseDTO.class)).toList();
    }

    @Override
    public List<FeeStructureResponseDTO> getFeeStructuresByFeeType(String feeType)
    {
        return null;
//        return feeStructureRepository.findByFeeTypeIgnoreCase(feeType).stream().map(fs -> modelMapper.map(fs, FeeStructureResponseDTO.class)).toList();
    }

    @Override
    public List<FeeStructureResponseDTO> getFeeStructuresByDueDateRange(Date startDate, Date endDate)
    {
        return null;
//        return feeStructureRepository.findByDueDateBetween(startDate, endDate).stream().map(fs -> modelMapper.map(fs, FeeStructureResponseDTO.class)).toList();
    }

    @Override
    public void deleteFeeStructure(Long id)
    {
        FeeStructure feeStructure = feeStructureRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("FeeStructure not found with ID: " + id));
        feeStructureRepository.delete(feeStructure);
    }
}
