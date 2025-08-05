package com.java.slms.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubjectsBulkDto
{
    private String className;
    private List<String> subjectNames;
}