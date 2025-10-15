package com.java.slms.service;

import com.java.slms.dto.BulkGalleryRequestDto;
import com.java.slms.dto.GalleryRequestDto;
import com.java.slms.dto.GalleryResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface GalleryService
{

    GalleryResponseDto addGallery(GalleryRequestDto dto);
    
    GalleryResponseDto uploadGalleryImage(MultipartFile file, String title, String description, 
                                          String uploadedByType, Long uploadedById, 
                                          String uploadedByName, Long sessionId) throws IOException;

    List<GalleryResponseDto> getAllGalleryItems();

    GalleryResponseDto getGalleryById(Long id);

    GalleryResponseDto updateGallery(Long id, GalleryRequestDto dto);

    void deleteGallery(Long id);

    List<GalleryResponseDto> getGalleryItemsBySessionId(Long sessionId);

    List<GalleryResponseDto> addBulkGalleryImages(BulkGalleryRequestDto dto);

}

