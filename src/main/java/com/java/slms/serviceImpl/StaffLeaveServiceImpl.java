package com.java.slms.serviceImpl;

import com.java.slms.dto.StaffLeaveRequestDto;
import com.java.slms.dto.StaffLeaveResponseDto;
import com.java.slms.dto.StaffLeaveStatusUpdateDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.*;
import com.java.slms.repository.SessionRepository;
import com.java.slms.repository.StaffLeaveAllowanceRepository;
import com.java.slms.repository.StaffLeaveRecordRepository;
import com.java.slms.repository.TeacherRepository;
import com.java.slms.service.StaffLeaveService;
import com.java.slms.util.LeaveStatus;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffLeaveServiceImpl implements StaffLeaveService
{
    private final TeacherRepository teacherRepository;
    private final SessionRepository sessionRepository;
    private final StaffLeaveAllowanceRepository staffLeaveAllowanceRepository;
    private final StaffLeaveRecordRepository staffLeaveRecordRepository;
    private final ModelMapper modelMapper;

    @Override
    public void raiseLeaveRequest(StaffLeaveRequestDto dto)
    {
        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        Session session = sessionRepository.findByActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Active session not found"));

        // Fetch or create leave allowance for the teacher in this session
        StaffLeaveAllowance allowance = staffLeaveAllowanceRepository
                .findByTeacherAndSession(teacher, session)
                .orElseGet(() -> {
                    // Auto-create leave allowance with default 15 days if not found
                    StaffLeaveAllowance newAllowance = new StaffLeaveAllowance();
                    newAllowance.setTeacher(teacher);
                    newAllowance.setSession(session);
                    newAllowance.setAllowedLeaves(15); // Default 15 days per session
                    return staffLeaveAllowanceRepository.save(newAllowance);
                });

        int approvedLeaves = staffLeaveRecordRepository
                .countApprovedLeaves(teacher.getId(), session.getId());

        int daysRequested = (int) ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;

        if (approvedLeaves + daysRequested > allowance.getAllowedLeaves())
        {
            throw new WrongArgumentException("Not enough leave balance. You have " + 
                (allowance.getAllowedLeaves() - approvedLeaves) + " days remaining.");
        }

        StaffLeaveRecord leave = modelMapper.map(dto, StaffLeaveRecord.class);
        leave.setId(null);
        leave.setTeacher(teacher);
        leave.setSession(session);
        leave.setDaysRequested(daysRequested);
        leave.setStatus(LeaveStatus.PENDING);

        staffLeaveRecordRepository.save(leave);
    }

    @Override
    public List<StaffLeaveResponseDto> getMyLeaves(Long teacherId, LeaveStatus status)
    {
        List<StaffLeaveRecord> records = staffLeaveRecordRepository
                .findByTeacherInActiveSessionAndStatus(teacherId, status);

        return records.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StaffLeaveResponseDto> getAllLeavesForAdmin(LeaveStatus status, Long sessionId, Long teacherId)
    {
        if (sessionId == null)
        {
            Session activeSession = sessionRepository.findByActiveTrue()
                    .orElseThrow(() -> new ResourceNotFoundException("Active session not found"));
            sessionId = activeSession.getId();
        }

        List<StaffLeaveRecord> records = staffLeaveRecordRepository.findByAdminFilters(status, sessionId, teacherId);

        return records.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateLeaveStatus(Long leaveId, Admin admin, StaffLeaveStatusUpdateDto dto)
    {
        StaffLeaveRecord record = staffLeaveRecordRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave record not found"));

        if (record.getStatus() != LeaveStatus.PENDING)
        {
            throw new WrongArgumentException("Only pending leaves can be updated");
        }

        record.setStatus(dto.getStatus());
        record.setAdminResponse(dto.getAdminResponse());
        record.setProcessedBy(admin.getName());
        record.setProcessedAt(LocalDateTime.now());

        staffLeaveRecordRepository.save(record);
    }


    private StaffLeaveResponseDto convertToDto(StaffLeaveRecord record)
    {
        StaffLeaveResponseDto dto = modelMapper.map(record, StaffLeaveResponseDto.class);
        if (record.getSession() != null)
        {
            dto.setSessionId(record.getSession().getId());
            dto.setSessionName(record.getSession().getName());
        }
        StaffLeaveAllowance allowance = staffLeaveAllowanceRepository
                .findByTeacherAndSession(record.getTeacher(), record.getSession())
                .orElse(null);

        if (allowance != null)
        {
            dto.setTotalLeavesAllowed(allowance.getAllowedLeaves());

            int usedLeaves = staffLeaveRecordRepository.countApprovedLeaves(record.getTeacher().getId(), record.getSession().getId());
            dto.setRemainingLeavesBalance(allowance.getAllowedLeaves() - usedLeaves);
        }

        return dto;
    }

}
