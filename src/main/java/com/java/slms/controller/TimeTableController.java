package com.java.slms.controller;

import com.java.slms.dto.TimetableRequestDTO;
import com.java.slms.dto.TimetableResponseDTO;
import com.java.slms.payload.ApiResponse;
import com.java.slms.service.TimeTableService;
import com.java.slms.util.DayOfWeek;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetables")
@RequiredArgsConstructor
@Slf4j
public class TimeTableController
{
    private final TimeTableService timetableService;

    /**
     * Create a new timetable entry
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TimetableResponseDTO>> createTimetable(
            @RequestBody TimetableRequestDTO dto)
    {
        log.info("Creating timetable for classId={}, subjectId={}, day={}", dto.getClassId(), dto.getSubjectId(), dto.getDay());
        TimetableResponseDTO saved = timetableService.createTimetable(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<TimetableResponseDTO>builder()
                        .data(saved)
                        .message("Timetable created successfully")
                        .status(HttpStatus.CREATED.value())
                        .build());
    }

    /**
     * Get timetable by classId
     */
    @GetMapping("/class/{classId}")
    public ResponseEntity<ApiResponse<List<TimetableResponseDTO>>> getByClassId(@PathVariable Long classId)
    {
        log.info("Fetching timetable for classId={}", classId);
        List<TimetableResponseDTO> data = timetableService.getTimetableByClassId(classId);
        return ResponseEntity.ok(ApiResponse.<List<TimetableResponseDTO>>builder()
                .data(data)
                .message("Timetable fetched successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    /**
     * Get timetable by teacherId
     */
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<ApiResponse<List<TimetableResponseDTO>>> getByTeacherId(@PathVariable Long teacherId)
    {
        log.info("Fetching timetable for teacherId={}", teacherId);
        List<TimetableResponseDTO> data = timetableService.getTimetableByTeacherId(teacherId);
        return ResponseEntity.ok(ApiResponse.<List<TimetableResponseDTO>>builder()
                .data(data)
                .message("Timetable fetched successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    /**
     * Get timetable by day for a class
     */
    @GetMapping("/class/{classId}/day/{day}")
    public ResponseEntity<ApiResponse<List<TimetableResponseDTO>>> getByClassAndDay(
            @PathVariable Long classId, @PathVariable DayOfWeek day)
    {
        log.info("Fetching timetable for classId={} on day={}", classId, day);
        List<TimetableResponseDTO> data = timetableService.getTimetableByClassAndDay(classId, day);
        return ResponseEntity.ok(ApiResponse.<List<TimetableResponseDTO>>builder()
                .data(data)
                .message("Timetable fetched successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    /**
     * Update timetable entry
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TimetableResponseDTO>> updateTimetable(
            @PathVariable Long id, @RequestBody TimetableRequestDTO dto)
    {
        log.info("Updating timetable with id={}", id);
        TimetableResponseDTO updated = timetableService.updateTimetable(id, dto);
        return ResponseEntity.ok(ApiResponse.<TimetableResponseDTO>builder()
                .data(updated)
                .message("Timetable updated successfully")
                .status(HttpStatus.OK.value())
                .build());
    }

    /**
     * Delete timetable entry
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTimetable(@PathVariable Long id)
    {
        log.info("Deleting timetable with id={}", id);
        timetableService.deleteTimetable(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .message("Timetable deleted successfully")
                .status(HttpStatus.OK.value())
                .build());
    }
}
