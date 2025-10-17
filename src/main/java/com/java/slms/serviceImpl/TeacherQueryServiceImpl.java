package com.java.slms.serviceImpl;

import com.java.slms.dto.AdminResponseDto;
import com.java.slms.dto.TeacherQueryRequest;
import com.java.slms.dto.TeacherQueryResponse;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.Admin;
import com.java.slms.model.School;
import com.java.slms.model.Teacher;
import com.java.slms.model.TeacherQuery;
import com.java.slms.repository.AdminRepository;
import com.java.slms.repository.SchoolRepository;
import com.java.slms.repository.TeacherQueryRepository;
import com.java.slms.repository.TeacherRepository;
import com.java.slms.service.TeacherQueryService;
import com.java.slms.util.QueryStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherQueryServiceImpl implements TeacherQueryService
{

    private final TeacherQueryRepository teacherQueryRepository;
    private final TeacherRepository teacherRepository;
    private final AdminRepository adminRepository;
    private final ModelMapper modelMapper;
    private final SchoolRepository schoolRepository;

    @Override
    public TeacherQueryResponse askQueryToAdmin(String teacherEmail, TeacherQueryRequest request, Long schoolId)
    {
        Teacher teacher = teacherRepository.findByEmailIgnoreCaseAndSchoolIdAndStatusActive(teacherEmail, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Active Teacher not found with email: " + teacherEmail));

        Admin admin = adminRepository.findBySchoolId(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));

        TeacherQuery teacherQuery = modelMapper.map(request, TeacherQuery.class);
        teacherQuery.setTeacher(teacher);
        teacherQuery.setAdmin(admin);
        teacherQuery.setStatus(QueryStatus.OPEN);
        teacherQuery.setId(null);
        teacherQuery.setSchool(school);

        TeacherQuery savedQuery = teacherQueryRepository.save(teacherQuery);

        TeacherQueryResponse response = modelMapper.map(savedQuery, TeacherQueryResponse.class);
        response.setCreatedAt(savedQuery.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        response.setAdminId(savedQuery.getAdmin().getId());
        response.setSchoolId(schoolId);
        response.setAdminName(savedQuery.getAdmin().getName());
        return response;
    }

    @Override
    public List<TeacherQueryResponse> getAllQueriesByTeacher(String teacherEmail, QueryStatus status, Long schoolId)
    {
        Teacher teacher = teacherRepository.findByEmailIgnoreCaseAndSchoolIdAndStatusActive(teacherEmail, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Active Teacher not found with email: " + teacherEmail));

        List<TeacherQuery> queries;

        if (status != null)
        {
            queries = teacherQueryRepository.findByTeacherAndStatusAndSchoolId(teacher, status, schoolId);
        }
        else
        {
            queries = teacherQueryRepository.findByTeacherAndSchoolId(teacher, schoolId);
        }

        return queries.stream().map(query ->
        {
            TeacherQueryResponse response = modelMapper.map(query, TeacherQueryResponse.class);
            response.setCreatedAt(query.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            response.setAdminId(query.getAdmin().getId());
            response.setSchoolId(schoolId);
            response.setAdminName(query.getAdmin().getName());
            return response;
        }).toList();
    }

    @Override
    public TeacherQueryResponse respondToTeacherQuery(Admin admin, AdminResponseDto responseRequest, Long schoolId)
    {
        TeacherQuery query = teacherQueryRepository.findByIdAndSchoolId(responseRequest.getQueryId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Query not found with ID: " + responseRequest.getQueryId()));

        if (!query.getAdmin().getId().equals(admin.getId()))
        {
            throw new AccessDeniedException("You are not authorized to respond to this query.");
        }

        if (query.getStatus() != QueryStatus.OPEN)
        {
            throw new WrongArgumentException("Query is not open for response.");
        }

        query.setResponse(responseRequest.getResponse());
        query.setRespondedAt(LocalDateTime.now());
        query.setStatus(QueryStatus.RESPONDED);

        TeacherQuery updatedQuery = teacherQueryRepository.save(query);

        TeacherQueryResponse response = modelMapper.map(updatedQuery, TeacherQueryResponse.class);
        response.setCreatedAt(updatedQuery.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        response.setAdminId(updatedQuery.getAdmin().getId());
        response.setAdminName(updatedQuery.getAdmin().getName());
        response.setSchoolId(schoolId);

        return response;
    }

    @Override
    public List<TeacherQueryResponse> getAllQueriesAssignedToAdmin(Admin admin, QueryStatus status, Long schoolId)
    {
        List<TeacherQuery> queries;

        if (status != null)
        {
            queries = teacherQueryRepository.findByAdminAndStatusAndSchoolId(admin, status, schoolId);
        }
        else
        {
            queries = teacherQueryRepository.findByAdminAndSchoolId(admin, schoolId);
        }

        return queries.stream().map(query ->
        {
            TeacherQueryResponse response = modelMapper.map(query, TeacherQueryResponse.class);
            response.setCreatedAt(query.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            response.setAdminId(query.getAdmin().getId());
            response.setAdminName(query.getAdmin().getName());
            response.setSchoolId(schoolId);
            return response;
        }).toList();
    }

}
