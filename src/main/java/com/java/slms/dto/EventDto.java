package com.java.slms.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDto
{
    private Long id;
    private String title;
    private String type;
    private String banner_url;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long sessionId;
    private String sessionName;
}
