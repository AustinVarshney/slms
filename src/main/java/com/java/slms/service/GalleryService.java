package com.java.slms.service;

import com.java.slms.dto.BulkGalleryRequestDto;
import com.java.slms.dto.GalleryRequestDto;
import com.java.slms.dto.GalleryResponseDto;

import java.util.List;

public interface GalleryService
{
    GalleryResponseDto addGallery(GalleryRequestDto dto, Long schoolId);

    List<GalleryResponseDto> getGalleryItemsBySessionId(Long sessionId, Long schoolId);

    List<GalleryResponseDto> getAllGalleryItems(Long schoolId);

    GalleryResponseDto getGalleryById(Long id, Long schoolId);

    void deleteGallery(Long id, Long schoolId);

    List<GalleryResponseDto> addBulkGalleryImages(BulkGalleryRequestDto dto, Long schoolId);

}

