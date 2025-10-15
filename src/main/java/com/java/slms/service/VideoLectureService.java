package com.java.slms.service;

import com.java.slms.dto.VideoLectureDTO;

import java.util.List;

public interface VideoLectureService {
    
    VideoLectureDTO createVideoLecture(VideoLectureDTO videoLectureDTO);
    
    VideoLectureDTO updateVideoLecture(Long id, VideoLectureDTO videoLectureDTO);
    
    void deleteVideoLecture(Long id);
    
    VideoLectureDTO getVideoLectureById(Long id);
    
    List<VideoLectureDTO> getVideoLecturesByTeacher(Long teacherId);
    
    List<VideoLectureDTO> getVideoLecturesByClass(String className, String section);
    
    List<VideoLectureDTO> getVideoLecturesByClassAndSubject(String className, String section, String subject);
    
    List<VideoLectureDTO> getAllActiveVideoLectures();
}
