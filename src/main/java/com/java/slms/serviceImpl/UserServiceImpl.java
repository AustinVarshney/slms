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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
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

        if (user.getPanNumber() != null)
        {
            log.info("User ID {} is a Student. Details update skipped; use Student API to update student details.", userId);
            throw new WrongArgumentException("Student details can only be updated via the Student API.");
        }

        // Enable skipping null properties for partial update
        modelMapper.getConfiguration().setSkipNullEnabled(true);

        // Update basic user fields
        modelMapper.map(updateUserDetails, user);

        // Update roles if active, collect changes
        updateAdminDetailsIfActive(user, updateUserDetails, userId);
        updateTeacherDetailsIfActive(user, updateUserDetails, userId);
        updateNonTeachingStaffDetailsIfActive(user, updateUserDetails, userId);

        // Save user entity after role updates if necessary
        User savedUser = userRepository.save(user);

        UserRequest updatedUserRequest = modelMapper.map(savedUser, UserRequest.class);

        log.info("User updated successfully for ID: {}", userId);
        return updatedUserRequest;
    }

    private void updateAdminDetailsIfActive(User user, UpdateUserDetails updateUserDetails, Long userId)
    {
        Admin admin = user.getAdmin();
        if (admin != null)
        {
            if (admin.getStatus() == UserStatus.INACTIVE)
            {
                log.info("User ID {} Admin is inactive; skipping admin update", userId);
            }
            else
            {
                log.info("User ID {} is an Admin, updating details", userId);
                modelMapper.map(updateUserDetails, admin);
                adminRepository.save(admin);
            }
        }
    }

    private void updateTeacherDetailsIfActive(User user, UpdateUserDetails updateUserDetails, Long userId)
    {
        Teacher teacher = user.getTeacher();
        if (teacher != null)
        {
            if (teacher.getStatus() == UserStatus.INACTIVE)
            {
                log.info("User ID {} Teacher is inactive; skipping teacher update", userId);
            }
            else
            {
                log.info("User ID {} is a Teacher, updating details", userId);
                modelMapper.map(updateUserDetails, teacher);
                teacherRepository.save(teacher);
            }
        }
    }

    private void updateNonTeachingStaffDetailsIfActive(User user, UpdateUserDetails updateUserDetails, Long userId)
    {
        NonTeachingStaff nts = user.getNonTeachingStaff();
        if (nts != null)
        {
            if (nts.getStatus() == UserStatus.INACTIVE)
            {
                log.info("User ID {} NonTeachingStaff is inactive; skipping non-teaching staff update", userId);
            }
            else
            {
                log.info("User ID {} is NonTeachingStaff, updating details", userId);
                modelMapper.map(updateUserDetails, nts);
                nonTeachingStaffRepository.save(nts);
            }
        }
    }
    
    private void updateStudentDetailsIfActive(User user, UpdateUserDetails updateUserDetails, Long userId)
    {
        // Check if user has a student entity
        if (user.getPanNumber() != null)
        {
            com.java.slms.model.Student student = studentRepository.findByPanNumberIgnoreCase(user.getPanNumber()).orElse(null);
            if (student != null)
            {
                if (student.getStatus() == UserStatus.INACTIVE || student.getStatus() == UserStatus.GRADUATED)
                {
                    log.info("User ID {} Student is inactive/graduated; skipping student update", userId);
                    throw new WrongArgumentException("Cannot update: student account is inactive or graduated");
                }
            }
        }
    }
}
