package com.java.slms.serviceImpl;

import com.java.slms.dto.BulkGalleryRequestDto;
import com.java.slms.dto.GalleryRequestDto;
import com.java.slms.dto.GalleryResponseDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.Gallery;
import com.java.slms.model.Session;
import com.java.slms.repository.GalleryRepository;
import com.java.slms.repository.SessionRepository;
import com.java.slms.service.GalleryService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GalleryServiceImpl implements GalleryService
{
    private final GalleryRepository galleryRepository;
    private final SessionRepository sessionRepository;
    private final ModelMapper modelMapper;

    @Override
    public GalleryResponseDto addGallery(GalleryRequestDto dto)
    {
        Session session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with ID: " + dto.getSessionId()));

        Gallery gallery = modelMapper.map(dto, Gallery.class);
        gallery.setSession(session);
        gallery.setId(null);
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

        gallery.setImage(dto.getImage());
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
                .map(image -> Gallery.builder()
                        .image(image)
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
                .image(gallery.getImage())
                .sessionId(gallery.getSession().getId())
                .build();
    }
}
