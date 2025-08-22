package com.java.slms.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrUpdateSessionRequest
{
    private String name;

    private LocalDate startDate;

    private LocalDate endDate;

    private boolean active;
}
