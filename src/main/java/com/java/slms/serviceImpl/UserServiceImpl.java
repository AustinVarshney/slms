package com.java.slms.serviceImpl;

import com.java.slms.dto.UserRequest;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.model.*;
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
    public void changePassword(Long userId, String password)
    {
        log.info("Changing password for user with ID: {}", userId);
        User user = EntityFetcher.fetchUserByUserId(userRepository, userId);
        user.setPassword(passwordEncoder.encode(password));
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
    public UserRequest updateUserDetails(Long userId, UserRequest userRequest)
    {
        log.info("Updating user details for ID: {}", userId);

        // Fetch the user by ID
        User user = EntityFetcher.fetchUserByUserId(userRepository, userId);

        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(userRequest, user);

        UserRequest updatedUser = null;

        if (user.getPanNumber() != null)
        {
            log.info("User ID {} is a Student", userId);

            Student student = EntityFetcher.fetchStudentByPan(studentRepository, user.getPanNumber());
            modelMapper.map(userRequest, student);
            studentRepository.save(student);

        }
        else
        {

            if (user.getAdmin() != null)
            {
                log.info("User ID {} is an Admin", userId);
                Admin admin = user.getAdmin();
                modelMapper.map(userRequest, admin);
                updatedUser = modelMapper.map(adminRepository.save(admin), UserRequest.class);
            }

            if (user.getTeacher() != null)
            {
                log.info("User ID {} is a Teacher", userId);
                Teacher teacher = user.getTeacher();
                modelMapper.map(userRequest, teacher);
                updatedUser = modelMapper.map(teacherRepository.save(teacher), UserRequest.class);

            }

            if (user.getNonTeachingStaff() != null)
            {
                log.info("User ID {} is a Fee Staff", userId);
                NonTeachingStaff nonTeachingStaff = user.getNonTeachingStaff();
                modelMapper.map(userRequest, nonTeachingStaff);
                updatedUser = modelMapper.map(nonTeachingStaffRepository.save(nonTeachingStaff), UserRequest.class);

            }
        }

        // Save updated user
        log.info("User updated successfully for ID: {}", userId);
        updatedUser.setStatus(UserStatus.ACTIVE);
        return updatedUser;
    }

    @Override
    public void inActiveUser(Long userId)
    {
        log.info("Disabling user with ID: {}", userId);
        User user = EntityFetcher.fetchUserByUserId(userRepository, userId);
        if (!user.isEnabled())
        {
            log.warn("User with ID: {} is already disabled", userId);
            throw new AlreadyExistException("User is already disabled with UserId: " + userId);
        }
        user.setEnabled(false);
        userRepository.save(user);
        log.info("User disabled successfully for ID: {}", userId);
    }
}
