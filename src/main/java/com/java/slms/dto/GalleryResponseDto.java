package com.java.slms.dto;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GalleryResponseDto
{
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String cloudinaryPublicId;
    private String uploadedByType;
    private Long uploadedById;
    private String uploadedByName;
    private Long sessionId;
    private String sessionName;
    private Date createdAt;
    private Date updatedAt;
}
