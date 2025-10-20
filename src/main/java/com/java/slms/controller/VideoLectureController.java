package com.java.slms.controller;

import com.java.slms.dto.VideoLectureDTO;
import com.java.slms.payload.RestResponse;
import com.java.slms.service.VideoLectureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/video-lectures")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER', 'ROLE_STUDENT')")
@Tag(name = "Video Lecture Controller", description = "APIs for managing video lectures")
public class VideoLectureController {
    
    private final VideoLectureService videoLectureService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    @Operation(summary = "Create video lecture", description = "Teacher/Admin creates a new video lecture")
    public ResponseEntity<RestResponse<VideoLectureDTO>> createVideoLecture(
            @RequestBody VideoLectureDTO videoLectureDTO,
            @RequestAttribute("schoolId") Long schoolId) {
        VideoLectureDTO created = videoLectureService.createVideoLecture(videoLectureDTO, schoolId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                RestResponse.<VideoLectureDTO>builder()
                        .data(created)
                        .message("Video lecture created successfully")
                        .status(HttpStatus.CREATED.value())
                        .build()
        );
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    @Operation(summary = "Update video lecture", description = "Update an existing video lecture")
    public ResponseEntity<RestResponse<VideoLectureDTO>> updateVideoLecture(
            @PathVariable Long id, 
            @RequestBody VideoLectureDTO videoLectureDTO,
            @RequestAttribute("schoolId") Long schoolId) {
        VideoLectureDTO updated = videoLectureService.updateVideoLecture(id, videoLectureDTO, schoolId);
        return ResponseEntity.ok(
                RestResponse.<VideoLectureDTO>builder()
                        .data(updated)
                        .message("Video lecture updated successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    @Operation(summary = "Delete video lecture", description = "Delete a video lecture")
    public ResponseEntity<RestResponse<Void>> deleteVideoLecture(
            @PathVariable Long id,
            @RequestAttribute("schoolId") Long schoolId) {
        videoLectureService.deleteVideoLecture(id, schoolId);
        return ResponseEntity.ok(
                RestResponse.<Void>builder()
                        .message("Video lecture deleted successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get video lecture by ID", description = "Retrieve a specific video lecture")
    public ResponseEntity<RestResponse<VideoLectureDTO>> getVideoLectureById(
            @PathVariable Long id,
            @RequestAttribute("schoolId") Long schoolId) {
        VideoLectureDTO videoLecture = videoLectureService.getVideoLectureById(id, schoolId);
        return ResponseEntity.ok(
                RestResponse.<VideoLectureDTO>builder()
                        .data(videoLecture)
                        .message("Video lecture retrieved successfully")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
    
    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "Get lectures by teacher", description = "Retrieve all lectures by a specific teacher")
    public ResponseEntity<RestResponse<List<VideoLectureDTO>>> getVideoLecturesByTeacher(
            @PathVariable Long teacherId,
            @RequestAttribute("schoolId") Long schoolId) {
        List<VideoLectureDTO> lectures = videoLectureService.getVideoLecturesByTeacher(teacherId, schoolId);
        return ResponseEntity.ok(
                RestResponse.<List<VideoLectureDTO>>builder()
                        .data(lectures)
                        .message("Found " + lectures.size() + " video lectures")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
    
    @GetMapping("/class/{className}/section/{section}")
    @Operation(summary = "Get lectures by class", description = "Retrieve all lectures for a specific class and section")
    public ResponseEntity<RestResponse<List<VideoLectureDTO>>> getVideoLecturesByClass(
            @PathVariable String className, 
            @PathVariable String section,
            @RequestAttribute("schoolId") Long schoolId) {
        List<VideoLectureDTO> lectures = videoLectureService.getVideoLecturesByClass(className, section, schoolId);
        return ResponseEntity.ok(
                RestResponse.<List<VideoLectureDTO>>builder()
                        .data(lectures)
                        .message("Found " + lectures.size() + " video lectures")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
    
    @GetMapping("/class/{className}/section/{section}/subject/{subject}")
    @Operation(summary = "Get lectures by class and subject", description = "Retrieve all lectures for a specific class, section and subject")
    public ResponseEntity<RestResponse<List<VideoLectureDTO>>> getVideoLecturesByClassAndSubject(
            @PathVariable String className,
            @PathVariable String section,
            @PathVariable String subject,
            @RequestAttribute("schoolId") Long schoolId) {
        List<VideoLectureDTO> lectures = videoLectureService.getVideoLecturesByClassAndSubject(className, section, subject, schoolId);
        return ResponseEntity.ok(
                RestResponse.<List<VideoLectureDTO>>builder()
                        .data(lectures)
                        .message("Found " + lectures.size() + " video lectures")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
    
    @GetMapping
    @Operation(summary = "Get all lectures", description = "Retrieve all active video lectures for the school")
    public ResponseEntity<RestResponse<List<VideoLectureDTO>>> getAllActiveVideoLectures(
            @RequestAttribute("schoolId") Long schoolId) {
        List<VideoLectureDTO> lectures = videoLectureService.getAllActiveVideoLectures(schoolId);
        return ResponseEntity.ok(
                RestResponse.<List<VideoLectureDTO>>builder()
                        .data(lectures)
                        .message("Found " + lectures.size() + " video lectures")
                        .status(HttpStatus.OK.value())
                        .build()
        );
    }
}
