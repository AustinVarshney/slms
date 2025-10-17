package com.java.slms.serviceImpl;

import com.java.slms.dto.StaffLeaveAllowanceDto;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.Session;
import com.java.slms.model.Staff;
import com.java.slms.model.StaffLeaveAllowance;
import com.java.slms.repository.SessionRepository;
import com.java.slms.repository.StaffLeaveAllowanceRepository;
import com.java.slms.repository.StaffRepository;
import com.java.slms.service.StaffLeaveAllowanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffLeaveAllowanceServiceImpl implements StaffLeaveAllowanceService
{

    private final StaffLeaveAllowanceRepository allowanceRepo;
    private final StaffRepository staffRepository;
    private final SessionRepository sessionRepo;

    @Override
    public void createLeaveAllowanceInCurrentSession(StaffLeaveAllowanceDto dto, Long schoolId)
    {
        Staff staff = staffRepository.findByIdAndSchoolId(dto.getStaffId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found for this school"));

        Session session = sessionRepo.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + dto.getSessionId()));

        boolean exists = allowanceRepo.findByStaffAndSessionAndSchoolId(staff, session, schoolId).isPresent();
        if (exists)
        {
            throw new WrongArgumentException("Leave allowance already exists for this staff in the selected session.");
        }

        StaffLeaveAllowance allowance = new StaffLeaveAllowance();
        allowance.setStaff(staff);
        allowance.setSession(session);
        allowance.setSchool(staff.getSchool());
        allowance.setAllowedLeaves(dto.getAllowedLeaves());

        allowanceRepo.save(allowance);
    }

    @Override
    public void updateLeaveAllowance(Long allowanceId, StaffLeaveAllowanceDto dto, Long schoolId)
    {
        StaffLeaveAllowance allowance = allowanceRepo
                .findByIdAndActiveSessionAndSchoolId(allowanceId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave allowance not found with ID: " + allowanceId + " in active session for this school"));

        Staff staff = staffRepository.findByIdAndSchoolId(dto.getStaffId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found for this school"));

        Session session = sessionRepo.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Active Session not found"));

        allowance.setStaff(staff);
        allowance.setSession(session);
        allowance.setSchool(staff.getSchool());
        allowance.setAllowedLeaves(dto.getAllowedLeaves());

        allowanceRepo.save(allowance);
    }

    @Override
    public StaffLeaveAllowanceDto getAllowance(Long staffId, Long sessionId, Long schoolId)
    {
        Staff staff = staffRepository.findByIdAndSchoolId(staffId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found for this school"));

        Session session = sessionRepo.findBySessionIdAndSchoolId(sessionId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));

        StaffLeaveAllowance allowance = allowanceRepo
                .findByStaffAndSessionAndSchoolId(staff, session, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave allowance not found"));

        return StaffLeaveAllowanceDto.builder()
                .staffId(allowance.getStaff().getId())
                .sessionId(allowance.getSession().getId())
                .allowedLeaves(allowance.getAllowedLeaves())
                .build();
    }

    @Override
    public List<StaffLeaveAllowanceDto> getAllowancesForSession(Long sessionId, Long schoolId)
    {
        Session session = sessionRepo.findBySessionIdAndSchoolId(sessionId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + sessionId));

        return allowanceRepo.findAllBySessionIdAndSchoolId(sessionId, schoolId)
                .stream()
                .map(allowance -> StaffLeaveAllowanceDto.builder()
                        .staffId(allowance.getStaff().getId())
                        .sessionId(allowance.getSession().getId())
                        .allowedLeaves(allowance.getAllowedLeaves())
                        .build())
                .toList();
    }
}
