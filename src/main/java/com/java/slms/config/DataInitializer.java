package com.java.slms.config;

import com.java.slms.model.User;
import com.java.slms.repository.UserRepository;
import com.java.slms.util.RoleEnum;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements ApplicationRunner
{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args)
    {
        boolean adminExists = userRepository.existsByRolesContaining(RoleEnum.ROLE_SUPER_ADMIN);
        if (!adminExists)
        {
            User admin = new User();
            admin.setEmail("admin@company.com");
            admin.setPassword(passwordEncoder.encode("temporaryStrongPassword!123"));
            admin.setRoles(Set.of(RoleEnum.ROLE_SUPER_ADMIN));
            admin.setEnabled(true);
            userRepository.save(admin);
        }
    }
}