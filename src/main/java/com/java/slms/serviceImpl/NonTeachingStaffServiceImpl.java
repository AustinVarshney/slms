package com.java.slms.serviceImpl;

import com.java.slms.dto.UserRequest;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.*;
import com.java.slms.repository.*;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NonTeachingStaffServiceImpl implements NonTeachingStaffService
{
    private final NonTeachingStaffRepository nonTeachingStaffRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final SchoolRepository schoolRepository;
    private final SessionRepository sessionRepository;
    private final StaffRepository staffRepository;
    private final StaffLeaveAllowanceRepository staffLeaveAllowanceRepository;

    @Override
    public UserRequest createFeeStaff(UserRequest feeStaffDto, Long schoolId)
    {
        log.info("Creating fee staff with email: {}", feeStaffDto.getEmail());
        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new ResourceNotFoundException("School not found with Id : " + schoolId));

        nonTeachingStaffRepository.findByEmailIgnoreCaseAndSchoolIdAndStatusActive(feeStaffDto.getEmail(), schoolId).ifPresent(existing ->
        {
            throw new AlreadyExistException("FeeStaff already exists with email: " + feeStaffDto.getEmail());
        });

        // Fetch the User entity
        User user = null;
        if (feeStaffDto.getUserId() != null) {
            user = userRepository.findById(feeStaffDto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with Id: " + feeStaffDto.getUserId()));
        }

        // Manually create NonTeachingStaff to avoid ModelMapper ambiguity
        NonTeachingStaff nonTeachingStaff = NonTeachingStaff.builder()
                .name(feeStaffDto.getName())
                .email(feeStaffDto.getEmail())
                .designation(feeStaffDto.getDesignation())
                .contactNumber(feeStaffDto.getContactNumber())
                .qualification(feeStaffDto.getQualification())
                .joiningDate(feeStaffDto.getJoiningDate())
                .status(UserStatus.ACTIVE)
                .user(user)
                .school(school)
                .build();

        NonTeachingStaff saved = nonTeachingStaffRepository.save(nonTeachingStaff);

        Staff staff = new Staff();
        staff.setStaffType(RoleEnum.ROLE_NON_TEACHING_STAFF);
        staff.setSchool(school);
        staff.setEmail(saved.getEmail());
        staffRepository.save(staff);

        log.info("FeeStaff created successfully with ID: {}", saved.getId());

        return convertToDto(saved);
    }

    @Override
    public UserRequest getFeeStaffById(Long id, Long schoolId)
    {
        log.info("Fetching fee staff with ID: {}", id);
        return convertToDto(fetchNonTeachingStaffById(id, schoolId));
    }

    @Override
    public NonTeachingStaff getNonTeachingStaffByEmailAndSchool(String email, Long schoolId)
    {
        return nonTeachingStaffRepository.findByEmailIgnoreCaseAndSchoolIdAndStatusActive(email, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Active NonTeachingStaff not found with Email: " + email));
    }

    @Override
    public List<UserRequest> getAllFeeStaff(Long schoolId)
    {
        log.info("Fetching all fee staff");
        return nonTeachingStaffRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserRequest> getActiveFeeStaff(Long schoolId)
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
    public UserRequest updateFeeStaff(Long id, UserRequest feeStaffDto, Long schoolId)
    {
        log.info("Updating fee staff with ID: {}", id);

        NonTeachingStaff existing = fetchNonTeachingStaffById(id, schoolId);
        modelMapper.map(feeStaffDto, existing); // Apply new values

        NonTeachingStaff updated = nonTeachingStaffRepository.save(existing);
        log.info("FeeStaff updated successfully with ID: {}", updated.getId());

        return convertToDto(updated);
    }

    @Transactional
    @Override
    public void inActiveNonTeachingStaff(Long id, Long schoolId)
    {
        log.info("Deactivating non-teaching staff with ID: {}", id);

        NonTeachingStaff staff = fetchNonTeachingStaffById(id, schoolId);

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

        NonTeachingStaff staff = nonTeachingStaffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NonTeachingStaff not found with ID: " + id));

        if (UserStatus.ACTIVE.equals(staff.getStatus()))
        {
            log.warn("NonTeachingStaff with ID {} is already active", id);
            return; // Return early if already active (idempotent)
        }

        staff.setStatus(UserStatus.ACTIVE);
        nonTeachingStaffRepository.save(staff);

        // If there's a user associated, restore the role
        if (staff.getUser() != null)
        {
            User user = staff.getUser();
            if (!user.getRoles().contains(RoleEnum.ROLE_NON_TEACHING_STAFF))
            {
                user.getRoles().add(RoleEnum.ROLE_NON_TEACHING_STAFF);
                userRepository.save(user);
            }
        }

        log.info("NonTeachingStaff marked as active with ID: {}", id);
    }

    private NonTeachingStaff fetchNonTeachingStaffById(Long id, Long schoolId)
    {
        return nonTeachingStaffRepository.findByNtsIdAndSchoolIdActiveStatus(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("NonTeachingStaff not found with ID: " + id));
    }

    private UserRequest convertToDto(NonTeachingStaff nts)
    {
        UserRequest dto = modelMapper.map(nts, UserRequest.class);
        dto.setId(nts.getId()); // Set the NonTeachingStaff entity ID
        dto.setUserId(nts.getUser() != null ? nts.getUser().getId() : null);
        dto.setCreatedAt(nts.getCreatedAt());
        dto.setUpdatedAt(nts.getUpdatedAt());
        dto.setDeletedAt(nts.getDeletedAt());

        Optional<Session> activeSessionOpt = sessionRepository.findBySchoolIdAndActiveTrue(nts.getSchool().getId());

        if (activeSessionOpt.isPresent())
        {
            Session activeSession = activeSessionOpt.get();

            Staff staff = staffRepository
                    .findByEmailAndSchoolIdIgnoreCase(
                            nts.getEmail(), nts.getSchool().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff record not found"));

            Optional<StaffLeaveAllowance> allowanceOpt =
                    staffLeaveAllowanceRepository.findByStaffAndSessionAndSchoolId(staff, activeSession, nts.getSchool().getId());

            allowanceOpt.ifPresent(allowance -> dto.setAllowedLeaves(allowance.getAllowedLeaves()));

        }

        return dto;
    }
}
