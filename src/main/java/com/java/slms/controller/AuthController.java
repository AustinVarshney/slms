package com.java.slms.controller;

import com.java.slms.dto.*;
import com.java.slms.model.User;
import com.java.slms.payload.ApiResponse;
import com.java.slms.repository.UserRepository;
import com.java.slms.security.CustomUserDetailsService;
import com.java.slms.service.AdminService;
import com.java.slms.service.FeeStaffService;
import com.java.slms.service.StudentService;
import com.java.slms.service.TeacherService;
import com.java.slms.util.JwtUtil;
import com.java.slms.util.RoleEnum;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final AdminService adminService;
    private final TeacherService teacherService;
    private final StudentService studentService;
    private final FeeStaffService feeStaffService;
    private final ModelMapper modelMapper;

    @PostMapping("/register/staff")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> registerStaff(@RequestBody StaffRegisterRequest req)
    {
        // Check if user already exists
        if (userRepository.findByEmailIgnoreCase(req.getEmail()).isPresent())
        {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder()
                            .message("Email already registered")
                            .status(HttpStatus.BAD_REQUEST.value())
                            .build());
        }

        // Validate and convert roles
        Set<RoleEnum> roleEnums = new HashSet<>();
        List<String> invalidRoles = new ArrayList<>();
        for (String roleStr : req.getRoles())
        {
            try
            {
                roleEnums.add(RoleEnum.valueOf(roleStr));
            } catch (IllegalArgumentException e)
            {
                invalidRoles.add(roleStr);
            }
        }

        if (!invalidRoles.isEmpty())
        {
            return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                    .message("Invalid roles: " + String.join(", ", invalidRoles))
                    .status(HttpStatus.BAD_REQUEST.value())
                    .build());
        }

        // Create and save user
        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(roleEnums)
                .enabled(true)
                .build();
        user = userRepository.save(user);

        // Process each role
        for (RoleEnum role : roleEnums)
        {
            switch (role)
            {
                case ROLE_TEACHER ->
                {
                    TeacherDto teacherDto = modelMapper.map(req, TeacherDto.class);
                    teacherDto.setUserId(user.getId());
                    teacherService.createTeacher(teacherDto);
                }
                case ROLE_ADMIN ->
                {
                    UserRequest adminReq = modelMapper.map(req, UserRequest.class);
                    adminReq.setUserId(user.getId());
                    adminService.createAdmin(adminReq);
                }
                case ROLE_FEE_STAFF ->
                {
                    UserRequest feeStaffReq = modelMapper.map(req, UserRequest.class);
                    feeStaffReq.setUserId(user.getId());
                    feeStaffService.createFeeStaff(feeStaffReq);
                }
                default ->
                {
                    return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                            .message("Unsupported role: " + role.name())
                            .status(HttpStatus.BAD_REQUEST.value())
                            .build());
                }
            }
        }

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Staff registered successfully with roles: " +
                                roleEnums.stream().map(Enum::name).collect(Collectors.joining(", ")))
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @PostMapping("/register/student")
    @Transactional
    public ResponseEntity<ApiResponse<StudentDto>> registerStudent(@RequestBody StudentDto req)
    {
        if (userRepository.findByPanNumberIgnoreCase(req.getPanNumber()).isPresent())
        {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<StudentDto>builder()
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

        req.setUserId(user.getId());

        return ResponseEntity.ok(
                ApiResponse.<StudentDto>builder()
                        .data(studentService.createStudent(req))
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
