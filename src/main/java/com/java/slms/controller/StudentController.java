package com.java.slms.controller;

import com.java.slms.dto.StudentDto;
import com.java.slms.dto.StudentAttendance;
import com.java.slms.payload.ApiResponse;
import com.java.slms.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/students")
public class StudentController
{
    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<ApiResponse<StudentDto>> createStudent(@RequestBody StudentDto studentDto)
    {
        ApiResponse<StudentDto> response = ApiResponse.<StudentDto>builder()
                .data(studentService.createStudent(studentDto))
                .message("Student Created")
                .status(HttpStatus.CREATED.value())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

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

    @PatchMapping("/{panNumber}")
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

    @PatchMapping("/delete/{panNumber}")
    public ResponseEntity<ApiResponse<StudentDto>> updateStudentToDelete(@PathVariable String panNumber)
    {
        ApiResponse<StudentDto> response = ApiResponse.<StudentDto>builder()
                .data(studentService.deleteStudent(panNumber))
                .message("Student Deleted successfully")
                .status(HttpStatus.OK.value())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping("/present-today")
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

    @GetMapping("/present-today/{className}")
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

}
