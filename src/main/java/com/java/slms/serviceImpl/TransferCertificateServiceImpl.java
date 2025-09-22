package com.java.slms.serviceImpl;

import com.java.slms.dto.AdminToTeacherDto;
import com.java.slms.dto.TCReasonDto;
import com.java.slms.dto.TeacherToAdminDto;
import com.java.slms.dto.TransferCertificateRequestDto;
import com.java.slms.exception.AlreadyExistException;
import com.java.slms.exception.ResourceNotFoundException;
import com.java.slms.exception.WrongArgumentException;
import com.java.slms.model.*;
import com.java.slms.repository.ClassEntityRepository;
import com.java.slms.repository.SessionRepository;
import com.java.slms.repository.StudentRepository;
import com.java.slms.repository.TransferCertificateRequestRepository;
import com.java.slms.service.TransferCertificateRequestService;
import com.java.slms.util.RequestStatus;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
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

    @Transactional
    @Override
    public TransferCertificateRequestDto createTransferCertificateRequest(String studentPan, TCReasonDto reasonDto)
    {
        Student student = fetchStudentByPan(studentPan);

        checkExistingPendingOrApprovedRequest(student);

        Session activeSession = fetchActiveSession();

        TransferCertificateRequest tcRequest = buildTransferCertificateRequest(student, activeSession, reasonDto);

        TransferCertificateRequest savedRequest = tcRequestRepository.save(tcRequest);

        return mapToDto(savedRequest);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TransferCertificateRequestDto> getAllRequestsByStudentPan(String studentPan)
    {
        Student student = fetchStudentByPan(studentPan);

        List<TransferCertificateRequest> tcRequests = tcRequestRepository.findByStudent(student);

        return tcRequests.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public TransferCertificateRequestDto processRequestDecision(Long requestId, String adminReply, RequestStatus decision)
    {
        validateDecision(decision);

        TransferCertificateRequest request = fetchRequestById(requestId);

        validateRequestPending(request);

        request.setStatus(decision);
        request.setAdminReplyToStudent(adminReply);
        request.setAdminActionDate(LocalDate.now());

        TransferCertificateRequest updatedRequest = tcRequestRepository.save(request);

        return mapToDto(updatedRequest);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TransferCertificateRequestDto> getAllRequests(RequestStatus status)
    {
        Sort sortByRequestDateDesc = Sort.by(Sort.Direction.DESC, "requestDate");

        List<TransferCertificateRequest> requests = (status != null) ?
                tcRequestRepository.findAllByStatus(status, sortByRequestDateDesc) :
                tcRequestRepository.findAll(sortByRequestDateDesc);

        return requests.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferCertificateRequestDto> getAllRequestForwardedByAdminToClassTeacher(Teacher teacher)
    {
        List<TransferCertificateRequest> requests = tcRequestRepository
                .findByApprovedByClassTeacherAndStatus(teacher, RequestStatus.PROCESSING);

        return requests.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void forwardTCRequestToClassTeacher(Long tcRequestId, Admin admin, AdminToTeacherDto adminToTeacherDto)
    {
        TransferCertificateRequest request = fetchRequestById(tcRequestId);

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
    public void replyTCRequestToAdmin(Long tcRequestId, Teacher teacher, TeacherToAdminDto teacherToAdminDto)
    {
        TransferCertificateRequest request = fetchRequestById(tcRequestId);

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

    private Student fetchStudentByPan(String studentPan)
    {
        return studentRepository.findById(studentPan)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with PAN: " + studentPan));
    }

    private void checkExistingPendingOrApprovedRequest(Student student)
    {
        boolean exists = tcRequestRepository.existsByStudentAndStatusIn(student, List.of(RequestStatus.PENDING, RequestStatus.APPROVED));
        if (exists)
        {
            throw new AlreadyExistException("Transfer Certificate request already exists or approved for this student.");
        }
    }

    private Session fetchActiveSession()
    {
        return sessionRepository.findByActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No active session found"));
    }

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
        return dto;
    }

    private void validateDecision(RequestStatus decision)
    {
        if (decision != RequestStatus.APPROVED && decision != RequestStatus.REJECTED)
        {
            throw new WrongArgumentException("Decision must be either APPROVED or REJECTED");
        }
    }

    private TransferCertificateRequest fetchRequestById(Long requestId)
    {
        return tcRequestRepository.findById(requestId)
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
