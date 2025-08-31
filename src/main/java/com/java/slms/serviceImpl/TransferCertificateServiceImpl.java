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
        // Fetch student entity
        Student student = studentRepository.findById(studentPan)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with PAN: " + studentPan));

        // Check for existing pending or approved request for this student
        boolean exists = tcRequestRepository.existsByStudentAndStatusIn(student, List.of(RequestStatus.PENDING, RequestStatus.APPROVED));
        if (exists)
        {
            throw new AlreadyExistException("Transfer Certificate request already exists or approved for this student.");
        }

        // Fetch active session
        Session activeSession = sessionRepository.findByActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("No active session found"));


        // Map requestDto to entity
        TransferCertificateRequest tcRequest = new TransferCertificateRequest();
        tcRequest.setStudent(student);
        tcRequest.setSession(activeSession);
        tcRequest.setLastClass(student.getCurrentClass());
        tcRequest.setRequestDate(LocalDate.now());
        tcRequest.setStatus(RequestStatus.PENDING);
        tcRequest.setReason(reasonDto.getReason());
        tcRequest.setClassTeacherApprovalStatus(RequestStatus.PENDING);

        // Save the request entity
        TransferCertificateRequest savedRequest = tcRequestRepository.save(tcRequest);

        // Map to DTO and populate additional fields for response
        TransferCertificateRequestDto responseDto = modelMapper.map(savedRequest, TransferCertificateRequestDto.class);
        responseDto.setStudentPanNumber(student.getPanNumber());
        responseDto.setStudentName(student.getName());
        responseDto.setSessionId(activeSession.getId());
        responseDto.setSessionName(activeSession.getName());
        responseDto.setClassId(student.getCurrentClass().getId());
        responseDto.setClassName(student.getCurrentClass().getClassName());

        return responseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferCertificateRequestDto> getAllRequestsByStudentPan(String studentPan)
    {
        Student student = studentRepository.findById(studentPan)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with PAN: " + studentPan));

        List<TransferCertificateRequest> tcRequests = tcRequestRepository.findByStudent(student);

        return tcRequests.stream()
                .map(tcRequest -> modelMapper.map(tcRequest, TransferCertificateRequestDto.class))
                .toList();
    }

    @Transactional
    @Override
    public TransferCertificateRequestDto processRequestDecision(Long requestId, String adminReply, RequestStatus decision)
    {
        if (decision != RequestStatus.APPROVED && decision != RequestStatus.REJECTED)
        {
            throw new WrongArgumentException("Decision must be either APPROVED or REJECTED");
        }

        TransferCertificateRequest request = tcRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING)
        {
            throw new WrongArgumentException("Only pending requests can be processed.");
        }

        request.setStatus(decision);
        request.setAdminReplyToStudent(adminReply);
        request.setAdminActionDate(LocalDate.now());

        TransferCertificateRequest updatedRequest = tcRequestRepository.save(request);

        return modelMapper.map(updatedRequest, TransferCertificateRequestDto.class);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TransferCertificateRequestDto> getAllRequests(RequestStatus status)
    {
        Sort sortByRequestDateDesc = Sort.by(Sort.Direction.DESC, "requestDate");
        List<TransferCertificateRequest> requests;

        if (status != null)
        {
            requests = tcRequestRepository.findAllByStatus(status, sortByRequestDateDesc);
        }
        else
        {
            requests = tcRequestRepository.findAll(sortByRequestDateDesc);
        }

        return requests.stream()
                .map(tcRequest -> modelMapper.map(tcRequest, TransferCertificateRequestDto.class))
                .toList();
    }

    @Override
    public void forwardTCRequestToClassTeacher(Long tcRequestId, Admin admin, AdminToTeacherDto adminToTeacherDto)
    {
        TransferCertificateRequest request = tcRequestRepository.findById(tcRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + tcRequestId));

        if (request.getStatus().equals(RequestStatus.APPROVED))
        {
            throw new WrongArgumentException("Request Already Approved with id " + request.getId());
        }

        request.setAdminMessageToTeacher(adminToTeacherDto.getAdminMessageToTeacher());
        request.setApprovedByClassTeacher(request.getLastClass().getClassTeacher());
        request.setStatus(RequestStatus.PROCESSING);
        request.setApprovedByAdmin(admin);

        tcRequestRepository.save(request);
    }

    @Override
    public void replyTCRequestToAdmin(Long tcRequestId, Teacher teacher, TeacherToAdminDto teacherToAdminDto)
    {
        TransferCertificateRequest request = tcRequestRepository.findById(tcRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + tcRequestId));

        if (!teacher.getId().equals(request.getApprovedByClassTeacher().getId()))
        {
            throw new WrongArgumentException("You are not authorized to respond to this TC request.");
        }

        request.setClassTeacherApprovalStatus(teacherToAdminDto.getStatus());
        request.setTeacherReplyToAdmin(teacherToAdminDto.getTeacherReplyToAdmin());
        request.setTeacherActionDate(LocalDate.now());
        tcRequestRepository.save(request);

    }


}
