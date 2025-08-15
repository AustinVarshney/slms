package com.java.slms.controller;

import com.java.slms.dto.StudentDto;
import com.java.slms.dto.StudentAttendance;
import com.java.slms.payload.ApiResponse;
import com.java.slms.repository.UserRepository;
import com.java.slms.service.StudentService;
import com.java.slms.util.UserStatuses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/students")
@PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
public class StudentController
{
    private final StudentService studentService;
    private final UserRepository userRepository;

//    @PostMapping
//    public ResponseEntity<ApiResponse<StudentDto>> createStudent(@RequestBody StudentDto studentDto)
//    {
//        ApiResponse<StudentDto> response = ApiResponse.<StudentDto>builder()
//                .data(studentService.createStudent(studentDto))
//                .message("Student Created")
//                .status(HttpStatus.CREATED.value())
//                .build();
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentDto>>> getAllStudents()
    {
        List<StudentDto> students = studentService.getAllStudent();
        ApiResponse<List<StudentDto>> response = ApiResponse.<List<StudentDto>>builder()
                .data(students)
                .message("Total Students - " + students.size())
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<List<StudentDto>>> getActiveStudents()
    {
        List<StudentDto> students = studentService.getActiveStudents();
        ApiResponse<List<StudentDto>> response = ApiResponse.<List<StudentDto>>builder()
                .data(students)
                .message("Total Students - " + students.size())
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


//    @GetMapping("/status")
//    public ResponseEntity<ApiResponse<List<StudentDto>>> getStudentByStatus(@RequestParam String status)
//    {
//        if (!isValidStatus(status))
//        {
//            throw new WrongArgumentException("Invalid status value: " + status);
//        }
//
//        StudentStatuses statusEnum = StudentStatuses.valueOf(status.toUpperCase());
//
//        List<StudentDto> students = studentService.getActiveStudents();
//
//        ApiResponse<List<StudentDto>> response = ApiResponse.<List<StudentDto>>builder()
//                .data(students)
//                .message("Total Students - " + students.size())
//                .status(HttpStatus.OK.value())
//                .build();
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }

    @GetMapping("/{panNumber}")
    public ResponseEntity<ApiResponse<StudentDto>> getStudentByPAN(@PathVariable String panNumber)
    {
        ApiResponse<StudentDto> response = ApiResponse.<StudentDto>builder()
                .data(studentService.getStudentByPAN(panNumber))
                .message("Student Fetched")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<ApiResponse<StudentDto>> getCurrentStudent()
    {
        String panNumber = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        StudentDto student = studentService.getStudentByPAN(panNumber);

        ApiResponse<StudentDto> response = ApiResponse.<StudentDto>builder()
                .data(student)
                .message("Current student fetched successfully")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{panNumber}")
    public ResponseEntity<ApiResponse<StudentDto>> updateStudent(
            @PathVariable String panNumber,
            @RequestBody StudentDto studentDto
    )
    {
        ApiResponse<StudentDto> response = ApiResponse.<StudentDto>builder()
                .data(studentService.updateStudent(panNumber, studentDto))
                .message("Student updated successfully")
                .status(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/present-today")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<List<StudentAttendance>>> getPresentToday()
    {
        List<StudentAttendance> list = studentService.getStudentsPresentToday();
        return ResponseEntity.ok(
                ApiResponse.<List<StudentAttendance>>builder()
                        .data(list)
                        .message("Students present today: " + list.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/present-today/{classId}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<List<StudentAttendance>>> getPresentTodayByClass(@PathVariable Long classId)
    {
        List<StudentAttendance> list = studentService.getStudentsPresentTodayByClass(classId);
        return ResponseEntity.ok(
                ApiResponse.<List<StudentAttendance>>builder()
                        .data(list)
                        .message("Students present today in class " + classId + ": " + list.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<List<StudentDto>>> getStudentByClassId(@PathVariable Long classId)
    {
        List<StudentDto> list = studentService.getStudentsByClassId(classId);
        return ResponseEntity.ok(
                ApiResponse.<List<StudentDto>>builder()
                        .data(list)
                        .message("Students in class " + classId + ": " + list.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @DeleteMapping("/{pan}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStudentByPan(@PathVariable String pan)
    {
        studentService.deleteStudentByPan(pan);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Student and user deleted successfully for PAN: " + pan)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    private boolean isValidStatus(String status)
    {
        return Arrays.stream(UserStatuses.values())
                .anyMatch(s -> s.name().equalsIgnoreCase(status));
    }

}
