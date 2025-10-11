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
import com.java.slms.repository.SessionRepository;
import com.java.slms.repository.StudentLeaveRecordRepository;
import com.java.slms.repository.StudentRepository;
import com.java.slms.repository.TeacherRepository;
import com.java.slms.service.StudentLeaveService;
import com.java.slms.util.LeaveStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
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

    @Override
    public void createLeaveRequest(StudentLeaveRequestDTO dto)
    {

        Student student = studentRepository.findById(dto.getStudentPan())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Session session = sessionRepository.findByActiveTrue()
                .orElseThrow(() -> new RuntimeException("Active Session not found"));

        // Check for overlapping leaves
        boolean exists = studentLeaveRecordRepository.existsByStudentAndSessionAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                student,
                session,
                dto.getEndDate(),
                dto.getStartDate()
        );

        if (exists)
        {
            throw new DuplicateEntryException("Duplicate entry detected.");
        }

        Teacher teacher = student.getCurrentClass().getClassTeacher();

        teacher = teacherRepository.findById(teacher.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Assigned teacher does not exist."));

        StudentLeaveRecord leave = new StudentLeaveRecord();
        leave.setStudent(student);
        leave.setTeacher(teacher);
        leave.setSession(session);
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
    public List<StudentLeaveResponse> getLeavesForLoggedInStudent(String panNumber)
    {
        Student student = studentRepository.findById(panNumber)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Session session = sessionRepository.findByActiveTrue()
                .orElseThrow(() -> new RuntimeException("Active Session not found"));

        List<StudentLeaveRecord> records = studentLeaveRecordRepository
                .findByStudentAndSessionOrderByStartDateDesc(student, session);

        return records.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void takeActionOnLeave(Long leaveId, Long teacherId, LeaveActionRequest request)
    {
        StudentLeaveRecord leave = studentLeaveRecordRepository.findById(leaveId)
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
    public List<StudentLeaveResponse> getLeavesForTeacher(Long teacherId, LeaveStatus status)
    {
        Session session = sessionRepository.findByActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Active session not found"));

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        List<StudentLeaveRecord> records;

        if (status != null)
        {
            records = studentLeaveRecordRepository
                    .findBySessionAndTeacherAndStatusOrderByCreatedAtDesc(session, teacher, status);
        }
        else
        {
            records = studentLeaveRecordRepository
                    .findBySessionAndTeacherOrderByCreatedAtDesc(session, teacher);
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
