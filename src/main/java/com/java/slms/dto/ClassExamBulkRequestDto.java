package com.java.slms.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClassExamBulkRequestDto
{
    private Long examTypeId;

    private List<ClassExamEntry> classExams;
}
