package com.java.slms.controller;

import com.java.slms.dto.TimetableRequestDTO;
import com.java.slms.dto.TimetableResponseDTO;
import com.java.slms.model.Student;
import com.java.slms.model.Teacher;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.StudentService;
import com.java.slms.service.TeacherService;
import com.java.slms.service.TimeTableService;
import com.java.slms.util.DayOfWeek;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetables")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
@Tag(name = "Time Table Controller", description = "APIs for managing time tables")
public class TimeTableController
{
    private final TimeTableService timetableService;
    private final TeacherService teacherService;
    private final StudentService studentService;

    @Operation(
            summary = "Create a new timetable entry",
            description = "Creates a new timetable entry for a specific class, subject, and day.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Timetable created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid timetable data", content = @Content)
            }
    )
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<TimetableResponseDTO>> createTimetable(
            @RequestBody TimetableRequestDTO dto
            , @RequestAttribute("schoolId") Long schoolId)
    {
        log.info("Creating timetable for classId={}, subjectId={}, day={}", dto.getClassId(), dto.getSubjectId(), dto.getDay());
        TimetableResponseDTO saved = timetableService.createTimetable(dto, schoolId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RestResponse.<TimetableResponseDTO>builder()
                        .data(saved)
                        .message("Timetable created successfully")
                        .status(HttpStatus.CREATED.value())
                        .build());
    }

    @Operation(
            summary = "Get timetable by class and optional day",
            description = "Retrieves timetable entries for a class, optionally filtered by day of the week.",
            parameters = {
                    @Parameter(name = "classId", description = "ID of the class", required = true),
                    @Parameter(name = "day", description = "Optional day filter (e.g., MONDAY, TUESDAY)")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Timetable fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid class ID or day value", content = @Content)
            }
    )
    @GetMapping("/class/{classId}/timetable")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<RestResponse<List<TimetableResponseDTO>>> getTimetableByClassAndOptionalDay(
            @PathVariable Long classId,
            @RequestParam(value = "day", required = false) DayOfWeek day
            , @RequestAttribute("schoolId") Long schoolId
    )
    {
        log.info("Fetching timetable for classId={}{}", classId, day != null ? " on day=" + day : "");

        List<TimetableResponseDTO> data = timetableService.getTimetableByClassAndOptionalDay(classId, day, schoolId);

        return ResponseEntity.ok(RestResponse.<List<TimetableResponseDTO>>builder()
                .data(data)
                .message("Timetable fetched successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    /**
     * Get the timetable for the currently logged-in student.
     */
    @Operation(
            summary = "Get current student's timetable",
            description = "Retrieves the timetable for the current student in the authenticated session, optionally filtered by day.",
            parameters = {
                    @Parameter(name = "day", description = "Optional day of the week for which to filter the timetable (e.g., MONDAY, TUESDAY)", required = false)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Timetable fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or student not found", content = @Content)
            }
    )
    @GetMapping("/student/me")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<RestResponse<List<TimetableResponseDTO>>> getTimetableByCurrentStudent(
            @RequestParam(value = "day", required = false) DayOfWeek day
            , @RequestAttribute("schoolId") Long schoolId
    )
    {
        String panNumber = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Student student = studentService.getActiveStudentByPan(panNumber, schoolId);

        log.info("Fetching timetable for classId={}{}", student.getCurrentClass().getId(), day != null ? " on day=" + day : "");

        List<TimetableResponseDTO> data = timetableService.getTimetableByClassAndOptionalDay(student.getCurrentClass().getId(), day, schoolId);

        return ResponseEntity.ok(RestResponse.<List<TimetableResponseDTO>>builder()
                .data(data)
                .message("Timetable fetched successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    @Operation(
            summary = "Get timetable by teacher ID",
            description = "Retrieves timetable entries of a teacher for the current session.",
            parameters = {
                    @Parameter(name = "teacherId", description = "ID of the teacher", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Timetable fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid teacher ID", content = @Content)
            }
    )
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<RestResponse<List<TimetableResponseDTO>>> getByTeacherId(@PathVariable Long teacherId, @RequestAttribute("schoolId") Long schoolId)
    {
        log.info("Fetching timetable for teacherId={}", teacherId);
        List<TimetableResponseDTO> data = timetableService.getTimetableByTeacherIdInCurrentSession(teacherId, schoolId);
        return ResponseEntity.ok(RestResponse.<List<TimetableResponseDTO>>builder()
                .data(data)
                .message("Timetable fetched successfully")
                .status(HttpStatus.OK.value())
                .build());
    }


    /**
     * Get timetable of the currently logged-in teacher.
     */
    @Operation(
            summary = "Get current teacher's timetable",
            description = "Retrieves the timetable for the currently authenticated teacher in the current academic session using JWT authentication.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Timetable fetched successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request or teacher not found", content = @Content)
            }
    )
    @GetMapping("/teacher/me")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<RestResponse<List<TimetableResponseDTO>>> getByTimeTableOfCurrentTeacher(@RequestAttribute("schoolId") Long schoolId)
    {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Teacher teacher = teacherService.getActiveTeacherByEmail(email, schoolId);

        log.info("Fetching timetable for teacherId={}", teacher.getId());
        List<TimetableResponseDTO> data = timetableService.getTimetableByTeacherIdInCurrentSession(teacher.getId(), schoolId);
        return ResponseEntity.ok(RestResponse.<List<TimetableResponseDTO>>builder()
                .data(data)
                .message("Timetable fetched successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    @Operation(
            summary = "Update timetable entry",
            description = "Updates an existing timetable entry by ID.",
            parameters = {
                    @Parameter(name = "id", description = "Timetable entry ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Timetable updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid timetable data or ID", content = @Content)
            }
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<TimetableResponseDTO>> updateTimetable(
            @PathVariable Long id, @RequestBody TimetableRequestDTO dto, @RequestAttribute("schoolId") Long schoolId)
    {
        log.info("Updating timetable with id={}", id);
        TimetableResponseDTO updated = timetableService.updateTimetable(id, dto, schoolId);
        return ResponseEntity.ok(RestResponse.<TimetableResponseDTO>builder()
                .data(updated)
                .message("Timetable updated successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    @Operation(
            summary = "Delete timetable entry",
            description = "Deletes a timetable entry by its ID.",
            parameters = {
                    @Parameter(name = "id", description = "Timetable entry ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Timetable deleted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid timetable ID", content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<RestResponse<Void>> deleteTimetable(@PathVariable Long id, @RequestAttribute("schoolId") Long schoolId)
    {
        log.info("Deleting timetable with id={}", id);
        timetableService.deleteTimetable(id, schoolId);
        return ResponseEntity.ok(RestResponse.<Void>builder()
                .message("Timetable deleted successfully")
                .status(HttpStatus.OK.value())
                .build());
    }
}
