package com.java.slms.serviceImpl;

import com.java.slms.dto.UserRequest;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.NonTeachingStaff;
import com.java.slms.model.User;
import com.java.slms.repository.NonTeachingStaffRepository;
import com.java.slms.repository.UserRepository;
import com.java.slms.service.FeeStaffService;
import com.java.slms.util.EntityFetcher;
import com.java.slms.util.RoleEnum;
import com.java.slms.util.UserStatus;
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

    private final NonTeachingStaffRepository nonTeachingStaffRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public UserRequest createFeeStaff(UserRequest feeStaffDto)
    {
        log.info("Creating fee staff with email: {}", feeStaffDto.getEmail());

        if (nonTeachingStaffRepository.findByEmailIgnoreCase(feeStaffDto.getEmail()).isPresent())
        {
            throw new AlreadyExistException("FeeStaff already exists with email: " + feeStaffDto.getEmail());
        }

        NonTeachingStaff nonTeachingStaff = modelMapper.map(feeStaffDto, NonTeachingStaff.class);
        nonTeachingStaff.setId(null);
        nonTeachingStaff.setStatus(UserStatus.ACTIVE);
        NonTeachingStaff savedNonTeachingStaff = nonTeachingStaffRepository.save(nonTeachingStaff);

        log.info("FeeStaff created with ID: {}", savedNonTeachingStaff.getId());
        return convertToDto(savedNonTeachingStaff);
    }

    @Override
    public UserRequest getFeeStaffById(Long id)
    {
        log.info("Fetching fee staff with ID: {}", id);
        NonTeachingStaff nonTeachingStaff = nonTeachingStaffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeeStaff not found with ID: " + id));

        return convertToDto(nonTeachingStaff);
    }

    @Override
    public List<UserRequest> getAllFeeStaff()
    {
        log.info("Fetching all fee staff");
        return nonTeachingStaffRepository.findAll().stream()
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

        List<NonTeachingStaff> nonTeachingStaffList = nonTeachingStaffRepository.findByEmailIn(emails);
        return nonTeachingStaffList.stream().map(this::convertToDto).toList();
    }

    @Override
    public UserRequest updateFeeStaff(Long id, UserRequest feeStaffDto)
    {
        log.info("Updating fee staff with ID: {}", id);
        NonTeachingStaff existingNonTeachingStaff = nonTeachingStaffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeeStaff not found with ID: " + id));

        modelMapper.map(feeStaffDto, existingNonTeachingStaff);
        NonTeachingStaff updatedNonTeachingStaff = nonTeachingStaffRepository.save(existingNonTeachingStaff);

        log.info("FeeStaff updated successfully: {}", id);
        return convertToDto(updatedNonTeachingStaff);
    }

    @Transactional
    @Override
    public void inActiveNonTeachingStaff(Long id)
    {
        NonTeachingStaff staff = nonTeachingStaffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NonTeachingStaff not found with ID: " + id));

        if (staff.getStatus() == UserStatus.INACTIVE)
        {
            throw new AlreadyExistException("NonTeachingStaff is already inactive");
        }

        User user = staff.getUser();
        staff.setStatus(UserStatus.INACTIVE);
        nonTeachingStaffRepository.save(staff);
        EntityFetcher.removeRoleFromUser(user.getId(), RoleEnum.ROLE_NON_TEACHING_STAFF, userRepository);
        log.info("Marked NonTeachingStaff inactive with ID: {}", id);
    }


    private UserRequest convertToDto(NonTeachingStaff nonTeachingStaff)
    {
        UserRequest dto = modelMapper.map(nonTeachingStaff, UserRequest.class);

        // Set status from user entity if exists
        dto.setCreatedAt(nonTeachingStaff.getCreatedAt());
        dto.setUpdatedAt(nonTeachingStaff.getUpdatedAt());
        dto.setDeletedAt(nonTeachingStaff.getDeletedAt());

        return dto;
    }
}
