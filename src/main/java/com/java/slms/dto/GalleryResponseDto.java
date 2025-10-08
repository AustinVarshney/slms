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
    private String image;
    private Long sessionId;
    private Date createdAt;
    private Date updatedAt;
}