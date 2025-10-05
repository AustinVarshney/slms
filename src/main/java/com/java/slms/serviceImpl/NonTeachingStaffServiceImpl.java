package com.java.slms.serviceImpl;

import com.java.slms.dto.UserRequest;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.NonTeachingStaff;
import com.java.slms.model.User;
import com.java.slms.repository.NonTeachingStaffRepository;
import com.java.slms.repository.UserRepository;
import com.java.slms.service.NonTeachingStaffService;
import com.java.slms.util.EntityFetcher;
import com.java.slms.util.RoleEnum;
import com.java.slms.util.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NonTeachingStaffServiceImpl implements NonTeachingStaffService
{

    private final NonTeachingStaffRepository nonTeachingStaffRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public UserRequest createFeeStaff(UserRequest feeStaffDto)
    {
        log.info("Creating fee staff with email: {}", feeStaffDto.getEmail());

        nonTeachingStaffRepository.findByEmailIgnoreCase(feeStaffDto.getEmail()).ifPresent(existing ->
        {
            throw new AlreadyExistException("FeeStaff already exists with email: " + feeStaffDto.getEmail());
        });

        NonTeachingStaff nonTeachingStaff = modelMapper.map(feeStaffDto, NonTeachingStaff.class);
        nonTeachingStaff.setId(null); // Ensure creation of new entity
        nonTeachingStaff.setStatus(UserStatus.ACTIVE);

        NonTeachingStaff saved = nonTeachingStaffRepository.save(nonTeachingStaff);
        log.info("FeeStaff created successfully with ID: {}", saved.getId());

        return convertToDto(saved);
    }

    @Override
    public UserRequest getFeeStaffById(Long id)
    {
        log.info("Fetching fee staff with ID: {}", id);
        return convertToDto(fetchNonTeachingStaffById(id));
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

        List<String> activeEmails = userRepository.findByEmailIsNotNullAndEnabledTrue().stream()
                .map(User::getEmail)
                .filter(Objects::nonNull)
                .toList();

        return nonTeachingStaffRepository.findByEmailIn(activeEmails).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public UserRequest updateFeeStaff(Long id, UserRequest feeStaffDto)
    {
        log.info("Updating fee staff with ID: {}", id);

        NonTeachingStaff existing = fetchNonTeachingStaffById(id);
        modelMapper.map(feeStaffDto, existing); // Apply new values

        NonTeachingStaff updated = nonTeachingStaffRepository.save(existing);
        log.info("FeeStaff updated successfully with ID: {}", updated.getId());

        return convertToDto(updated);
    }

    @Transactional
    @Override
    public void inActiveNonTeachingStaff(Long id)
    {
        log.info("Deactivating non-teaching staff with ID: {}", id);

        NonTeachingStaff staff = fetchNonTeachingStaffById(id);

        if (UserStatus.INACTIVE.equals(staff.getStatus()))
        {
            log.warn("NonTeachingStaff with ID {} is already inactive", id);
            return; // Return early if already inactive (idempotent)
        }

        staff.setStatus(UserStatus.INACTIVE);
        nonTeachingStaffRepository.save(staff);

        User user = staff.getUser();
        EntityFetcher.removeRoleFromUser(user.getId(), RoleEnum.ROLE_NON_TEACHING_STAFF, userRepository);

        log.info("NonTeachingStaff marked as inactive with ID: {}", id);
    }

    @Transactional
    @Override
    public void activateNonTeachingStaff(Long id)
    {
        log.info("Activating non-teaching staff with ID: {}", id);

        NonTeachingStaff staff = fetchNonTeachingStaffById(id);

        if (UserStatus.ACTIVE.equals(staff.getStatus()))
        {
            log.warn("NonTeachingStaff with ID {} is already active", id);
            return; // Return early if already active (idempotent)
        }

        staff.setStatus(UserStatus.ACTIVE);
        nonTeachingStaffRepository.save(staff);

        User user = staff.getUser();
        EntityFetcher.addRoleToUser(user.getId(), RoleEnum.ROLE_NON_TEACHING_STAFF, userRepository);

        log.info("NonTeachingStaff marked as active with ID: {}", id);
    }

    private NonTeachingStaff fetchNonTeachingStaffById(Long id)
    {
        return nonTeachingStaffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NonTeachingStaff not found with ID: " + id));
    }

    private UserRequest convertToDto(NonTeachingStaff staff)
    {
        UserRequest dto = modelMapper.map(staff, UserRequest.class);
        dto.setCreatedAt(staff.getCreatedAt());
        dto.setUpdatedAt(staff.getUpdatedAt());
        dto.setDeletedAt(staff.getDeletedAt());
        return dto;
    }
}
