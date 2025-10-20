package com.java.slms.controller;

import com.java.slms.dto.*;
import com.java.slms.model.User;
import com.java.slms.payload.RestResponse;
import com.java.slms.repository.UserRepository;
import com.java.slms.security.CustomUserDetailsService;
import com.java.slms.service.*;
import com.java.slms.util.JwtUtil;
import com.java.slms.util.RoleEnum;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "Handles user authentication and registration")
public class AuthController
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    private final AdminService adminService;
    private final SchoolService schoolService;
    private final TeacherService teacherService;
    private final StudentService studentService;
    private final NonTeachingStaffService nonTeachingStaffService;
    private final ModelMapper modelMapper;
    private final ExcelStudentParseService excelStudentParseService;

    @PostMapping("/register/admin")
    @Transactional
    public ResponseEntity<RestResponse<UserRequest>> registerAdmin(@RequestBody AdminRegisterRequestDto req)
    {
        // Check if email is already registered
        if (userRepository.findByPanNumberIgnoreCase(req.getEmail()).isPresent())
        {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(RestResponse.<UserRequest>builder()
                            .message("Email already registered")
                            .status(HttpStatus.BAD_REQUEST.value())
                            .build());
        }

        // Create User object with the provided data
        User user = User.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))  // Encoded password
                .roles(Set.of(RoleEnum.ROLE_ADMIN))  // Assign role as admin
                .enabled(true)  // Enable the user
                .build();

        // Save the user to generate an ID
        userRepository.save(user);

        // Map AdminRegisterRequestDto to UserRequest manually to avoid conflicts
        UserRequest userRequest = new UserRequest();
        userRequest.setEmail(req.getEmail());
        userRequest.setContactNumber(req.getContactNumber());
        userRequest.setDesignation(req.getDesignation());
        userRequest.setQualification(req.getQualification());
        userRequest.setName(req.getName());

        // Set the userId to link the user to the admin
        userRequest.setUserId(user.getId());

        // Map the school data and create a school
        SchoolRequestDto schoolRequestDto = new SchoolRequestDto();
        schoolRequestDto.setSchoolName(req.getSchoolName());
        schoolRequestDto.setSchoolEmail(req.getSchoolEmail());
        schoolRequestDto.setSchoolWebsite(req.getSchoolWebsite());
        schoolRequestDto.setSchoolContactNumber(req.getSchoolContactNumber());
        schoolRequestDto.setSchoolAddress(req.getSchoolAddress());  // Assuming there is a schoolAddress in the request

        // Create school using school service
        SchoolResponseDto schoolResponseDto = schoolService.createSchool(schoolRequestDto);

        // Set the schoolId in UserRequest
        userRequest.setSchoolId(schoolResponseDto.getId());

        // Now, create the admin using adminService
        return ResponseEntity.ok(
                RestResponse.<UserRequest>builder()
                        .data(adminService.createAdmin(userRequest))  // Save the admin and return response
                        .message("Admin With School Details registered successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @PostMapping("/register/staff")
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<Void>> registerStaff(@RequestBody StaffRegisterRequest req,
                                                            @RequestAttribute("schoolId") Long schoolId
    )
    {
        // Check if user already exists
        if (userRepository.findByEmailIgnoreCase(req.getEmail()).isPresent())
        {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(RestResponse.<Void>builder()
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
            return ResponseEntity.badRequest().body(RestResponse.<Void>builder()
                    .message("Invalid roles: " + String.join(", ", invalidRoles))
                    .status(HttpStatus.BAD_REQUEST.value())
                    .build());
        }

        if (roleEnums.contains(RoleEnum.ROLE_TEACHER) && roleEnums.contains(RoleEnum.ROLE_NON_TEACHING_STAFF))
        {
            return ResponseEntity.badRequest().body(RestResponse.<Void>builder()
                    .message("A person cannot have both ROLE_TEACHER and NON_TEACHING_STAFF")
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
                    teacherService.createTeacher(teacherDto, schoolId);
                }
//                case ROLE_ADMIN ->
//                {
//                    UserRequest adminReq = modelMapper.map(req, UserRequest.class);
//                    adminReq.setUserId(user.getId());
//                    adminService.createAdmin(adminReq);
//                }
                case ROLE_NON_TEACHING_STAFF ->
                {
                    UserRequest feeStaffReq = modelMapper.map(req, UserRequest.class);
                    feeStaffReq.setUserId(user.getId());
                    nonTeachingStaffService.createFeeStaff(feeStaffReq, schoolId);
                }
                default ->
                {
                    return ResponseEntity.badRequest().body(RestResponse.<Void>builder()
                            .message("Unsupported role: " + role.name())
                            .status(HttpStatus.BAD_REQUEST.value())
                            .build());
                }
            }
        }

        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .message("Staff registered successfully with roles: " +
                                roleEnums.stream().map(Enum::name).collect(Collectors.joining(", ")))
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/register/student")
    @Transactional
    public ResponseEntity<RestResponse<StudentResponseDto>> registerStudent(@RequestBody StudentRequestDto req , @RequestAttribute("schoolId") Long schoolId
    )
    {
        // Check if user with this PAN already exists
        var existingUserOpt = userRepository.findByPanNumberIgnoreCase(req.getPanNumber());
        
        if (existingUserOpt.isPresent())
        {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(RestResponse.<StudentResponseDto>builder()
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
                RestResponse.<StudentResponseDto>builder()
                        .data(studentService.createStudent(req, schoolId))
                        .message("Student registered successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }


    @PostMapping("/upload-students")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public ResponseEntity<?> uploadStudents(@RequestParam("file") MultipartFile file , @RequestAttribute("schoolId") Long schoolId
    )
    {
        try
        {
            String filename = file.getOriginalFilename();
            if (filename == null || !(filename.endsWith(".xlsx") || filename.endsWith(".xls") || filename.endsWith(".csv")))
            {
                return ResponseEntity.badRequest().body("Please upload an Excel or CSV file");
            }

            List<StudentRequestDto> students = excelStudentParseService.uploadStudents(file, schoolId);
            List<StudentResponseDto> responses = new ArrayList<>();

            for (StudentRequestDto studentDto : students)
            {
                if (userRepository.findByPanNumberIgnoreCase(studentDto.getPanNumber()).isPresent())
                {
                    continue; // Or log + add to error list
                }

                User user = User.builder()
                        .panNumber(studentDto.getPanNumber())
                        .password(passwordEncoder.encode(studentDto.getPassword() == null ? "default123" : studentDto.getPassword()))
                        .roles(Set.of(RoleEnum.ROLE_STUDENT))
                        .enabled(true)
                        .build();
                userRepository.save(user);

                studentDto.setUserId(user.getId());

                StudentResponseDto response = studentService.createStudent(studentDto, schoolId);
                responses.add(response);
            }

            return ResponseEntity.ok(RestResponse.<List<StudentResponseDto>>builder()
                    .data(responses)
                    .message("Students uploaded successfully")
                    .status(HttpStatus.OK.value())
                    .build());

        } catch (Exception e)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to upload file: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<RestResponse<AuthResponse>> staffLogin(@RequestBody AuthRequest req)
    {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        UserDetails ud = userDetailsService.loadUserByUsername(req.getEmail());
        String token = jwtUtil.generateToken(ud);
        AuthResponse resp = new AuthResponse(token, "Bearer", 3600);
        return ResponseEntity.ok(
                RestResponse.<AuthResponse>builder()
                        .data(resp)
                        .message("Login successful")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @PostMapping("/student/login")
    public ResponseEntity<RestResponse<AuthResponse>> studentLogin(@RequestBody StudentAuthRequest req)
    {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getPanNumber(), req.getPassword()));
        UserDetails ud = userDetailsService.loadUserByUsername(req.getPanNumber());
        String token = jwtUtil.generateToken(ud);
        AuthResponse resp = new AuthResponse(token, "Bearer", 3600);
        return ResponseEntity.ok(
                RestResponse.<AuthResponse>builder()
                        .data(resp)
                        .message("Login successful")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
