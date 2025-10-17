package com.java.slms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamTypeRequestDto
{
    @NotBlank(message = "Name is mandatory")
    private String name;

    private String description;
}