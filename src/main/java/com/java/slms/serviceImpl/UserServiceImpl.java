package com.java.slms.serviceImpl;

import com.java.slms.dto.PasswordDto;
import com.java.slms.dto.UpdateUserDetails;
import com.java.slms.dto.UserRequest;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.Admin;
import com.java.slms.model.NonTeachingStaff;
import com.java.slms.model.Teacher;
import com.java.slms.model.User;
import com.java.slms.repository.*;
import com.java.slms.service.UserService;
import com.java.slms.util.EntityFetcher;
import com.java.slms.util.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class UserServiceImpl implements UserService
{
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final NonTeachingStaffRepository nonTeachingStaffRepository;
    private final TeacherRepository teacherRepository;
    private final AdminRepository adminRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void changePassword(Long userId, PasswordDto password)
    {
        log.info("Changing password for user with ID: {}", userId);
        User user = EntityFetcher.fetchUserByUserId(userRepository, userId);

        if (!user.isEnabled())
        {
            throw new WrongArgumentException("Cannot change password: user account is disabled");
        }

        user.setPassword(passwordEncoder.encode(password.getPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for user ID: {}", userId);
    }

    @Override
    public void deleteUser(Long userId)
    {
        log.info("Deleting user with ID: {}", userId);
        User user = EntityFetcher.fetchUserByUserId(userRepository, userId);
        userRepository.delete(user);
        log.info("User deleted successfully with ID: {}", userId);
    }

    @Override
    public UserRequest updateUserDetails(Long userId, UpdateUserDetails updateUserDetails)
    {
        log.info("Updating user details for ID: {}", userId);

        User user = EntityFetcher.fetchUserByUserId(userRepository, userId);

        if (!user.isEnabled())
        {
            throw new WrongArgumentException("Cannot change details: user account is disabled");
        }

        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(updateUserDetails, user);

        UserRequest updatedUser = null;

        if (user.getPanNumber() != null)
        {
            log.info("User ID {} is a Student. Details update skipped; use Student API to update student details.", userId);
            throw new WrongArgumentException("Student details can only be updated via the Student API.");
        }
        else
        {
            updatedUser = modelMapper.map(user, UserRequest.class);

            if (user.getAdmin() != null)
            {
                Admin admin = user.getAdmin();
                if (admin.getStatus() == UserStatus.INACTIVE)
                {
                    log.info("User ID {} Admin is inactive; skipping admin update", userId);
                }
                else
                {
                    log.info("User ID {} is an Admin", userId);
                    modelMapper.map(updateUserDetails, admin);
                    updatedUser = modelMapper.map(adminRepository.save(admin), UserRequest.class);
                }
            }

            if (user.getTeacher() != null)
            {
                Teacher teacher = user.getTeacher();
                if (teacher.getStatus() == UserStatus.INACTIVE)
                {
                    log.info("User ID {} Teacher is inactive; skipping teacher update", userId);
                }
                else
                {
                    log.info("User ID {} is a Teacher", userId);
                    modelMapper.map(updateUserDetails, teacher);
                    updatedUser = modelMapper.map(teacherRepository.save(teacher), UserRequest.class);
                }
            }

            if (user.getNonTeachingStaff() != null)
            {
                NonTeachingStaff nts = user.getNonTeachingStaff();
                if (nts.getStatus() == UserStatus.INACTIVE)
                {
                    log.info("User ID {} NonTeachingStaff is inactive; skipping fee staff update", userId);
                }
                else
                {
                    log.info("User ID {} is a Fee Staff", userId);
                    modelMapper.map(updateUserDetails, nts);
                    updatedUser = modelMapper.map(nonTeachingStaffRepository.save(nts), UserRequest.class);
                }
            }
        }

        log.info("User updated successfully for ID: {}", userId);
        return updatedUser;
    }

}
