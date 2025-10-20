package com.java.slms.security;

import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.User;
import com.java.slms.repository.*;
import lombok.RequiredArgsConstructor;
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

        Long schoolId = getSchoolId(user);
        return new CustomUserDetails(user, schoolId);
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
