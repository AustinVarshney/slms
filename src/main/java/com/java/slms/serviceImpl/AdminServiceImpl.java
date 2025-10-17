package com.java.slms.serviceImpl;

import com.java.slms.dto.UserRequest;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.Admin;
import com.java.slms.model.School;
import com.java.slms.model.User;
import com.java.slms.repository.AdminRepository;
import com.java.slms.repository.SchoolRepository;
import com.java.slms.repository.UserRepository;
import com.java.slms.service.AdminService;
import com.java.slms.util.EntityFetcher;
import com.java.slms.util.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService
{

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final SchoolRepository schoolRepository;

    @Override
    public UserRequest createAdmin(UserRequest adminDto)
    {
        log.info("Creating admin with email: {}", adminDto.getEmail());

        if (adminRepository.findByEmailIgnoreCase(adminDto.getEmail()).isPresent())
        {
            throw new AlreadyExistException("Admin already exists with email: " + adminDto.getEmail());
        }

        School school = schoolRepository.findById(adminDto.getSchoolId()).orElseThrow(() -> new ResourceNotFoundException("School not found with Id : " + adminDto.getSchoolId()));

        User user = EntityFetcher.fetchUserByUserId(userRepository, adminDto.getUserId());

        Admin admin = new Admin();
        admin.setName(adminDto.getName());
        admin.setSchool(school);
        admin.setContactNumber(adminDto.getContactNumber());
        admin.setEmail(adminDto.getEmail());
        admin.setUser(user);
        admin.setDesignation("MANAGER");
        admin.setId(null);
        admin.setJoiningDate(LocalDate.now());
        admin.setStatus(UserStatus.ACTIVE);
        Admin savedAdmin = adminRepository.save(admin);

        log.info("Admin created with ID: {}", savedAdmin.getId());
        UserRequest userRequest = modelMapper.map(savedAdmin, UserRequest.class);
        userRequest.setSchoolName(savedAdmin.getSchool().getSchoolName());
        return userRequest;
    }

    public UserRequest getAdminDetails(String email, Long schoolId)
    {
        Admin admin = adminRepository
                .findByEmailIgnoreCaseAndSchoolIdAndStatusActive(email, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin with email '" + email + "' and schoolId '" + schoolId + "' not found or is inactive"));

        UserRequest userRequest = modelMapper.map(admin, UserRequest.class);
        userRequest.setSchoolName(admin.getSchool().getSchoolName());
        return userRequest;
    }

    public Admin getAdminInfo(String email, Long schoolId)
    {
        return adminRepository
                .findByEmailIgnoreCaseAndSchoolIdAndStatusActive(email, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin with email '" + email + "' and schoolId '" + schoolId + "' not found or is inactive"));

    }


}
