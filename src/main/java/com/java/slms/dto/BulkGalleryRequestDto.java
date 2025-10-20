package com.java.slms.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkGalleryRequestDto
{
    private Long sessionId;
    private List<ImageData> images;
    private String uploadedByType; // "TEACHER" or "ADMIN"
    private Long uploadedById;
    private String uploadedByName;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImageData {
        private String imageUrl;
        private String cloudinaryPublicId;
        private String title;
        private String description;
    }
}
