package com.java.slms.service;

import com.java.slms.dto.StudentQueryRequest;
import com.java.slms.dto.StudentQueryResponse;
import com.java.slms.dto.TeacherResponseDto;
import com.java.slms.util.QueryStatus;

import java.util.List;

public interface StudentQueryService
{
    StudentQueryResponse askQueryToTeacher(String pan, StudentQueryRequest studentQueryRequest);

    List<StudentQueryResponse> getAllQueriesByStudent(String pan, QueryStatus status);

    StudentQueryResponse respondToQuery(Long teacherId, TeacherResponseDto responseRequest);

    List<StudentQueryResponse> getAllQueriesAssignedToTeacher(Long teacherId, QueryStatus status);


}
