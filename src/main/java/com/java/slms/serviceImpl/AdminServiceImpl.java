package com.java.slms.serviceImpl;

import com.java.slms.dto.UserRegistrationRequest;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.Admin;
import com.java.slms.model.User;
import com.java.slms.repository.AdminRepository;
import com.java.slms.repository.UserRepository;
import com.java.slms.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService
{

    private final AdminRepository adminRepo;
    private final UserRepository userRepo;
    private final ModelMapper modelMapper;

    @Override
    public UserRegistrationRequest createAdmin(UserRegistrationRequest req)
    {
        User user = userRepo.findById(req.getUserId()).orElseThrow(() -> new ResourceNotFoundException("Admin user not found with UserId:" + req.getUserId()));
        Admin admin = modelMapper.map(req, Admin.class);
        admin.setUser(user);
        adminRepo.save(admin);
        return req;
    }

    @Override
    public UserRegistrationRequest getAdmin(Long id)
    {
        Admin admin = adminRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Admin user not found with UserId:" + id));
        return modelMapper.map(admin, UserRegistrationRequest.class);
    }

    @Override
    public List<UserRegistrationRequest> getAllAdmins()
    {
        return adminRepo.findAll().stream().map(a -> modelMapper.map(a, UserRegistrationRequest.class)).toList();
    }
}

