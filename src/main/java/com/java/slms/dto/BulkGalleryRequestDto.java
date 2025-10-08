package com.java.slms.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkGalleryRequestDto
{
    private Long sessionId;
    private List<String> images;
}
