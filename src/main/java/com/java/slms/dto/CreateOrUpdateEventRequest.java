package com.java.slms.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrUpdateEventRequest
{
    private String title;
    private String type;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}
