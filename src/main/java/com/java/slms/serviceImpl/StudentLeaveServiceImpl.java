package com.java.slms.serviceImpl;

import com.java.slms.dto.LeaveActionRequest;
import com.java.slms.dto.StudentLeaveRequestDTO;
import com.java.slms.dto.StudentLeaveResponse;
import com.java.slms.exception.DuplicateEntryException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.Session;
import com.java.slms.model.Student;
import com.java.slms.model.StudentLeaveRecord;
import com.java.slms.model.Teacher;
import com.java.slms.model.School;
import com.java.slms.repository.SessionRepository;
import com.java.slms.repository.StudentLeaveRecordRepository;
import com.java.slms.repository.StudentRepository;
import com.java.slms.repository.TeacherRepository;
import com.java.slms.repository.SchoolRepository;
import com.java.slms.service.StudentLeaveService;
import com.java.slms.util.LeaveStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentLeaveServiceImpl implements StudentLeaveService
{
    private final StudentRepository studentRepository;

    private final TeacherRepository teacherRepository;

    private final ModelMapper modelMapper;

    private final SessionRepository sessionRepository;

    private final StudentLeaveRecordRepository studentLeaveRecordRepository;
    
    private final SchoolRepository schoolRepository;

    @Override
    public void createLeaveRequest(StudentLeaveRequestDTO dto, Student student, Long schoolId)
    {
        Session session = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new RuntimeException("Active Session not found"));
        
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));

        // Check for overlapping leaves
        boolean exists = studentLeaveRecordRepository.existsByStudentAndSessionAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStudent_School_Id(
                student,
                session,
                dto.getEndDate(),
                dto.getStartDate(),
                schoolId
        );

        if (exists)
        {
            throw new DuplicateEntryException("Duplicate entry detected.");
        }

        Teacher teacher = student.getCurrentClass().getClassTeacher();
        
        if (teacher == null)
        {
            throw new WrongArgumentException("No class teacher assigned to your class. Please contact administration.");
        }

        teacher = teacherRepository.findByTeacherIdAndSchoolIdAndStatusActive(teacher.getId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Assigned teacher does not exist."));

        StudentLeaveRecord leave = new StudentLeaveRecord();
        leave.setStudent(student);
        leave.setTeacher(teacher);
        leave.setSession(session);
        leave.setSchool(school);
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());

        int daysRequested = (int) ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        leave.setDaysRequested(daysRequested);

        leave.setReason(dto.getReason());
        leave.setStatus(LeaveStatus.PENDING);
        leave.setProcessedAt(null);
        leave.setClassTeacherResponse(null);

        log.info("About to save leave for: {}", student.getPanNumber());
        StudentLeaveRecord saved = studentLeaveRecordRepository.save(leave);
        log.info("Saved leave with ID: {}", saved.getId());

    }

    @Override
    public List<StudentLeaveResponse> getLeavesForLoggedInStudent(String panNumber, Long schoolId)
    {
        Student student = studentRepository.findByPanNumberIgnoreCaseAndSchool_IdAndStatusActive(panNumber, schoolId)
                .orElseThrow(() -> new RuntimeException("Student not found with pan: " + panNumber));

        Session session = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new RuntimeException("Active Session not found"));

        List<StudentLeaveRecord> records = studentLeaveRecordRepository
                .findByStudentAndSessionOrderByStartDateDesc(student, session);

        return records.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void takeActionOnLeave(Long leaveId, Long teacherId, Long schoolId, LeaveActionRequest request)
    {
        StudentLeaveRecord leave = studentLeaveRecordRepository.findByIdAndStudent_School_Id(leaveId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave record not found"));

        if (leave.getTeacher() == null || !leave.getTeacher().getId().equals(teacherId))
        {
            throw new WrongArgumentException("You are not authorized to perform action on this leave request.");
        }

        if (leave.getStatus() != LeaveStatus.PENDING)
        {
            throw new WrongArgumentException("Action already taken on this leave");
        }

        leave.setStatus(request.getStatus());
        leave.setProcessedAt(LocalDateTime.now());
        leave.setClassTeacherResponse(request.getResponseMessage());

        studentLeaveRecordRepository.save(leave);
    }

    @Override
    public List<StudentLeaveResponse> getLeavesForTeacher(Long teacherId, LeaveStatus status, Long schoolId)
    {
        Session session = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Active session not found"));

        Teacher teacher = teacherRepository.findByTeacherIdAndSchoolId(teacherId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        List<StudentLeaveRecord> records;

        if (status != null)
        {
            records = studentLeaveRecordRepository
                    .findBySessionAndTeacherAndStatusAndStudent_School_IdOrderByCreatedAtDesc(session, teacher, status, schoolId);
        }
        else
        {
            records = studentLeaveRecordRepository
                    .findBySessionAndTeacherAndStudent_School_IdOrderByCreatedAtDesc(session, teacher, schoolId);
        }

        return records.stream()
                .map(record ->
                {
                    StudentLeaveResponse res = modelMapper.map(record, StudentLeaveResponse.class);
                    res.setSessionName(session.getName());
                    res.setClassTeacherName(teacher.getName());
                    return res;
                })
                .toList();
    }


    private StudentLeaveResponse mapToResponse(StudentLeaveRecord record)
    {
        StudentLeaveResponse response = modelMapper.map(record, StudentLeaveResponse.class);

        response.setClassTeacherName(record.getTeacher().getName());
        response.setSessionName(record.getSession().getName());

        return response;
    }


}
