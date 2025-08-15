package com.java.slms.serviceImpl;

import com.java.slms.dto.UserRequest;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.Admin;
import com.java.slms.model.User;
import com.java.slms.repository.AdminRepository;
import com.java.slms.repository.UserRepository;
import com.java.slms.service.AdminService;
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
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public UserRequest createAdmin(UserRequest adminDto) {
        log.info("Creating admin with email: {}", adminDto.getEmail());

        if (adminRepository.findByEmailIgnoreCase(adminDto.getEmail()).isPresent()) {
            throw new AlreadyExistException("Admin already exists with email: " + adminDto.getEmail());
        }

        Admin admin = modelMapper.map(adminDto, Admin.class);
        admin.setId(null);
        Admin savedAdmin = adminRepository.save(admin);

        log.info("Admin created with ID: {}", savedAdmin.getId());
        return convertToDto(savedAdmin);
    }

    @Override
    public UserRequest getAdminById(Long id) {
        log.info("Fetching admin with ID: {}", id);
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + id));

        return convertToDto(admin);
    }

    @Override
    public List<UserRequest> getAllAdmins() {
        log.info("Fetching all admins");
        return adminRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserRequest> getActiveAdmins() {
        log.info("Fetching active admins");

        List<User> activeUsers = userRepository.findByEmailIsNotNullAndEnabledTrue();
        List<String> activeEmails = activeUsers.stream()
                .map(User::getEmail)
                .filter(Objects::nonNull)
                .toList();

        List<Admin> activeAdmins = adminRepository.findByEmailIn(activeEmails);
        return activeAdmins.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public UserRequest updateAdmin(Long id, UserRequest adminDto) {
        log.info("Updating admin with ID: {}", id);
        Admin existingAdmin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + id));

        modelMapper.map(adminDto, existingAdmin);
        Admin updatedAdmin = adminRepository.save(existingAdmin);

        log.info("Admin updated successfully: {}", id);
        return convertToDto(updatedAdmin);
    }

    @Override
    @Transactional
    public void deleteAdmin(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + id));

        adminRepository.delete(admin);
        log.info("Deleted admin record with ID: {}", id);
    }

    private UserRequest convertToDto(Admin admin) {
        UserRequest dto = modelMapper.map(admin, UserRequest.class);

        // Set user status
        userRepository.findByEmailIgnoreCase(admin.getEmail()).ifPresent(user ->
                dto.setStatus(user.isEnabled() ? UserStatuses.ACTIVE : UserStatuses.INACTIVE));

        // Set timestamps
        dto.setCreatedAt(admin.getCreatedAt());
        dto.setUpdatedAt(admin.getUpdatedAt());
        dto.setDeletedAt(admin.getDeletedAt());

        return dto;
    }
}
