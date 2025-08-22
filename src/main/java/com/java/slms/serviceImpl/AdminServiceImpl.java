package com.java.slms.serviceImpl;

import com.java.slms.dto.UserRequest;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.Admin;
import com.java.slms.model.User;
import com.java.slms.repository.AdminRepository;
import com.java.slms.repository.UserRepository;
import com.java.slms.service.AdminService;
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
public class AdminServiceImpl implements AdminService
{

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public UserRequest createAdmin(UserRequest adminDto)
    {
        log.info("Creating admin with email: {}", adminDto.getEmail());

        if (adminRepository.findByEmailIgnoreCase(adminDto.getEmail()).isPresent())
        {
            throw new AlreadyExistException("Admin already exists with email: " + adminDto.getEmail());
        }

        Admin admin = modelMapper.map(adminDto, Admin.class);
        admin.setId(null);
        admin.setStatus(UserStatus.ACTIVE);
        Admin savedAdmin = adminRepository.save(admin);

        log.info("Admin created with ID: {}", savedAdmin.getId());
        return modelMapper.map(savedAdmin, UserRequest.class);
    }

    @Override
    public UserRequest getAdminById(Long id)
    {
        log.info("Fetching admin with ID: {}", id);
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + id));

        return modelMapper.map(admin, UserRequest.class);
    }

    @Override
    public List<UserRequest> getAllAdmins()
    {
        log.info("Fetching all admins");
        return adminRepository.findAll().stream()
                .map(admin -> modelMapper.map(admin, UserRequest.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserRequest> getActiveAdmins()
    {
        log.info("Fetching active admins");
        List<Admin> activeAdmins = adminRepository.findByStatus(UserStatus.ACTIVE);
        return activeAdmins.stream()
                .map(admin -> modelMapper.map(admin, UserRequest.class))
                .toList();
    }

    @Override
    public UserRequest updateAdmin(Long id, UserRequest adminDto)
    {
        return null;
    }

    @Override
    @Transactional
    public void inActiveAdmin(Long id)
    {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + id));

        if (admin.getStatus().equals(UserStatus.INACTIVE))
            throw new AlreadyExistException("Admin Already inactive");
        User user = admin.getUser();
        admin.setStatus(UserStatus.INACTIVE);
        adminRepository.save(admin);
        EntityFetcher.removeRoleFromUser(user.getId(), RoleEnum.ROLE_ADMIN, userRepository);
    }
}
