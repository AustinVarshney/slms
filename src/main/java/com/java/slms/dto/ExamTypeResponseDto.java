package com.java.slms.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamTypeResponseDto
{
    private Long id;
    private String name;
    private String description;
}
