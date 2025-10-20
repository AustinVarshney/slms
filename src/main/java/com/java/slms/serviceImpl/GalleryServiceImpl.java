package com.java.slms.serviceImpl;

import com.java.slms.dto.BulkGalleryRequestDto;
import com.java.slms.dto.GalleryRequestDto;
import com.java.slms.dto.GalleryResponseDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.Gallery;
import com.java.slms.model.School;
import com.java.slms.model.Session;
import com.java.slms.repository.GalleryRepository;
import com.java.slms.repository.SchoolRepository;
import com.java.slms.repository.SessionRepository;
import com.java.slms.service.CloudinaryService;
import com.java.slms.service.GalleryService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GalleryServiceImpl implements GalleryService
{
    private final GalleryRepository galleryRepository;
    private final SessionRepository sessionRepository;
    private final CloudinaryService cloudinaryService;
    private final ModelMapper modelMapper;
    private final SchoolRepository schoolRepository;

    @Override
    public GalleryResponseDto addGallery(GalleryRequestDto dto, Long schoolId)
    {
        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));

        Session session = sessionRepository.findBySessionIdAndSchoolId(dto.getSessionId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + dto.getSessionId()));

        Gallery gallery = modelMapper.map(dto, Gallery.class);
        gallery.setSession(session);
        gallery.setId(null);
        gallery.setSchool(school);
        gallery = galleryRepository.save(gallery);

        return toResponseDto(gallery);
    }

    @Override
    public List<GalleryResponseDto> getAllGalleryItems(Long schoolId)
    {
        return galleryRepository.findAllBySchoolId(schoolId)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public GalleryResponseDto getGalleryById(Long id, Long schoolId)
    {
        Gallery gallery = galleryRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Gallery item not found with ID: " + id));
        return toResponseDto(gallery);
    }

    @Override
    public GalleryResponseDto updateGallery(Long id, GalleryRequestDto dto, Long schoolId)
    {
        Gallery gallery = galleryRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Gallery item not found with ID: " + id));
        
        // Update only title and description
        if (dto.getTitle() != null) {
            gallery.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            gallery.setDescription(dto.getDescription());
        }
        
        gallery = galleryRepository.save(gallery);
        return toResponseDto(gallery);
    }

    @Override
    public void deleteGallery(Long id, Long schoolId)
    {
        Gallery gallery = galleryRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Gallery item not found with ID: " + id));
        
        // Delete from Cloudinary if publicId exists
        if (gallery.getCloudinaryPublicId() != null && !gallery.getCloudinaryPublicId().isEmpty()) {
            try {
                cloudinaryService.deleteImage(gallery.getCloudinaryPublicId());
            } catch (IOException e) {
                // Log error but continue with database deletion
                System.err.println("Failed to delete image from Cloudinary: " + e.getMessage());
            }
        }
        
        galleryRepository.delete(gallery);
    }

    @Override
    public List<GalleryResponseDto> getGalleryItemsBySessionId(Long sessionId, Long schoolId)
    {
        return galleryRepository.findBySessionIdAndSchoolId(sessionId, schoolId)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<GalleryResponseDto> addBulkGalleryImages(BulkGalleryRequestDto dto, Long schoolId)
    {
        Session session = sessionRepository.findBySessionIdAndSchoolId(dto.getSessionId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + dto.getSessionId()));

        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));

        List<Gallery> galleries = dto.getImages().stream()
                .map(imageData -> Gallery.builder()
                        .imageUrl(imageData.getImageUrl())
                        .cloudinaryPublicId(imageData.getCloudinaryPublicId())
                        .title(imageData.getTitle())
                        .description(imageData.getDescription())
                        .uploadedByType(dto.getUploadedByType())
                        .uploadedById(dto.getUploadedById())
                        .uploadedByName(dto.getUploadedByName())
                        .school(school)
                        .session(session)
                        .build())
                .collect(Collectors.toList());

        List<Gallery> saved = galleryRepository.saveAll(galleries);

        return saved.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }


    private GalleryResponseDto toResponseDto(Gallery gallery)
    {
        return GalleryResponseDto.builder()
                .id(gallery.getId())
                .imageUrl(gallery.getImageUrl())
                .title(gallery.getTitle())
                .description(gallery.getDescription())
                .cloudinaryPublicId(gallery.getCloudinaryPublicId())
                .uploadedByType(gallery.getUploadedByType())
                .uploadedById(gallery.getUploadedById())
                .uploadedByName(gallery.getUploadedByName())
                .sessionId(gallery.getSession().getId())
                .schoolId(gallery.getSchool().getId())
                .createdAt(gallery.getCreatedAt())
                .updatedAt(gallery.getUpdatedAt())
                .build();
    }
}
