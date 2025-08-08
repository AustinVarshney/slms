package com.java.slms.serviceImpl;

import com.java.slms.dto.FeeStructureRequestDTO;
import com.java.slms.dto.FeeStructureResponseDTO;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.ClassEntity;
import com.java.slms.model.FeeStructure;
import com.java.slms.repository.ClassEntityRepository;
import com.java.slms.repository.FeeStructureRepository;
import com.java.slms.service.FeeStructureService;
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

    @Override
    public FeeStructureResponseDTO createFeeStructure(FeeStructureRequestDTO dto)
    {
        log.info("Creating FeeStructure for Class ID: {} with FeeType: {}", dto.getClassId(), dto.getFeeType());

        // Step 1: Validate class exists
        ClassEntity classEntity = classEntityRepository.findById(dto.getClassId())
                .orElseThrow(() ->
                {
                    log.error("Class not found with ID: {}", dto.getClassId());
                    return new ResourceNotFoundException("Class not found with ID: " + dto.getClassId());
                });

        // Step 2: Check if FeeType already exists for this class
        boolean exists = feeStructureRepository
                .existsByFeeTypeIgnoreCaseAndClassEntity_Id(dto.getFeeType(), dto.getClassId());

        if (exists)
        {
            log.warn("FeeStructure with FeeType '{}' already exists for Class ID: {}", dto.getFeeType(), dto.getClassId());
            throw new AlreadyExistException(
                    "FeeType '" + dto.getFeeType() + "' already exists for Class ID: " + dto.getClassId()
            );
        }

        // Step 3: Map and save
        FeeStructure feeStructure = modelMapper.map(dto, FeeStructure.class);
        feeStructure.setClassEntity(classEntity);
        feeStructure.setId(null); // ensure create

        FeeStructure saved = feeStructureRepository.save(feeStructure);
        log.info("FeeStructure with ID {} created successfully for Class ID {}", saved.getId(), dto.getClassId());

        return modelMapper.map(saved, FeeStructureResponseDTO.class);
    }

    @Override
    public FeeStructureResponseDTO updateFeeStructure(Long id, FeeStructureRequestDTO dto)
    {
        log.info("Updating FeeStructure with ID: {}", id);

        FeeStructure existing = feeStructureRepository.findById(id)
                .orElseThrow(() ->
                {
                    log.error("FeeStructure not found with ID: {}", id);
                    return new ResourceNotFoundException("FeeStructure not found with ID: " + id);
                });

        log.debug("Current FeeStructure details: {}", existing);

        Long existingClassId = existing.getClassEntity().getId();
        if (dto.getClassId() != null && !existingClassId.equals(dto.getClassId()))
        {
            log.warn("Attempt to update FeeStructure ID {} with mismatched Class ID. Existing: {}, Provided: {}",
                    id, existingClassId, dto.getClassId());
            throw new ResourceNotFoundException("FeeStructure with ID: " + id +
                    " is not linked to Class ID: " + dto.getClassId());
        }

        if (dto.getFeeType() != null &&
                !dto.getFeeType().equalsIgnoreCase(existing.getFeeType()))
        {

            boolean exists = feeStructureRepository
                    .existsByFeeTypeIgnoreCaseAndClassEntity_Id(dto.getFeeType(), existingClassId);

            if (exists)
            {
                log.warn("FeeType '{}' already exists for Class ID: {}. Update aborted.",
                        dto.getFeeType(), existingClassId);
                throw new AlreadyExistException("FeeType '" + dto.getFeeType() +
                        "' already exists for Class ID: " + existingClassId);
            }
        }

        if (dto.getFeeType() != null) existing.setFeeType(dto.getFeeType());
        if (dto.getDefaultAmount() != null) existing.setDefaultAmount(dto.getDefaultAmount());
        if (dto.getDueDate() != null) existing.setDueDate(dto.getDueDate());

        log.debug("Updated FeeStructure fields: FeeType={}, DefaultAmount={}, DueDate={}",
                existing.getFeeType(), existing.getDefaultAmount(), existing.getDueDate());

        FeeStructure updated = feeStructureRepository.save(existing);

        log.info("FeeStructure with ID {} successfully updated", id);

        return modelMapper.map(updated, FeeStructureResponseDTO.class);
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
        return feeStructureRepository.findByClassEntityId(classId).stream().map(fs -> modelMapper.map(fs, FeeStructureResponseDTO.class)).toList();
    }

    @Override
    public List<FeeStructureResponseDTO> getFeeStructuresByFeeType(String feeType)
    {
        return feeStructureRepository.findByFeeTypeIgnoreCase(feeType).stream().map(fs -> modelMapper.map(fs, FeeStructureResponseDTO.class)).toList();
    }

    @Override
    public List<FeeStructureResponseDTO> getFeeStructuresByDueDateRange(Date startDate, Date endDate)
    {
        return feeStructureRepository.findByDueDateBetween(startDate, endDate).stream().map(fs -> modelMapper.map(fs, FeeStructureResponseDTO.class)).toList();
    }

    @Override
    public void deleteFeeStructure(Long id)
    {
        FeeStructure feeStructure = feeStructureRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("FeeStructure not found with ID: " + id));
        feeStructureRepository.delete(feeStructure);
    }
}
