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
    TransferCertificateRequestDto createTransferCertificateRequest(String studentPan, TCReasonDto reasonDto, Long schoolId);

    List<TransferCertificateRequestDto> getAllRequestsByStudentPan(String studentPan, Long schoolId);

    @Transactional
    TransferCertificateRequestDto processRequestDecision(Long requestId, String adminReply, RequestStatus decision, Long schoolId);

    @Transactional(readOnly = true)
    List<TransferCertificateRequestDto> getAllRequests(RequestStatus status, Long schoolId);

    List<TransferCertificateRequestDto> getAllRequestForwardedByAdminToClassTeacher(Teacher teacher, Long schoolId);

    void forwardTCRequestToClassTeacher(Long tcRequestId, Admin admin, AdminToTeacherDto adminToTeacherDto, Long schoolId);

    void replyTCRequestToAdmin(Long tcRequestId, Teacher teacher, TeacherToAdminDto teacherToAdminDto, Long schoolId);
}
