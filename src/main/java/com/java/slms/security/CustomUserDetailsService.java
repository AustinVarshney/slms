package com.java.slms.security;

import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.Student;
import com.java.slms.model.Teacher;
import com.java.slms.model.Admin;
import com.java.slms.model.NonTeachingStaff;
import com.java.slms.model.User;
import com.java.slms.repository.*;
import com.java.slms.util.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService
{

    private final UserRepository userRepository;
    private final NonTeachingStaffRepository nonTeachingStaffRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        User user = userRepository.findUserWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + username));

        // Check if user's role-specific status is inactive
        checkUserRoleStatus(user);

        Long schoolId = getSchoolId(user);
        return new CustomUserDetails(user, schoolId);
    }
    
    private void checkUserRoleStatus(User user)
    {
        // Check student status
        if (user.getStudent() != null)
        {
            Student student = user.getStudent();
            if (student.getStatus() == UserStatus.INACTIVE || student.getStatus() == UserStatus.GRADUATED)
            {
                throw new DisabledException("Student account is " + student.getStatus().name().toLowerCase() + " and cannot login");
            }
        }
        
        // Check teacher status
        if (user.getTeacher() != null)
        {
            Teacher teacher = user.getTeacher();
            if (teacher.getStatus() == UserStatus.INACTIVE)
            {
                throw new DisabledException("Teacher account is inactive and cannot login");
            }
        }
        
        // Check admin status
        if (user.getAdmin() != null)
        {
            Admin admin = user.getAdmin();
            if (admin.getStatus() == UserStatus.INACTIVE)
            {
                throw new DisabledException("Admin account is inactive and cannot login");
            }
        }
        
        // Check non-teaching staff status
        if (user.getNonTeachingStaff() != null)
        {
            NonTeachingStaff nts = user.getNonTeachingStaff();
            if (nts.getStatus() == UserStatus.INACTIVE)
            {
                throw new DisabledException("Non-teaching staff account is inactive and cannot login");
            }
        }
    }

    private Long getSchoolId(User user)
    {
        // Now you can directly get the schoolId based on roles
        if (user.getStudent() != null)
        {
            return getSchoolIdForStudent(user);
        }
        else if (user.getAdmin() != null)
        {
            return getSchoolIdForAdmin(user);
        }
        else if (user.getTeacher() != null)
        {
            return getSchoolIdForTeachingStaff(user);
        }
        else if (user.getNonTeachingStaff() != null)
        {
            return getSchoolIdForNonTeachingStaff(user);
        }
        throw new ResourceNotFoundException("User does not have a valid role to fetch schoolId.");
    }

    private Long getSchoolIdForStudent(User user)
    {
        return user.getStudent().getSchool().getId();
    }

    private Long getSchoolIdForAdmin(User user)
    {
        return user.getAdmin().getSchool().getId();
    }

    private Long getSchoolIdForNonTeachingStaff(User user)
    {
        return user.getNonTeachingStaff().getSchool().getId();
    }

    private Long getSchoolIdForTeachingStaff(User user)
    {
        return user.getTeacher().getSchool().getId();
    }

}
