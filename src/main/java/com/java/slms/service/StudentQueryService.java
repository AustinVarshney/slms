package com.java.slms.service;

import com.java.slms.dto.StudentQueryRequest;
import com.java.slms.dto.StudentQueryResponse;
import com.java.slms.dto.TeacherResponseDto;
import com.java.slms.util.QueryStatus;

import java.util.List;

public interface StudentQueryService
{
    StudentQueryResponse askQueryToTeacher(String studentPan, StudentQueryRequest request, Long schoolId);

    List<StudentQueryResponse> getAllQueriesByStudent(String studentPan, QueryStatus status, Long schoolId);

    StudentQueryResponse respondToQuery(Long teacherId, TeacherResponseDto responseRequest, Long schoolId);

    List<StudentQueryResponse> getAllQueriesAssignedToTeacher(Long teacherId, QueryStatus status, Long schoolId);

}
