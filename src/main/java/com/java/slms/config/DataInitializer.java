package com.java.slms.config;

import com.java.slms.model.User;
import com.java.slms.repository.UserRepository;
import com.java.slms.util.ConfigUtil;
import com.java.slms.util.RoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner
{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "admin.email";
    private static final String ADMIN_PASSWORD = "admin.password";

    @Override
    public void run(ApplicationArguments args)
    {
        boolean adminExists = userRepository.existsByRolesContaining(RoleEnum.ROLE_ADMIN);
        if (!adminExists)
        {
            User admin = new User();
            String adminEmail = ConfigUtil.getRequired(ADMIN_EMAIL);
            String adminPassword = ConfigUtil.getRequired(ADMIN_PASSWORD);

            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRoles(Set.of(RoleEnum.ROLE_ADMIN));
            admin.setEnabled(true);
            userRepository.save(admin);
        }
    }
}