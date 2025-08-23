package com.java.slms.dto;


import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AttendanceUpdateResult
{
    private List<String> updatedPanNumbers = new ArrayList<>();
    private List<String> invalidPanNumbers = new ArrayList<>();
    private List<String> unchangedPanNumbers = new ArrayList<>();
}
