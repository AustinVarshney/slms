package com.java.slms.service;

import com.java.slms.dto.AdminResponseDto;
import com.java.slms.dto.TeacherQueryRequest;
import com.java.slms.dto.TeacherQueryResponse;
import com.java.slms.model.Admin;
import com.java.slms.util.QueryStatus;

import java.util.List;

public interface TeacherQueryService
{
    TeacherQueryResponse askQueryToAdmin(String teacherEmail, TeacherQueryRequest request, Long schoolId);

    List<TeacherQueryResponse> getAllQueriesByTeacher(String teacherEmail, QueryStatus status, Long schoolId);

    TeacherQueryResponse respondToTeacherQuery(Admin admin, AdminResponseDto responseRequest, Long schoolId);

    List<TeacherQueryResponse> getAllQueriesAssignedToAdmin(Admin admin, QueryStatus status, Long schoolId);
}
