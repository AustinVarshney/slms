package com.java.slms.config;

import com.java.slms.repository.AdminRepository;
import com.java.slms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner
{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;

    private static final String ADMIN_EMAIL = "admin.email";
    private static final String ADMIN_PASSWORD = "admin.password";
    private static final String ADMIN_CONTACT = "admin.contactNumber";
    private static final String ADMIN_NAME = "admin.name";
    private static final String ADMIN_SCHOOL = "admin.schoolName";

    @Override
    @Transactional
    public void run(ApplicationArguments args)
    {
//        boolean adminExists = userRepository.existsByRolesContaining(RoleEnum.ROLE_ADMIN);
//        if (!adminExists)
//        {
//            User adminUser = new User();
//            String adminEmail = ConfigUtil.getRequired(ADMIN_EMAIL);
//            String adminPassword = ConfigUtil.getRequired(ADMIN_PASSWORD);
//            String adminContactNumber = ConfigUtil.getRequired(ADMIN_CONTACT);
//            String adminName = ConfigUtil.getRequired(ADMIN_NAME);
//            String schoolName = ConfigUtil.getRequired(ADMIN_SCHOOL);
//
//            School school = new School();
//            school.setName(schoolName);
//
//            adminUser.setEmail(adminEmail);
//            adminUser.setPassword(passwordEncoder.encode(adminPassword));
//            adminUser.setRoles(Set.of(RoleEnum.ROLE_ADMIN));
//            adminUser.setEnabled(true);
//            User savedUser = userRepository.save(adminUser);
//
//            Admin admin = new Admin();
//            admin.setStatus(UserStatus.ACTIVE);
//            admin.setContactNumber(adminContactNumber);
//            admin.setName(adminName);
//            admin.setEmail(adminEmail);
//            admin.setDesignation("MANAGER");
//            admin.setUser(savedUser);
//            adminRepository.save(admin);

//        }
    }
}