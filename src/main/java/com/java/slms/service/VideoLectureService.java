package com.java.slms.service;

import com.java.slms.dto.VideoLectureDTO;

import java.util.List;

public interface VideoLectureService {
    
    VideoLectureDTO createVideoLecture(VideoLectureDTO videoLectureDTO, Long schoolId);
    
    VideoLectureDTO updateVideoLecture(Long id, VideoLectureDTO videoLectureDTO, Long schoolId);
    
    void deleteVideoLecture(Long id, Long schoolId);
    
    VideoLectureDTO getVideoLectureById(Long id, Long schoolId);
    
    List<VideoLectureDTO> getVideoLecturesByTeacher(Long teacherId, Long schoolId);
    
    List<VideoLectureDTO> getVideoLecturesByClass(String className, String section, Long schoolId);
    
    List<VideoLectureDTO> getVideoLecturesByClassAndSubject(String className, String section, String subject, Long schoolId);
    
    List<VideoLectureDTO> getAllActiveVideoLectures(Long schoolId);
}
