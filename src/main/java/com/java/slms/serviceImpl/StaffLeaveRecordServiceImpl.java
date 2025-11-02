package com.java.slms.serviceImpl;

import com.java.slms.dto.StaffLeaveRequestDto;
import com.java.slms.dto.StaffLeaveResponseDto;
import com.java.slms.dto.StaffLeaveStatusUpdateDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.*;
import com.java.slms.repository.*;
import com.java.slms.service.StaffLeaveRecordService;
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
public class StaffLeaveRecordServiceImpl implements StaffLeaveRecordService
{
    private final TeacherRepository teacherRepository;
    private final SessionRepository sessionRepository;
    private final StaffLeaveAllowanceRepository staffLeaveAllowanceRepository;
    private final StaffLeaveRecordRepository staffLeaveRecordRepository;
    private final ModelMapper modelMapper;
    private final SchoolRepository schoolRepository;
    private final StaffRepository staffRepository;

    @Override
    public void raiseLeaveRequest(StaffLeaveRequestDto dto, Long schoolId, String email)
    {
        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new ResourceNotFoundException("School not found with Id : " + schoolId));

        Session session = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Active session not found"));

        Staff staff = staffRepository
                .findByEmailAndSchoolIdIgnoreCase(email, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff record not found"));

        // Fetch or create leave allowance for the staff in this session
        StaffLeaveAllowance allowance = staffLeaveAllowanceRepository
                .findByStaffAndSessionAndSchoolId(staff, session, schoolId)
                .orElseGet(() -> {
                    // Auto-create leave allowance with default 15 days if not found
                    StaffLeaveAllowance newAllowance = new StaffLeaveAllowance();
                    newAllowance.setStaff(staff);
                    newAllowance.setSession(session);
                    newAllowance.setSchool(school);
                    newAllowance.setAllowedLeaves(15); // Default 15 days per session
                    return staffLeaveAllowanceRepository.save(newAllowance);
                });

        int approvedLeaves = staffLeaveRecordRepository
                .countApprovedLeaves(staff.getId(), session.getId(), schoolId);

        int daysRequested = (int) ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;

        if (approvedLeaves + daysRequested > allowance.getAllowedLeaves())
        {
            throw new WrongArgumentException("Not enough leave balance. You have " + 
                (allowance.getAllowedLeaves() - approvedLeaves) + " days remaining.");
        }

        StaffLeaveRecord leave = modelMapper.map(dto, StaffLeaveRecord.class);
        leave.setId(null);
        leave.setStaff(staff);
        leave.setSession(session);
        leave.setDaysRequested(daysRequested);
        leave.setStatus(LeaveStatus.PENDING);
        leave.setSchool(school);

        staffLeaveRecordRepository.save(leave);
    }

    @Override
    public List<StaffLeaveResponseDto> getMyLeaves(String email, LeaveStatus status, Long schoolId)
    {
        Staff staff = staffRepository
                .findByEmailAndSchoolIdIgnoreCase(
                        email, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff record not found"));

        List<StaffLeaveRecord> records = staffLeaveRecordRepository
                .findByStaffInActiveSessionAndStatus(staff.getId(), status, schoolId);

        return records.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StaffLeaveResponseDto> getAllLeavesForAdmin(LeaveStatus status, Long sessionId, Long staffId, Long schoolId)
    {
        if (sessionId == null)
        {
            Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Active session not found"));
            sessionId = activeSession.getId();
        }

        List<StaffLeaveRecord> records = staffLeaveRecordRepository.findByAdminFilters(status, sessionId, staffId, schoolId);

        return records.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateLeaveStatus(Long leaveId, Admin admin, StaffLeaveStatusUpdateDto dto, Long schoolId)
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
        
        // Set teacher information - fetch from Teacher or NonTeachingStaff table
        if (record.getStaff() != null)
        {
            dto.setTeacherId(record.getStaff().getId());
            
            // Try to get name from Teacher table first
            String staffName = "Staff Member"; // Default fallback
            String staffEmail = record.getStaff().getEmail();
            
            if (staffEmail != null)
            {
                // Check if this is a teacher
                var teacher = teacherRepository.findByEmailIgnoreCaseAndSchoolId(
                    staffEmail, record.getSchool().getId());
                
                if (teacher.isPresent())
                {
                    staffName = teacher.get().getName();
                }
                // Otherwise, it might be non-teaching staff
                // Add NonTeachingStaff repository check if needed
            }
            
            dto.setTeacherName(staffName);
        }
        
        if (record.getSession() != null)
        {
            dto.setSessionId(record.getSession().getId());
            dto.setSessionName(record.getSession().getName());
        }
        StaffLeaveAllowance allowance = staffLeaveAllowanceRepository
                .findByStaffAndSessionAndSchoolId(record.getStaff(), record.getSession(), record.getSchool().getId())
                .orElse(null);

        if (allowance != null)
        {
            dto.setTotalLeavesAllowed(allowance.getAllowedLeaves());

            int usedLeaves = staffLeaveRecordRepository.countApprovedLeaves(record.getStaff().getId(), record.getSession().getId(), record.getSchool().getId());
            dto.setRemainingLeavesBalance(allowance.getAllowedLeaves() - usedLeaves);
        }

        return dto;
    }

}
