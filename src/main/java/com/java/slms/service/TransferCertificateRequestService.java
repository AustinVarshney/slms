package com.java.slms.service;

import com.java.slms.dto.AdminToTeacherDto;
import com.java.slms.dto.TCReasonDto;
import com.java.slms.dto.TeacherToAdminDto;
import com.java.slms.dto.TransferCertificateRequestDto;
import com.java.slms.model.Admin;
import com.java.slms.model.Teacher;
import com.java.slms.util.RequestStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TransferCertificateRequestService
{
    @Transactional
    TransferCertificateRequestDto createTransferCertificateRequest(String studentPan, TCReasonDto tcReasonDto);

    List<TransferCertificateRequestDto> getAllRequestsByStudentPan(String studentPan);

    @Transactional
    TransferCertificateRequestDto processRequestDecision(Long requestId, String adminReply, RequestStatus decision);

    @Transactional(readOnly = true)
    List<TransferCertificateRequestDto> getAllRequests(RequestStatus status);

    List<TransferCertificateRequestDto> getAllRequestForwardedByAdminToClassTeacher(Teacher teacher);

    void forwardTCRequestToClassTeacher(Long tcRequestId, Admin admin, AdminToTeacherDto adminToTeacherDto);

    void replyTCRequestToAdmin(Long tcRequestId, Teacher teacher, TeacherToAdminDto teacherToAdminDto);
}
