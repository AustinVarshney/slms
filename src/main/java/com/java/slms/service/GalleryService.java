package com.java.slms.service;

import com.java.slms.dto.BulkGalleryRequestDto;
import com.java.slms.dto.GalleryRequestDto;
import com.java.slms.dto.GalleryResponseDto;

import java.util.List;

public interface GalleryService
{

    GalleryResponseDto addGallery(GalleryRequestDto dto);

    List<GalleryResponseDto> getAllGalleryItems();

    GalleryResponseDto getGalleryById(Long id);

    GalleryResponseDto updateGallery(Long id, GalleryRequestDto dto);

    void deleteGallery(Long id);

    List<GalleryResponseDto> getGalleryItemsBySessionId(Long sessionId);

    List<GalleryResponseDto> addBulkGalleryImages(BulkGalleryRequestDto dto);

}

