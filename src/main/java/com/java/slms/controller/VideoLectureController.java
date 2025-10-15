package com.java.slms.controller;

import com.java.slms.dto.VideoLectureDTO;
import com.java.slms.service.VideoLectureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/video-lectures")
@CrossOrigin(origins = "*")
public class VideoLectureController {
    
    @Autowired
    private VideoLectureService videoLectureService;
    
    @PostMapping
    public ResponseEntity<VideoLectureDTO> createVideoLecture(@RequestBody VideoLectureDTO videoLectureDTO) {
        try {
            VideoLectureDTO created = videoLectureService.createVideoLecture(videoLectureDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<VideoLectureDTO> updateVideoLecture(
            @PathVariable Long id, 
            @RequestBody VideoLectureDTO videoLectureDTO) {
        try {
            VideoLectureDTO updated = videoLectureService.updateVideoLecture(id, videoLectureDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideoLecture(@PathVariable Long id) {
        try {
            videoLectureService.deleteVideoLecture(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<VideoLectureDTO> getVideoLectureById(@PathVariable Long id) {
        try {
            VideoLectureDTO videoLecture = videoLectureService.getVideoLectureById(id);
            return ResponseEntity.ok(videoLecture);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<VideoLectureDTO>> getVideoLecturesByTeacher(@PathVariable Long teacherId) {
        List<VideoLectureDTO> lectures = videoLectureService.getVideoLecturesByTeacher(teacherId);
        return ResponseEntity.ok(lectures);
    }
    
    @GetMapping("/class/{className}/section/{section}")
    public ResponseEntity<List<VideoLectureDTO>> getVideoLecturesByClass(
            @PathVariable String className, 
            @PathVariable String section) {
        List<VideoLectureDTO> lectures = videoLectureService.getVideoLecturesByClass(className, section);
        return ResponseEntity.ok(lectures);
    }
    
    @GetMapping("/class/{className}/section/{section}/subject/{subject}")
    public ResponseEntity<List<VideoLectureDTO>> getVideoLecturesByClassAndSubject(
            @PathVariable String className,
            @PathVariable String section,
            @PathVariable String subject) {
        List<VideoLectureDTO> lectures = videoLectureService.getVideoLecturesByClassAndSubject(className, section, subject);
        return ResponseEntity.ok(lectures);
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<VideoLectureDTO>> getAllActiveVideoLectures() {
        List<VideoLectureDTO> lectures = videoLectureService.getAllActiveVideoLectures();
        return ResponseEntity.ok(lectures);
    }
}
