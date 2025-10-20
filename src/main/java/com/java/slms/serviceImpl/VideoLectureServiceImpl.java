package com.java.slms.serviceImpl;

import com.java.slms.dto.VideoLectureDTO;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.School;
import com.java.slms.model.Teacher;
import com.java.slms.model.VideoLecture;
import com.java.slms.repository.SchoolRepository;
import com.java.slms.repository.TeacherRepository;
import com.java.slms.repository.VideoLectureRepository;
import com.java.slms.service.VideoLectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class VideoLectureServiceImpl implements VideoLectureService {
    
    private final VideoLectureRepository videoLectureRepository;
    private final TeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;
    
    @Override
    public VideoLectureDTO createVideoLecture(VideoLectureDTO videoLectureDTO, Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));
        
        Teacher teacher = teacherRepository.findById(videoLectureDTO.getTeacherId())
            .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + videoLectureDTO.getTeacherId()));
        
        VideoLecture videoLecture = VideoLecture.builder()
                .title(videoLectureDTO.getTitle())
                .description(videoLectureDTO.getDescription())
                .youtubeLink(videoLectureDTO.getYoutubeLink())
                .subject(videoLectureDTO.getSubject())
                .className(videoLectureDTO.getClassName())
                .section(videoLectureDTO.getSection())
                .teacher(teacher)
                .school(school)
                .build();
        
        VideoLecture saved = videoLectureRepository.save(videoLecture);
        return convertToDTO(saved);
    }
    
    @Override
    public VideoLectureDTO updateVideoLecture(Long id, VideoLectureDTO videoLectureDTO, Long schoolId) {
        VideoLecture existing = videoLectureRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Video lecture not found with id: " + id));
        
        // Update fields
        existing.setTitle(videoLectureDTO.getTitle());
        existing.setDescription(videoLectureDTO.getDescription());
        existing.setYoutubeLink(videoLectureDTO.getYoutubeLink());
        existing.setSubject(videoLectureDTO.getSubject());
        existing.setClassName(videoLectureDTO.getClassName());
        existing.setSection(videoLectureDTO.getSection());
        
        VideoLecture updated = videoLectureRepository.save(existing);
        return convertToDTO(updated);
    }
    
    @Override
    public void deleteVideoLecture(Long id, Long schoolId) {
        VideoLecture videoLecture = videoLectureRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Video lecture not found with id: " + id));
        videoLectureRepository.delete(videoLecture);
    }
    
    @Override
    public VideoLectureDTO getVideoLectureById(Long id, Long schoolId) {
        VideoLecture videoLecture = videoLectureRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Video lecture not found with id: " + id));
        return convertToDTO(videoLecture);
    }
    
    @Override
    public List<VideoLectureDTO> getVideoLecturesByTeacher(Long teacherId, Long schoolId) {
        return videoLectureRepository.findByTeacherIdAndSchoolId(teacherId, schoolId)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<VideoLectureDTO> getVideoLecturesByClass(String className, String section, Long schoolId) {
        return videoLectureRepository.findByClassNameAndSectionAndSchoolId(className, section, schoolId)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<VideoLectureDTO> getVideoLecturesByClassAndSubject(String className, String section, String subject, Long schoolId) {
        return videoLectureRepository.findByClassNameAndSectionAndSubjectAndSchoolId(className, section, subject, schoolId)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<VideoLectureDTO> getAllActiveVideoLectures(Long schoolId) {
        return videoLectureRepository.findBySchoolIdOrderByCreatedAtDesc(schoolId)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    private VideoLectureDTO convertToDTO(VideoLecture videoLecture) {
        VideoLectureDTO dto = new VideoLectureDTO();
        BeanUtils.copyProperties(videoLecture, dto);
        if (videoLecture.getTeacher() != null) {
            dto.setTeacherId(videoLecture.getTeacher().getId());
            dto.setTeacherName(videoLecture.getTeacher().getName());
        }
        return dto;
    }
}
