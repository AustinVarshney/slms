package com.java.slms.security;

import com.java.slms.repository.UserRepository;
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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        // try email first (case-insensitive)
            return userRepository.findByEmailIgnoreCase(username)
                .map(CustomUserDetails::new)
                .or(() -> userRepository.findByPanNumberIgnoreCase(username).map(CustomUserDetails::new))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + username));
    }

}
