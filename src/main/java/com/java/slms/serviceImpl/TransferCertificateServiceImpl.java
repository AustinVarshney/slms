package com.java.slms.serviceImpl;

import com.java.slms.dto.AdminToTeacherDto;
import com.java.slms.dto.TCReasonDto;
import com.java.slms.dto.TeacherToAdminDto;
import com.java.slms.dto.TransferCertificateRequestDto;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.*;
import com.java.slms.repository.*;
import com.java.slms.service.TransferCertificateRequestService;
import com.java.slms.util.RequestStatus;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferCertificateServiceImpl implements TransferCertificateRequestService
{

    private final StudentRepository studentRepository;
    private final TransferCertificateRequestRepository tcRequestRepository;
    private final ModelMapper modelMapper;
    private final SessionRepository sessionRepository;
    private final ClassEntityRepository classEntityRepository;
    private final SchoolRepository schoolRepository;

    @Transactional
    @Override
    public TransferCertificateRequestDto createTransferCertificateRequest(String studentPan, TCReasonDto reasonDto, Long schoolId)
    {
        School school = schoolRepository.findById(schoolId).orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + schoolId));

        Student student = studentRepository.findByPanNumberIgnoreCaseAndSchool_IdAndStatusActive(studentPan, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with PAN Number: " + studentPan +
                                " and ACTIVE status in school with ID: " + schoolId));

        boolean exists = tcRequestRepository.existsByStudentAndStatusInAndSchoolId(student, List.of(RequestStatus.PENDING, RequestStatus.APPROVED), schoolId);
        if (exists)
        {
            throw new AlreadyExistException("Transfer Certificate request already exists or approved for this student.");
        }
        Session activeSession = sessionRepository.findBySchoolIdAndActiveTrue(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("No active session found"));

        TransferCertificateRequest tcRequest = buildTransferCertificateRequest(student, activeSession, reasonDto);
        tcRequest.setSchool(school);
        TransferCertificateRequest savedRequest = tcRequestRepository.save(tcRequest);

        return mapToDto(savedRequest);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TransferCertificateRequestDto> getAllRequestsByStudentPan(String studentPan, Long schoolId)
    {
        Student student = studentRepository.findByPanNumberIgnoreCaseAndSchool_Id(studentPan, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with PAN: " + studentPan));

        List<TransferCertificateRequest> tcRequests = tcRequestRepository.findByStudentAndSchoolId(student, schoolId);

        return tcRequests.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public TransferCertificateRequestDto processRequestDecision(Long requestId, String adminReply, RequestStatus decision, Long schoolId)
    {
        validateDecision(decision);

        TransferCertificateRequest request = fetchRequestById(requestId, schoolId);

        // Only validate that request is not already processed by admin
        if (request.getStatus() == RequestStatus.APPROVED || request.getStatus() == RequestStatus.REJECTED)
        {
            throw new WrongArgumentException("Request has already been processed by admin.");
        }

        request.setStatus(decision);
        request.setAdminReplyToStudent(adminReply);
        request.setAdminActionDate(LocalDate.now());

        TransferCertificateRequest updatedRequest = tcRequestRepository.save(request);

        return mapToDto(updatedRequest);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TransferCertificateRequestDto> getAllRequests(RequestStatus status, Long schoolId)
    {

        List<TransferCertificateRequest> requests = (status != null) ?
                tcRequestRepository.findAllByStatusAndSchoolId(status, schoolId) :
                tcRequestRepository.findAllBySchoolId(schoolId);

        return requests.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferCertificateRequestDto> getAllRequestForwardedByAdminToClassTeacher(Teacher teacher, Long schoolId)
    {
        List<TransferCertificateRequest> requests = tcRequestRepository
                .findByApprovedByClassTeacherAndStatusAndSchoolId(teacher, RequestStatus.PROCESSING, schoolId);

        return requests.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void forwardTCRequestToClassTeacher(Long tcRequestId, Admin admin, AdminToTeacherDto adminToTeacherDto, Long schoolId)
    {
        TransferCertificateRequest request = fetchRequestById(tcRequestId, schoolId);

        if (request.getStatus().equals(RequestStatus.APPROVED))
        {
            throw new WrongArgumentException("Request already approved with id " + request.getId());
        }

        request.setAdminMessageToTeacher(adminToTeacherDto.getAdminMessageToTeacher());
        request.setApprovedByClassTeacher(request.getLastClass().getClassTeacher());
        request.setStatus(RequestStatus.PROCESSING);
        request.setClassTeacherApprovalStatus(RequestStatus.PENDING);
        request.setApprovedByAdmin(admin);

        tcRequestRepository.save(request);
    }

    @Override
    public void replyTCRequestToAdmin(Long tcRequestId, Teacher teacher, TeacherToAdminDto teacherToAdminDto, Long schoolId)
    {
        TransferCertificateRequest request = fetchRequestById(tcRequestId, schoolId);

        if (!teacher.getId().equals(request.getApprovedByClassTeacher().getId()))
        {
            throw new WrongArgumentException("You are not authorized to respond to this TC request.");
        }

        request.setClassTeacherApprovalStatus(teacherToAdminDto.getStatus());
        request.setTeacherReplyToAdmin(teacherToAdminDto.getTeacherReplyToAdmin());
        request.setTeacherActionDate(LocalDate.now());

        tcRequestRepository.save(request);
    }

    // Private helper methods

    private TransferCertificateRequest buildTransferCertificateRequest(Student student, Session activeSession, TCReasonDto reasonDto)
    {
        TransferCertificateRequest tcRequest = new TransferCertificateRequest();
        tcRequest.setStudent(student);
        tcRequest.setSession(activeSession);
        tcRequest.setLastClass(student.getCurrentClass());
        tcRequest.setRequestDate(LocalDate.now());
        tcRequest.setStatus(RequestStatus.PENDING);
        tcRequest.setReason(reasonDto.getReason());
        return tcRequest;
    }

    private TransferCertificateRequestDto mapToDto(TransferCertificateRequest tcRequest)
    {
        TransferCertificateRequestDto dto = modelMapper.map(tcRequest, TransferCertificateRequestDto.class);
        dto.setStudentPanNumber(tcRequest.getStudent().getPanNumber());
        dto.setStudentName(tcRequest.getStudent().getName());
        dto.setSessionId(tcRequest.getSession().getId());
        dto.setSessionName(tcRequest.getSession().getName());
        dto.setClassId(tcRequest.getLastClass().getId());
        dto.setClassName(tcRequest.getLastClass().getClassName());
        dto.setSchoolId(tcRequest.getSchool().getId());
        return dto;
    }

    private void validateDecision(RequestStatus decision)
    {
        if (decision != RequestStatus.APPROVED && decision != RequestStatus.REJECTED)
        {
            throw new WrongArgumentException("Decision must be either APPROVED or REJECTED");
        }
    }

    private TransferCertificateRequest fetchRequestById(Long requestId, Long schoolId)
    {
        return tcRequestRepository.findByIdAndSchoolId(requestId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));
    }

    private void validateRequestPending(TransferCertificateRequest request)
    {
        RequestStatus status = request.getStatus();
        RequestStatus teacherStatus = request.getClassTeacherApprovalStatus();

        if (status == RequestStatus.APPROVED || status == RequestStatus.REJECTED)
        {
            throw new WrongArgumentException("Request has already been processed by admin.");
        }

        if (teacherStatus == null || teacherStatus == RequestStatus.PENDING)
        {
            throw new WrongArgumentException("Class teacher has not yet responded to the request.");
        }

    }

}
