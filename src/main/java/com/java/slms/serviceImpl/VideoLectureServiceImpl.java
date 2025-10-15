package com.java.slms.serviceImpl;

import com.java.slms.dto.VideoLectureDTO;
import com.java.slms.entity.VideoLecture;
import com.java.slms.model.Teacher;
import com.java.slms.repository.TeacherRepository;
import com.java.slms.repository.VideoLectureRepository;
import com.java.slms.service.VideoLectureService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class VideoLectureServiceImpl implements VideoLectureService {
    
    @Autowired
    private VideoLectureRepository videoLectureRepository;
    
    @Autowired
    private TeacherRepository teacherRepository;
    
    @Override
    public VideoLectureDTO createVideoLecture(VideoLectureDTO videoLectureDTO) {
        VideoLecture videoLecture = new VideoLecture();
        BeanUtils.copyProperties(videoLectureDTO, videoLecture, "id", "teacher");
        
        // Fetch teacher
        Teacher teacher = teacherRepository.findById(videoLectureDTO.getTeacherId())
            .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + videoLectureDTO.getTeacherId()));
        
        videoLecture.setTeacher(teacher);
        videoLecture.setTeacherName(teacher.getName());
        
        VideoLecture saved = videoLectureRepository.save(videoLecture);
        return convertToDTO(saved);
    }
    
    @Override
    public VideoLectureDTO updateVideoLecture(Long id, VideoLectureDTO videoLectureDTO) {
        VideoLecture existing = videoLectureRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Video lecture not found with id: " + id));
        
        // Update fields
        existing.setTitle(videoLectureDTO.getTitle());
        existing.setDescription(videoLectureDTO.getDescription());
        existing.setYoutubeLink(videoLectureDTO.getYoutubeLink());
        existing.setSubject(videoLectureDTO.getSubject());
        existing.setClassName(videoLectureDTO.getClassName());
        existing.setSection(videoLectureDTO.getSection());
        existing.setDuration(videoLectureDTO.getDuration());
        existing.setTopic(videoLectureDTO.getTopic());
        existing.setIsActive(videoLectureDTO.getIsActive());
        
        VideoLecture updated = videoLectureRepository.save(existing);
        return convertToDTO(updated);
    }
    
    @Override
    public void deleteVideoLecture(Long id) {
        VideoLecture videoLecture = videoLectureRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Video lecture not found with id: " + id));
        
        // Soft delete
        videoLecture.setIsActive(false);
        videoLectureRepository.save(videoLecture);
    }
    
    @Override
    public VideoLectureDTO getVideoLectureById(Long id) {
        VideoLecture videoLecture = videoLectureRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Video lecture not found with id: " + id));
        return convertToDTO(videoLecture);
    }
    
    @Override
    public List<VideoLectureDTO> getVideoLecturesByTeacher(Long teacherId) {
        return videoLectureRepository.findByTeacherIdAndIsActiveTrue(teacherId)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<VideoLectureDTO> getVideoLecturesByClass(String className, String section) {
        return videoLectureRepository.findByClassNameAndSection(className, section)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<VideoLectureDTO> getVideoLecturesByClassAndSubject(String className, String section, String subject) {
        return videoLectureRepository.findByClassNameAndSectionAndSubject(className, section, subject)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<VideoLectureDTO> getAllActiveVideoLectures() {
        return videoLectureRepository.findByIsActiveTrueOrderByUploadedAtDesc()
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
