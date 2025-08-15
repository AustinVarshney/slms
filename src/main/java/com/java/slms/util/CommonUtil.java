package com.java.slms.util;

import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.model.ClassEntity;
import com.java.slms.model.Student;
import com.java.slms.repository.ClassEntityRepository;
import com.java.slms.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonUtil
{

    public static ClassEntity fetchClassEntityByClassId(ClassEntityRepository classEntityRepository, Long classId)
    {
        return classEntityRepository.findById(classId).orElseThrow(() ->
        {
            log.error("Class with ID '{}' not found.", classId);
            return new ResourceNotFoundException("Class not found with ID: " + classId);
        });
    }

    public static Student fetchStudentByPan(StudentRepository studentRepository, String pan)
    {
        return studentRepository.findById(pan)
                .orElseThrow(() ->
                {
                    log.error("Student with PAN number '{}' was not found.", pan);
                    return new ResourceNotFoundException("Student with PAN number '" + pan + "' was not found.");
                });

    }

}
