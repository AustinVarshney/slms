package com.java.slms.serviceImpl;

import com.java.slms.dto.BulkGalleryRequestDto;
import com.java.slms.dto.GalleryRequestDto;
import com.java.slms.dto.GalleryResponseDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.Gallery;
import com.java.slms.model.Session;
import com.java.slms.repository.GalleryRepository;
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

    @Override
    public GalleryResponseDto addGallery(GalleryRequestDto dto)
    {
        Session session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + dto.getSessionId()));

        Gallery gallery = Gallery.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .cloudinaryPublicId(dto.getCloudinaryPublicId())
                .uploadedByType(dto.getUploadedByType())
                .uploadedById(dto.getUploadedById())
                .uploadedByName(dto.getUploadedByName())
                .session(session)
                .build();
        
        gallery = galleryRepository.save(gallery);

        return toResponseDto(gallery);
    }
    
    @Override
    public GalleryResponseDto uploadGalleryImage(MultipartFile file, String title, String description,
                                                 String uploadedByType, Long uploadedById,
                                                 String uploadedByName, Long sessionId) throws IOException {
        // Validate inputs
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + sessionId));

        // Upload to Cloudinary
        Map<String, Object> uploadResult = cloudinaryService.uploadImage(file, "gallery");
        String imageUrl = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");

        // Create gallery entity
        Gallery gallery = Gallery.builder()
                .title(title)
                .description(description)
                .imageUrl(imageUrl)
                .cloudinaryPublicId(publicId)
                .uploadedByType(uploadedByType)
                .uploadedById(uploadedById)
                .uploadedByName(uploadedByName)
                .session(session)
                .build();

        gallery = galleryRepository.save(gallery);
        return toResponseDto(gallery);
    }

    @Override
    public List<GalleryResponseDto> getAllGalleryItems()
    {
        return galleryRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public GalleryResponseDto getGalleryById(Long id)
    {
        Gallery gallery = galleryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gallery item not found with ID: " + id));
        return toResponseDto(gallery);
    }

    @Override
    public GalleryResponseDto updateGallery(Long id, GalleryRequestDto dto)
    {
        Gallery gallery = galleryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gallery item not found with ID: " + id));

        Session session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + dto.getSessionId()));

        gallery.setTitle(dto.getTitle());
        gallery.setDescription(dto.getDescription());
        gallery.setImageUrl(dto.getImageUrl());
        gallery.setCloudinaryPublicId(dto.getCloudinaryPublicId());
        gallery.setUploadedByType(dto.getUploadedByType());
        gallery.setUploadedById(dto.getUploadedById());
        gallery.setUploadedByName(dto.getUploadedByName());
        gallery.setSession(session);
        gallery = galleryRepository.save(gallery);

        return toResponseDto(gallery);
    }

    @Override
    public void deleteGallery(Long id)
    {
        Gallery gallery = galleryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gallery item not found with ID: " + id));
        galleryRepository.delete(gallery);
    }

    @Override
    public List<GalleryResponseDto> getGalleryItemsBySessionId(Long sessionId)
    {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + sessionId));

        return galleryRepository.findBySession_Id(sessionId)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<GalleryResponseDto> addBulkGalleryImages(BulkGalleryRequestDto dto)
    {
        Session session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + dto.getSessionId()));

        List<Gallery> galleries = dto.getImages().stream()
                .map(imageUrl -> Gallery.builder()
                        .imageUrl(imageUrl)
                        .title("Bulk Upload")
                        .description("Uploaded via bulk upload")
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
                .title(gallery.getTitle())
                .description(gallery.getDescription())
                .imageUrl(gallery.getImageUrl())
                .cloudinaryPublicId(gallery.getCloudinaryPublicId())
                .uploadedByType(gallery.getUploadedByType())
                .uploadedById(gallery.getUploadedById())
                .uploadedByName(gallery.getUploadedByName())
                .sessionId(gallery.getSession() != null ? gallery.getSession().getId() : null)
                .sessionName(gallery.getSession() != null ? gallery.getSession().getName() : null)
                .createdAt(gallery.getCreatedAt())
                .updatedAt(gallery.getUpdatedAt())
                .build();
    }
}
