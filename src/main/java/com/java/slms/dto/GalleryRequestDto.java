package com.java.slms.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GalleryRequestDto
{
    private String image;
    private Long sessionId;
}
