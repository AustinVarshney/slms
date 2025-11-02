package com.java.slms.serviceImpl;

import com.java.slms.dto.StudentQueryRequest;
import com.java.slms.dto.StudentQueryResponse;
import com.java.slms.dto.TeacherResponseDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.School;
import com.java.slms.model.Session;
import com.java.slms.model.Student;
import com.java.slms.model.StudentQuery;
import com.java.slms.model.Teacher;
import com.java.slms.repository.SchoolRepository;
import com.java.slms.repository.SessionRepository;
import com.java.slms.repository.StudentQueryRepository;
import com.java.slms.repository.StudentRepository;
import com.java.slms.repository.TeacherRepository;
import com.java.slms.service.StudentQueryService;
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
    private final SchoolRepository schoolRepository;
    private final SessionRepository sessionRepository;

    @Override
    public StudentQueryResponse askQueryToTeacher(String pan, StudentQueryRequest studentQueryRequest, Long schoolId)
    {
        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new ResourceNotFoundException("School not found with Id : " + schoolId));

        // Get active session
        Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("No active session found for school"));

        Student student = studentRepository.findByPanNumberIgnoreCaseAndSchool_IdAndStatusActive(pan, schoolId)
                .orElseThrow(() -> new RuntimeException("Student not found with pan: " + pan));

        Teacher teacher = teacherRepository.findByTeacherIdAndSchoolIdAndStatusActive(studentQueryRequest.getTeacherId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not present with id " + studentQueryRequest.getTeacherId()));

        StudentQuery studentQuery = modelMapper.map(studentQueryRequest, StudentQuery.class);
        studentQuery.setStudent(student);
        studentQuery.setTeacher(teacher);
        studentQuery.setStatus(QueryStatus.OPEN);
        studentQuery.setId(null);
        studentQuery.setSchool(school);
        studentQuery.setSession(activeSession);
        StudentQuery raisedQuery = studentQueryRepository.save(studentQuery);
        StudentQueryResponse studentQueryResponse = modelMapper.map(raisedQuery, StudentQueryResponse.class);
        studentQueryResponse.setCreatedAt(raisedQuery.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        studentQueryResponse.setTeacherId(raisedQuery.getTeacher().getId());
        studentQueryResponse.setTeacherName(raisedQuery.getTeacher().getName());
        return studentQueryResponse;
    }

    @Override
    public List<StudentQueryResponse> getAllQueriesByStudent(String pan, QueryStatus status, Long schoolId)
    {
        // Get active session
        Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("No active session found for school"));

        Student student = studentRepository.findByPanNumberIgnoreCaseAndSchool_IdAndStatusActive(pan, schoolId)
                .orElseThrow(() -> new RuntimeException("Student not found with pan: " + pan));

        List<StudentQuery> queries;

        if (status != null)
        {
            queries = studentQueryRepository.findByStudentAndStatusAndSchoolIdAndSessionId(student, status, schoolId, activeSession.getId());
        }
        else
        {
            queries = studentQueryRepository.findByStudentAndSchoolIdAndSessionId(student, schoolId, activeSession.getId());
        }

        return queries.stream().map(query ->
        {
            StudentQueryResponse response = modelMapper.map(query, StudentQueryResponse.class);
            response.setCreatedAt(query.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            response.setTeacherId(query.getTeacher().getId());
            response.setTeacherName(query.getTeacher().getName());
            response.setSchoolId(schoolId);
            return response;
        }).toList();
    }

    @Override
    public StudentQueryResponse respondToQuery(Long teacherId, TeacherResponseDto responseRequest, Long schoolId)
    {
        // Get active session for proper filtering
        Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("No active session found for school"));

        Teacher teacher = teacherRepository.findByTeacherIdAndSchoolIdAndStatusActive(teacherId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Active teacher not found with id " + teacherId));

        // Find query by ID, schoolId, AND active session (to avoid cross-session query access)
        StudentQuery query = studentQueryRepository.findByIdAndSchoolIdAndSessionId(responseRequest.getQueryId(), schoolId, activeSession.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Query not found with ID: " + responseRequest.getQueryId() + " in active session"));

        if (!query.getTeacher().getId().equals(teacher.getId()))
        {
            throw new AccessDeniedException("You are not authorized to respond to this query.");
        }

        // Allow response if query is OPEN or already RESPONDED (to update response)
        if (query.getStatus() == QueryStatus.CLOSED)
        {
            throw new WrongArgumentException("Query is closed and cannot be responded to.");
        }

        query.setResponse(responseRequest.getResponse());
        query.setRespondedAt(LocalDateTime.now());
        query.setStatus(QueryStatus.RESPONDED);

        StudentQuery updatedQuery = studentQueryRepository.save(query);

        StudentQueryResponse response = modelMapper.map(updatedQuery, StudentQueryResponse.class);
        response.setCreatedAt(query.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        response.setTeacherId(updatedQuery.getTeacher().getId());
        response.setTeacherName(updatedQuery.getTeacher().getName());
        response.setSchoolId(schoolId);
        return response;
    }

    @Override
    public List<StudentQueryResponse> getAllQueriesAssignedToTeacher(Long teacherId, QueryStatus status, Long schoolId)
    {
        // Get active session
        Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("No active session found for school"));

        Teacher teacher = teacherRepository.findByTeacherIdAndSchoolIdAndStatusActive(teacherId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Active teacher not found with id " + teacherId));
        List<StudentQuery> queries;
        if (status != null)
        {
            queries = studentQueryRepository.findByTeacherAndStatusAndSchoolIdAndSessionId(teacher, status, schoolId, activeSession.getId());
        }
        else
        {
            queries = studentQueryRepository.findByTeacherAndSchoolIdAndSessionId(teacher, schoolId, activeSession.getId());
        }

        return queries.stream().map(query ->
        {
            StudentQueryResponse response = modelMapper.map(query, StudentQueryResponse.class);
            response.setCreatedAt(query.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            response.setTeacherId(query.getTeacher().getId());
            response.setTeacherName(query.getTeacher().getName());
            response.setSchoolId(schoolId);
            return response;
        }).toList();
    }


}
