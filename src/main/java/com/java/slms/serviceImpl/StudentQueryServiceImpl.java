package com.java.slms.serviceImpl;

import com.java.slms.dto.StudentQueryRequest;
import com.java.slms.dto.StudentQueryResponse;
import com.java.slms.dto.TeacherResponseDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.Student;
import com.java.slms.model.StudentQuery;
import com.java.slms.model.Teacher;
import com.java.slms.repository.StudentQueryRepository;
import com.java.slms.repository.StudentRepository;
import com.java.slms.repository.TeacherRepository;
import com.java.slms.service.StudentQueryService;
import com.java.slms.util.EntityFetcher;
import com.java.slms.util.EntityNames;
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
public class StudentQueryServiceImpl implements StudentQueryService
{
    private final StudentQueryRepository studentQueryRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ModelMapper modelMapper;

    @Override
    public StudentQueryResponse askQueryToTeacher(String pan, StudentQueryRequest studentQueryRequest)
    {
        Student student = EntityFetcher.fetchByIdOrThrow(studentRepository, pan, EntityNames.STUDENT);
        Teacher teacher = teacherRepository.findById(studentQueryRequest.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not present with id " + studentQueryRequest.getTeacherId()));

        StudentQuery studentQuery = modelMapper.map(studentQueryRequest, StudentQuery.class);
        studentQuery.setStudent(student);
        studentQuery.setTeacher(teacher);
        studentQuery.setStatus(QueryStatus.OPEN);
        studentQuery.setId(null);
        StudentQuery raisedQuery = studentQueryRepository.save(studentQuery);
        StudentQueryResponse studentQueryResponse = modelMapper.map(raisedQuery, StudentQueryResponse.class);
        studentQueryResponse.setCreatedAt(raisedQuery.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        studentQueryResponse.setTeacherId(raisedQuery.getTeacher().getId());
        studentQueryResponse.setTeacherName(raisedQuery.getTeacher().getName());
        return studentQueryResponse;
    }

    @Override
    public List<StudentQueryResponse> getAllQueriesByStudent(String pan, QueryStatus status)
    {
        Student student = EntityFetcher.fetchByIdOrThrow(studentRepository, pan, EntityNames.STUDENT);

        List<StudentQuery> queries;

        if (status != null)
        {
            queries = studentQueryRepository.findByStudentAndStatus(student, status);
        }
        else
        {
            queries = studentQueryRepository.findByStudent(student);
        }

        return queries.stream().map(query ->
        {
            StudentQueryResponse response = modelMapper.map(query, StudentQueryResponse.class);
            response.setCreatedAt(query.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            response.setTeacherId(query.getTeacher().getId());
            response.setTeacherName(query.getTeacher().getName());
            return response;
        }).toList();
    }

    @Override
    public StudentQueryResponse respondToQuery(Long teacherId, TeacherResponseDto responseRequest)
    {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not present with id " + teacherId));

        StudentQuery query = studentQueryRepository.findById(responseRequest.getQueryId())
                .orElseThrow(() -> new ResourceNotFoundException("Query not found with ID: " + responseRequest.getQueryId()));

        if (!query.getTeacher().getId().equals(teacher.getId()))
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

        StudentQuery updatedQuery = studentQueryRepository.save(query);

        StudentQueryResponse response = modelMapper.map(updatedQuery, StudentQueryResponse.class);
        response.setCreatedAt(query.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        response.setTeacherId(updatedQuery.getTeacher().getId());
        response.setTeacherName(updatedQuery.getTeacher().getName());
        return response;
    }

    @Override
    public List<StudentQueryResponse> getAllQueriesAssignedToTeacher(Long teacherId, QueryStatus status)
    {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not present with id " + teacherId));
        List<StudentQuery> queries;

        if (status != null)
        {
            queries = studentQueryRepository.findByTeacherAndStatus(teacher, status);
        }
        else
        {
            queries = studentQueryRepository.findByTeacher(teacher);
        }

        return queries.stream().map(query ->
        {
            StudentQueryResponse response = modelMapper.map(query, StudentQueryResponse.class);
            response.setCreatedAt(query.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            response.setTeacherId(query.getTeacher().getId());
            response.setTeacherName(query.getTeacher().getName());
            return response;
        }).toList();
    }


}
