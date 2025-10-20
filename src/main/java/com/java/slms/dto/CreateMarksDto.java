package com.java.slms.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateMarksDto
{
    private Long examTypeId;
    private int maxMarks;
    private int passingMarks;
    private List<MarksDto> marks;
}
