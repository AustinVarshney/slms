package com.java.slms.service;

import com.java.slms.dto.StudentRequestDto;
import com.java.slms.dto.StudentResponseDto;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ExcelStudentParseService
{
    List<StudentRequestDto> uploadStudents(MultipartFile file) throws IOException, CsvValidationException;

    List<StudentRequestDto> uploadCsvStudents(MultipartFile file) throws IOException, CsvValidationException;  // New method for CSV parsing
}
