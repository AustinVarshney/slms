package com.java.slms.serviceImpl;

import com.java.slms.dto.UserRegistrationRequest;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.FeeStaff;
import com.java.slms.model.User;
import com.java.slms.repository.FeeStaffRepository;
import com.java.slms.repository.UserRepository;
import com.java.slms.service.FeeStaffService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class FeeStaffServiceImpl implements FeeStaffService
{
    private final FeeStaffRepository feeStaffRepo;
    private final UserRepository userRepo;
    private final ModelMapper modelMapper;


    @Override
    public UserRegistrationRequest createFeeStaff(UserRegistrationRequest req)
    {
        User user = userRepo.findById(req.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        FeeStaff fs = modelMapper.map(req, FeeStaff.class);
        fs.setId(null);
        fs.setUser(user);
        feeStaffRepo.save(fs);
        return modelMapper.map(fs, UserRegistrationRequest.class);
    }

    @Override
    public UserRegistrationRequest getFeeStaff(Long id)
    {
        FeeStaff feeStaff = feeStaffRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("FeeStaff not found with id: " + id));

        return modelMapper.map(feeStaff, UserRegistrationRequest.class);
    }

    @Override
    public List<UserRegistrationRequest> getAllFeeStaff()
    {
        List<FeeStaff> feeStaffList = feeStaffRepo.findAll();
        return feeStaffList.stream()
                .map(feeStaff -> modelMapper.map(feeStaff, UserRegistrationRequest.class))
                .toList();
    }

}
