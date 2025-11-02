package com.java.slms.controller;

import com.java.slms.dto.CurrentDayAttendance;
import com.java.slms.dto.PreviousSchoolingRecordDto;
import com.java.slms.dto.StudentResponseDto;
import com.java.slms.dto.UpdateStudentInfo;
import com.java.slms.dto.UpdateStudentStatusRequest;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/students")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name = "Student Controller", description = "APIs for managing students")
public class StudentController
{
    private final StudentService studentService;

    @Operation(
            summary = "Get all students",
            description = "Retrieves all students in the system.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Students retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @GetMapping
    public ResponseEntity<RestResponse<List<StudentResponseDto>>> getAllStudents(@RequestAttribute("schoolId") Long schoolId)
    {
        List<StudentResponseDto> students = studentService.getAllStudent(schoolId);
        RestResponse<List<StudentResponseDto>> response = RestResponse.<List<StudentResponseDto>>builder()
                .data(students)
                .message("Total Students - " + students.size())
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "Get active students",
            description = "Retrieves all active students.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Active students retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_NON_TEACHING_STAFF', 'ROLE_STUDENT')")
    public ResponseEntity<RestResponse<List<StudentResponseDto>>> getActiveStudents(@RequestAttribute("schoolId") Long schoolId)
    {
        List<StudentResponseDto> students = studentService.getActiveStudents(schoolId);
        RestResponse<List<StudentResponseDto>> response = RestResponse.<List<StudentResponseDto>>builder()
                .data(students)
                .message("Total Students - " + students.size())
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "Get student by PAN number",
            description = "Fetches a student by their PAN number.",
            parameters = {
                    @Parameter(name = "panNumber", description = "PAN number of the student", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Student fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid PAN or student not found", content = @Content)
            }
    )
    @GetMapping("/{panNumber}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<RestResponse<StudentResponseDto>> getStudentByPAN(@PathVariable String panNumber, @RequestAttribute("schoolId") Long schoolId)
    {
        RestResponse<StudentResponseDto> response = RestResponse.<StudentResponseDto>builder()
                .data(studentService.getStudentByPAN(panNumber, schoolId))
                .message("Student Fetched")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
            summary = "Get current student details",
            description = "Fetches the currently logged in student's details.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Current student fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Unauthorized or student not found", content = @Content)
            }
    )
    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<RestResponse<StudentResponseDto>> getCurrentStudent(@RequestAttribute("schoolId") Long schoolId)
    {
        String panNumber = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        StudentResponseDto student = studentService.getStudentByPAN(panNumber, schoolId);

        RestResponse<StudentResponseDto> response = RestResponse.<StudentResponseDto>builder()
                .data(student)
                .message("Current student fetched successfully")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get student's previous schooling records",
            description = "Fetches academic history including past sessions, results, attendance, and grades for the currently logged-in student",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Previous schooling records retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - student not logged in", content = @Content)
            }
    )
    @GetMapping("/me/previous-schooling")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<RestResponse<List<PreviousSchoolingRecordDto>>> getPreviousSchoolingRecords(@RequestAttribute("schoolId") Long schoolId)
    {
        String panNumber = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        List<PreviousSchoolingRecordDto> records = studentService.getPreviousSchoolingRecords(panNumber, schoolId);

        RestResponse<List<PreviousSchoolingRecordDto>> response = RestResponse.<List<PreviousSchoolingRecordDto>>builder()
                .data(records)
                .message("Previous schooling records fetched successfully")
                .status(HttpStatus.OK.value())
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update student information",
            description = "Updates information for active students identified by PAN number.",
            parameters = {
                    @Parameter(name = "panNumber", description = "PAN number of the student", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Student updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or student inactive", content = @Content)
            }
    )
    @PutMapping("/{panNumber}")
    public ResponseEntity<RestResponse<StudentResponseDto>> updateStudent(
            @PathVariable String panNumber,
            @RequestBody UpdateStudentInfo updateStudentInfo
            , @RequestAttribute("schoolId") Long schoolId
    )
    {
        RestResponse<StudentResponseDto> response = RestResponse.<StudentResponseDto>builder()
                .data(studentService.updateStudent(panNumber, updateStudentInfo, schoolId))
                .message("Student updated successfully")
                .status(HttpStatus.OK.value())
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get students present today",
            description = "Returns list of students marked present for today. Optional filter by class ID.",
            parameters = {
                    @Parameter(name = "classId", description = "Optional class ID to filter students")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Present students fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request")
            }
    )
    @GetMapping("/present-today")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_NON_TEACHING_STAFF')")
    public ResponseEntity<RestResponse<CurrentDayAttendance>> getPresentToday(
            @RequestParam(required = false) Long classId
            , @RequestAttribute("schoolId") Long schoolId)
    {

        CurrentDayAttendance attendance = studentService.getStudentsPresentToday(Optional.ofNullable(classId), schoolId);

        String message = (classId != null)
                ? "Students present today in class " + classId + ": " + attendance.getStudentAttendances().size()
                : "Students present today: " + attendance.getStudentAttendances().size();

        return ResponseEntity.ok(
                RestResponse.<CurrentDayAttendance>builder()
                        .data(attendance)
                        .message(message)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Get students by class ID",
            description = "Fetch students for a given class by ID.",
            parameters = {
                    @Parameter(name = "classId", description = "ID of the class", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Students fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT', 'ROLE_NON_TEACHING_STAFF')")
    public ResponseEntity<RestResponse<List<StudentResponseDto>>> getStudentByClassId(@PathVariable Long classId, @RequestAttribute("schoolId") Long schoolId)
    {
        List<StudentResponseDto> list = studentService.getStudentsByClassId(classId, schoolId);
        return ResponseEntity.ok(
                RestResponse.<List<StudentResponseDto>>builder()
                        .data(list)
                        .message("Students in class " + classId + ": " + list.size())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Update students status",
            description = "Mark multiple students as graduate or inactive based on PAN numbers.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Students marked successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or student status", content = @Content)
            }
    )
    @PutMapping("/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<Void>> updateStudentsStatus(
            @RequestBody UpdateStudentStatusRequest request
            , @RequestAttribute("schoolId") Long schoolId)
    {

        studentService.markStudentsGraduateOrInActive(request.getPanNumbers(), request.getStatus(), schoolId);

        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .message("Students updated successfully with status: " + request.getStatus())
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Promote students to another class",
            description = "Promotes a list of active students (by PAN) to a given class in the active session.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Students promoted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid class ID or some PANs inactive/missing", content = @Content)
            }
    )
    @PutMapping("/promote-to/{classId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<Void>> promoteStudentsToClass(
            @PathVariable Long classId,
            @RequestBody List<String> panNumbers,
            @RequestAttribute("schoolId") Long schoolId)
    {
        studentService.promoteStudentsToClass(panNumbers, classId, schoolId);

        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .message("Students promoted to class ID: " + classId)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Assign roll numbers alphabetically",
            description = "Assigns roll numbers to students in a class alphabetically by name.",
            parameters = {
                    @Parameter(name = "classId", description = "ID of the class", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Roll numbers assigned successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @PutMapping("/class/{classId}/assign-roll-numbers-alphabetically")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<Void>> assignRollNumbersAlphabetically(
            @PathVariable Long classId,
            @RequestAttribute("schoolId") Long schoolId)
    {
        studentService.assignRollNumbersAlphabetically(classId, schoolId);

        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .message("Roll numbers assigned alphabetically for class ID: " + classId)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Reassign roll numbers",
            description = "Reassigns roll numbers sequentially for students in a class (useful after a student leaves).",
            parameters = {
                    @Parameter(name = "classId", description = "ID of the class", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Roll numbers reassigned successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
            }
    )
    @PutMapping("/class/{classId}/reassign-roll-numbers")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<Void>> reassignRollNumbers(
            @PathVariable Long classId,
            @RequestAttribute("schoolId") Long schoolId)
    {
        studentService.reassignRollNumbers(classId, schoolId);

        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .message("Roll numbers reassigned for class ID: " + classId)
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

    @Operation(
            summary = "Swap roll numbers of two students",
            description = "Swaps the roll numbers of two students in the same class.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Roll numbers swapped successfully"),
                    @ApiResponse(responseCode = "404", description = "Student not found"),
                    @ApiResponse(responseCode = "400", description = "Invalid request")
            }
    )
    @PutMapping("/swap-roll-numbers")
    public ResponseEntity<RestResponse<Void>> swapRollNumbers(
            @Parameter(description = "PAN of first student") @RequestParam String panNumber1,
            @Parameter(description = "PAN of second student") @RequestParam String panNumber2,
            @RequestAttribute("schoolId") Long schoolId) {
        studentService.swapRollNumbers(panNumber1, panNumber2, schoolId);

        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .message("Roll numbers swapped successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }

}
