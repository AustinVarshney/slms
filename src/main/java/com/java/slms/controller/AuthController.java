package com.java.slms.controller;

import com.java.slms.dto.*;
import com.java.slms.model.User;
import com.java.slms.payload.ApiResponse;
import com.java.slms.repository.UserRepository;
import com.java.slms.security.CustomUserDetailsService;
import com.java.slms.util.JwtUtil;
import com.java.slms.util.RoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController
{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register/staff")
    public ResponseEntity<ApiResponse<Void>> registerStaff(@RequestBody StaffRegisterRequest req)
    {
        if (userRepository.findByEmailIgnoreCase(req.getEmail()).isPresent())
        {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder()
                            .message("Email already registered")
                            .status(HttpStatus.BAD_REQUEST.value())
                            .build());
        }
        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(Set.of(RoleEnum.valueOf(req.getRole())))
                .enabled(true)
                .build();
        userRepository.save(user);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Staff registered successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @PostMapping("/register/student")
    public ResponseEntity<ApiResponse<Void>> registerStudent(@RequestBody StudentRegisterRequest req)
    {
        if (userRepository.findByPanNumberIgnoreCase(req.getPanNumber()).isPresent())
        {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder()
                            .message("PAN already registered")
                            .status(HttpStatus.BAD_REQUEST.value())
                            .build());
        }
        User user = User.builder()
                .panNumber(req.getPanNumber())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(Set.of(RoleEnum.ROLE_STUDENT))
                .enabled(true)
                .build();
        userRepository.save(user);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Student registered successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody AuthRequest req)
    {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        UserDetails ud = userDetailsService.loadUserByUsername(req.getEmail());
        String token = jwtUtil.generateToken(ud);
        AuthResponse resp = new AuthResponse(token, "Bearer", 3600);
        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .data(resp)
                        .message("Login successful")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @PostMapping("/student/login")
    public ResponseEntity<ApiResponse<AuthResponse>> studentLogin(@RequestBody StudentAuthRequest req)
    {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getPanNumber(), req.getPassword()));
        UserDetails ud = userDetailsService.loadUserByUsername(req.getPanNumber());
        String token = jwtUtil.generateToken(ud);
        AuthResponse resp = new AuthResponse(token, "Bearer", 3600);
        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .data(resp)
                        .message("Login successful")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
