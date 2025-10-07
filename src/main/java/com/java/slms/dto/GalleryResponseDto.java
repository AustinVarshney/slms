package com.java.slms.dto;

import lombok.*;

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
}