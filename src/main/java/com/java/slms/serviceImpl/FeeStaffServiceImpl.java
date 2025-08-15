package com.java.slms.serviceImpl;

import com.java.slms.dto.UserRequest;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.FeeStaff;
import com.java.slms.model.User;
import com.java.slms.repository.FeeStaffRepository;
import com.java.slms.repository.UserRepository;
import com.java.slms.service.FeeStaffService;
import com.java.slms.util.UserStatuses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeeStaffServiceImpl implements FeeStaffService
{

    private final FeeStaffRepository feeStaffRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public UserRequest createFeeStaff(UserRequest feeStaffDto)
    {
        log.info("Creating fee staff with email: {}", feeStaffDto.getEmail());

        if (feeStaffRepository.findByEmailIgnoreCase(feeStaffDto.getEmail()).isPresent())
        {
            throw new AlreadyExistException("FeeStaff already exists with email: " + feeStaffDto.getEmail());
        }

        FeeStaff feeStaff = modelMapper.map(feeStaffDto, FeeStaff.class);
        feeStaff.setId(null);
        FeeStaff savedFeeStaff = feeStaffRepository.save(feeStaff);

        log.info("FeeStaff created with ID: {}", savedFeeStaff.getId());
        return convertToDto(savedFeeStaff);
    }

    @Override
    public UserRequest getFeeStaffById(Long id)
    {
        log.info("Fetching fee staff with ID: {}", id);
        FeeStaff feeStaff = feeStaffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeeStaff not found with ID: " + id));

        return convertToDto(feeStaff);
    }

    @Override
    public List<UserRequest> getAllFeeStaff()
    {
        log.info("Fetching all fee staff");
        return feeStaffRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserRequest> getActiveFeeStaff()
    {
        log.info("Fetching active fee staff");
        List<User> activeUsers = userRepository.findByEmailIsNotNullAndEnabledTrue();
        List<String> emails = activeUsers.stream()
                .map(User::getEmail)
                .filter(Objects::nonNull)
                .toList();

        List<FeeStaff> feeStaffList = feeStaffRepository.findByEmailIn(emails);
        return feeStaffList.stream().map(this::convertToDto).toList();
    }

    @Override
    public UserRequest updateFeeStaff(Long id, UserRequest feeStaffDto)
    {
        log.info("Updating fee staff with ID: {}", id);
        FeeStaff existingFeeStaff = feeStaffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeeStaff not found with ID: " + id));

        modelMapper.map(feeStaffDto, existingFeeStaff);
        FeeStaff updatedFeeStaff = feeStaffRepository.save(existingFeeStaff);

        log.info("FeeStaff updated successfully: {}", id);
        return convertToDto(updatedFeeStaff);
    }

    @Override
    @Transactional
    public void deleteFeeStaff(Long id)
    {
        FeeStaff feeStaff = feeStaffRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("FeeStaff not found with ID: " + id));
        feeStaffRepository.delete(feeStaff);
        log.info("Deleted fee staff record with ID: {}", id);
    }

    private UserRequest convertToDto(FeeStaff feeStaff)
    {
        UserRequest dto = modelMapper.map(feeStaff, UserRequest.class);

        // Set status from user entity if exists
        userRepository.findByEmailIgnoreCase(feeStaff.getEmail()).ifPresent(user ->
        {
            dto.setStatus(user.isEnabled() ? UserStatuses.ACTIVE : UserStatuses.INACTIVE);
            dto.setUserId(user.getId());
        });

        dto.setCreatedAt(feeStaff.getCreatedAt());
        dto.setUpdatedAt(feeStaff.getUpdatedAt());
        dto.setDeletedAt(feeStaff.getDeletedAt());

        return dto;
    }
}
